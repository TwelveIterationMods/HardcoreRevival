package net.blay09.mods.hardcorerevival;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.hardcorerevival.api.PlayerKnockedOutEvent;
import net.blay09.mods.hardcorerevival.api.PlayerRescuedEvent;
import net.blay09.mods.hardcorerevival.api.PlayerRevivedEvent;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalRules;
import net.blay09.mods.hardcorerevival.handler.KnockoutSyncHandler;
import net.blay09.mods.hardcorerevival.network.RevivalProgressMessage;
import net.blay09.mods.hardcorerevival.network.RevivalSuccessMessage;
import net.blay09.mods.hardcorerevival.stats.ModStats;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.scores.Team;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class HardcoreRevivalManager {
    public static final ResourceKey<DamageType> NOT_RESCUED_IN_TIME = ResourceKey.create(Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "not_rescued_in_time"));

    public static void knockout(ServerPlayer player, DamageSource source) {
        if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            return;
        }

        player.closeContainer();
        player.stopUsingItem();
        player.stopRiding();
        player.removeEffect(MobEffects.REGENERATION);

        PlayerHardcoreRevivalManager.setKnockedOut(player, true);
        PlayerHardcoreRevivalManager.setKnockoutTicksPassed(player, 0);
        Entity attacker = source.getEntity();
        PlayerHardcoreRevivalManager.setKnockoutAttackerId(player, attacker != null ? attacker.getUUID() : null);
        if (attacker instanceof Mob mob && mob.getTarget() == player) {
            mob.setTarget(null);
        }

        PlayerHardcoreRevivalManager.setLastKnockoutAt(player, System.currentTimeMillis());
        player.awardStat(ModStats.knockouts);

        // Punish consecutive knockouts
        final var lastRescuedAt = PlayerHardcoreRevivalManager.getLastRescuedAt(player);
        final var consecutiveThresholdSeconds = HardcoreRevivalConfig.getActive().consecutiveKnockoutThresholdSeconds;
        final var secondsSinceLastRescue = (System.currentTimeMillis() - lastRescuedAt) / 1000;
        final var isConsecutiveKnockout = consecutiveThresholdSeconds > 0 && lastRescuedAt > 0 && secondsSinceLastRescue <= consecutiveThresholdSeconds;
        if (isConsecutiveKnockout) {
            if (HardcoreRevivalConfig.getActive().resumeTimerOnConsecutiveKnockout) {
                PlayerHardcoreRevivalManager.setKnockoutTicksPassed(player, PlayerHardcoreRevivalManager.getLastKnockoutTicksPassed(player));
            }
            final var multiplyTimerOnConsecutiveKnockout = HardcoreRevivalConfig.getActive().multiplyTimerOnConsecutiveKnockout;
            final var maxTicksUntilDeath = HardcoreRevivalConfig.getActive().secondsUntilDeath * 20;
            final var ticksLeft = maxTicksUntilDeath - PlayerHardcoreRevivalManager.getKnockoutTicksPassed(player);
            final var newTicksLeft = (int) (ticksLeft * multiplyTimerOnConsecutiveKnockout);
            PlayerHardcoreRevivalManager.setKnockoutTicksPassed(player, maxTicksUntilDeath - newTicksLeft);
        }

        // Fire event for compatibility addons
        PlayerKnockedOutEvent.EVENT.invoker().accept(new PlayerKnockedOutEvent(player, source));

        // If enabled, show a death message
        if (player.level().getGameRules().get(GameRules.SHOW_DEATH_MESSAGES)) {
            final var server = player.level().getServer();
            final var team = player.getTeam();
            if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                    server.getPlayerList().broadcastSystemToTeam(player, player.getCombatTracker().getDeathMessage());
                } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                    server.getPlayerList().broadcastSystemToAllExceptTeam(player, player.getCombatTracker().getDeathMessage());
                }
            } else {
                server.getPlayerList().broadcastSystemMessage(player.getCombatTracker().getDeathMessage(), false);
            }
        }

        updateKnockoutEffects(player);
    }

    public static void wakeup(Player player) {
        wakeup(player, true);
    }

    public static void wakeup(Player player, boolean applyEffects) {
        PlayerHardcoreRevivalManager.setLastRescuedAt(player, System.currentTimeMillis());
        PlayerHardcoreRevivalManager.setLastKnockoutTicksPassed(player, PlayerHardcoreRevivalManager.getKnockoutTicksPassed(player));
        reset(player);
        player.awardStat(ModStats.timesRescued);

        if (applyEffects) {
            HardcoreRevivalRules.applyRevivedEffects(player);
        }

        PlayerRevivedEvent.EVENT.invoker().accept(new PlayerRevivedEvent(player));
    }

    public static void finishRescue(Player player) {
        Player rescueTarget = PlayerHardcoreRevivalManager.getRescueTarget(player);
        if (rescueTarget != null) {
            MinecraftServer server = rescueTarget.level().getServer();
            if (server != null) {
                final var ruleResult = HardcoreRevivalRules.executeRescueRules(player, rescueTarget);
                if (ruleResult.right().isPresent()) {
                    abortRescue(player);
                    if (player instanceof ServerPlayer serverPlayer) {
                        Balm.networking().sendTo(serverPlayer, new RevivalProgressMessage(-1, -1, ruleResult));
                    }
                    return;
                }

                wakeup(rescueTarget);
                player.awardStat(ModStats.playersRevived);

                Balm.networking().sendTo(player, new RevivalProgressMessage(rescueTarget.getId(), -1f));
                Balm.networking().sendTo(rescueTarget, new RevivalSuccessMessage(rescueTarget.getId()));
                Balm.networking().sendToTracking(rescueTarget, new RevivalSuccessMessage(rescueTarget.getId()));

                PlayerHardcoreRevivalManager.setRescueTarget(player, null);

                PlayerRescuedEvent.EVENT.invoker().accept(new PlayerRescuedEvent(rescueTarget, player));
            }
        }

        Balm.hooks().setForcedPose(player, null);
    }

    public static void abortRescue(Player player) {
        Player rescueTarget = PlayerHardcoreRevivalManager.getRescueTarget(player);
        if (rescueTarget != null) {
            PlayerHardcoreRevivalManager.setRescueTime(player, 0);
            PlayerHardcoreRevivalManager.setRescueTarget(player, null);
            if (player instanceof ServerPlayer serverPlayer) {
                Balm.networking().sendTo(serverPlayer, new RevivalProgressMessage(-1, -1, Either.left(true)));
            }
            KnockoutSyncHandler.sendHardcoreRevivalData(rescueTarget, rescueTarget);

            Balm.hooks().setForcedPose(player, null);
        }
    }

    public static void notRescuedInTime(ServerPlayer player) {
        final var damageTypes = player.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
        final var damageType = damageTypes.getOrThrow(NOT_RESCUED_IN_TIME);
        final var knockoutAttackerId = PlayerHardcoreRevivalManager.getKnockoutAttackerId(player);
        final var knockoutAttacker = knockoutAttackerId != null ? findLoadedEntity(player.level().getServer(), knockoutAttackerId) : null;
        final var damageSource = knockoutAttacker != null
                ? new DamageSource(damageType, knockoutAttacker)
                : new DamageSource(damageType);
        PlayerHardcoreRevivalManager.setLastKnockoutTicksPassed(player, 0);
        final var damageApplied = player.hurtServer(player.level(), damageSource, Float.MAX_VALUE);
        if (!damageApplied) {
            HardcoreRevival.logger.error("Failed to apply knockout timeout damage to player {} ({})", player.getName().getString(), player.getUUID());
        }
        reset(player);
    }

    public static void reset(Player player) {
        PlayerHardcoreRevivalManager.setKnockedOut(player, false);
        PlayerHardcoreRevivalManager.setKnockoutTicksPassed(player, 0);
        PlayerHardcoreRevivalManager.setKnockoutAttackerId(player, null);

        updateKnockoutEffects(player);
    }

    @Nullable
    private static Entity findLoadedEntity(@Nullable MinecraftServer server, UUID entityId) {
        if (server != null) {
            for (ServerLevel level : server.getAllLevels()) {
                Entity entity = level.getEntity(entityId);
                if (entity != null) {
                    return entity;
                }
            }
        }
        return null;
    }

    public static void updateKnockoutEffects(Player player) {
        if (HardcoreRevivalConfig.getActive().glowOnKnockout) {
            player.setGlowingTag(PlayerHardcoreRevivalManager.isKnockedOut(player));
        }

        Balm.hooks().setForcedPose(player, PlayerHardcoreRevivalManager.isKnockedOut(player) ? Pose.FALL_FLYING : null);

        KnockoutSyncHandler.sendHardcoreRevivalDataToWatching(player);
    }

    public static void startRescue(Player player, Player target) {
        if (player instanceof ServerPlayer serverPlayer) {
            final var ruleResult = HardcoreRevivalRules.simulateRescueRules(player, target);
            final var canRevive = ruleResult.left().isPresent();
            if (canRevive) {
                Balm.networking().sendTo(serverPlayer, new RevivalProgressMessage(target.getId(), 0.1f, ruleResult));
            } else {
                Balm.networking().sendTo(serverPlayer, new RevivalProgressMessage(-1, -1, ruleResult));
                return;
            }
        }

        PlayerHardcoreRevivalManager.setRescueTarget(player, target);
        PlayerHardcoreRevivalManager.setRescueTime(player, 0);
        KnockoutSyncHandler.sendHardcoreRevivalData(target, target, true);

        Balm.hooks().setForcedPose(player, Pose.CROUCHING);
    }

    public static boolean isRescuing(Player player) {
        Player rescueTarget = PlayerHardcoreRevivalManager.getRescueTarget(player);
        return rescueTarget != null;
    }

    public static boolean isKnockedOut(Player player) {
        return PlayerHardcoreRevivalManager.isKnockedOut(player);
    }
}
