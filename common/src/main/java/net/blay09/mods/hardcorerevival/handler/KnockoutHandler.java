package net.blay09.mods.hardcorerevival.handler;


import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.event.EventPhases;
import net.blay09.mods.balm.platform.event.callback.LivingEntityCallback;
import net.blay09.mods.balm.platform.event.callback.ServerPlayerCallback;
import net.blay09.mods.balm.platform.event.callback.ServerTickCallback;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.api.PlayerAboutToKnockOutEvent;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.mixin.LivingEntityAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;


public class KnockoutHandler {

    public static void initialize() {
        LivingEntityCallback.Death.Before.EVENT.register(EventPhases.HIGH, KnockoutHandler::allowPlayerDeath);
        ServerPlayerCallback.Respawn.EVENT.register(KnockoutHandler::onPlayerRespawn);

        ServerTickCallback.ServerPlayerTick.BEFORE.register(KnockoutHandler::onPlayerTick);
    }

    public static boolean allowPlayerDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity instanceof ServerPlayer player) {
            if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
                Entity attacker = damageSource.getEntity();
                if (attacker instanceof Mob mob) {
                    mob.setTarget(null);
                }
                player.setHealth(0.5f);
                return damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || damageSource.is(HardcoreRevivalManager.NOT_RESCUED_IN_TIME);
            }

            if (isKnockoutEnabledFor(player, damageSource)) {
                if (((LivingEntityAccessor) player).callCheckTotemDeathProtection(damageSource)) {
                    return false;
                }

                final var aboutToKnockOutEvent = new PlayerAboutToKnockOutEvent(player, damageSource);
                PlayerAboutToKnockOutEvent.EVENT.invoker().accept(aboutToKnockOutEvent);

                if (!aboutToKnockOutEvent.isCanceled()) {
                    HardcoreRevivalManager.knockout(player, damageSource);
                    player.setHealth(0.5f);
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isKnockoutEnabledFor(ServerPlayer player, DamageSource damageSource) {
        final var server = player.level().getServer();

        boolean canDamageSourceKnockout = !damageSource.is(DamageTypes.FELL_OUT_OF_WORLD) && !damageSource.is(HardcoreRevivalManager.NOT_RESCUED_IN_TIME);
        final var damageSourceId = player.level().getServer().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getKey(damageSource.type());
        if (!canDamageSourceKnockout || HardcoreRevivalConfig.getActive().instantDeathSources.contains(damageSourceId)) {
            return false;
        }

        final var attacker = damageSource.getEntity();
        if (attacker != null) {
            final var entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType());
            if (HardcoreRevivalConfig.getActive().instantDeathEntityTypes.contains(entityTypeId)) {
                return false;
            }
        }

        if (HardcoreRevivalConfig.getActive().disableInSingleplayer && server.isSingleplayer() && server.getPlayerCount() == 1) {
            return false;
        } else if (HardcoreRevivalConfig.getActive().disableInLonelyMultiplayer && !server.isSingleplayer() && server.getPlayerCount() == 1) {
            return false;
        }

        return true;
    }

    public static void onPlayerTick(ServerPlayer player) {
        //if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER) {
        if (PlayerHardcoreRevivalManager.isKnockedOut(player) && player.isAlive()) {
            // Make sure health stays locked at half a heart
            player.setHealth(1f);

            PlayerHardcoreRevivalManager.setKnockoutTicksPassed(player, PlayerHardcoreRevivalManager.getKnockoutTicksPassed(player) + 1);

            if (player.tickCount % 20 == 0) {
                Balm.hooks().setForcedPose(player, PlayerHardcoreRevivalManager.isKnockedOut(player) ? Pose.FALL_FLYING : null);
            }

            int maxTicksUntilDeath = HardcoreRevivalConfig.getActive().secondsUntilDeath * 20;
            if (maxTicksUntilDeath > 0 && PlayerHardcoreRevivalManager.getKnockoutTicksPassed(player) >= maxTicksUntilDeath) {
                HardcoreRevivalManager.notRescuedInTime(player);
            }
        }
    }

    public static void onPlayerRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        HardcoreRevivalManager.reset(newPlayer);
    }


}
