package net.blay09.mods.hardcorerevival;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.hardcorerevival.api.PlayerKnockedOutEvent;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataCapability;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataImpl;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfigData;
import net.blay09.mods.hardcorerevival.handler.KnockoutSyncHandler;
import net.blay09.mods.hardcorerevival.mixin.ServerPlayerAccessor;
import net.blay09.mods.hardcorerevival.network.RevivalProgressMessage;
import net.blay09.mods.hardcorerevival.network.RevivalSuccessMessage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class HardcoreRevivalManager implements IHardcoreRevivalManager {
    public static final DamageSource notRescuedInTime = new DamageSource("not_rescued_in_time")
            .bypassMagic()
            .bypassArmor()
            .bypassInvul();

    @Override
    public HardcoreRevivalData getRevivalData(Player player) {
        return DistExecutor.runForDist(() -> () -> {
            if (Minecraft.getInstance().player == player) {
                return HardcoreRevival.getClientRevivalData();
            }

            LazyOptional<HardcoreRevivalData> revivalData = player.getCapability(HardcoreRevivalDataCapability.REVIVAL_CAPABILITY);
            return revivalData.orElseGet(HardcoreRevivalDataImpl::new);
        }, () -> () -> {
            LazyOptional<HardcoreRevivalData> revivalData = player.getCapability(HardcoreRevivalDataCapability.REVIVAL_CAPABILITY);
            return revivalData.orElseGet(HardcoreRevivalDataImpl::new);
        });
    }

    public void knockout(Player player, DamageSource source) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        if (revivalData.isKnockedOut()) {
            return;
        }

        player.removeEffect(MobEffects.REGENERATION);

        revivalData.setKnockedOut(true);
        revivalData.setKnockoutTicksPassed(0);

        // Fire event for compatibility addons
        MinecraftForge.EVENT_BUS.post(new PlayerKnockedOutEvent(player, source));

        // If enabled, show a death message
        if (player.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                Team team = player.getTeam();
                if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
                    if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                        server.getPlayerList().broadcastToTeam(player, player.getCombatTracker().getDeathMessage());
                    } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                        server.getPlayerList().broadcastToAllExceptTeam(player, player.getCombatTracker().getDeathMessage());
                    }
                } else {
                    server.getPlayerList().broadcastMessage(player.getCombatTracker().getDeathMessage(), ChatType.SYSTEM, Util.NIL_UUID);
                }
            }
        }

        updateKnockoutEffects(player);
    }

    public void wakeup(Player player) {
        wakeup(player, true);
    }

    public void wakeup(Player player, boolean applyEffects) {
        reset(player);

        if (applyEffects) {
            HardcoreRevivalConfigData config = HardcoreRevivalConfig.getActive();
            player.setHealth(config.rescueRespawnHealth);
            player.getFoodData().setFoodLevel(config.rescueRespawnFoodLevel);
            // client only, won't bother: player.getFoodStats().setFoodSaturationLevel((float) config.getRescueRespawnFoodSaturation());

            for (String effectString : config.rescueRespawnEffects) {
                String[] parts = effectString.split("\\|");
                ResourceLocation registryName = ResourceLocation.tryParse(parts[0]);
                if (registryName != null) {
                    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(registryName);
                    if (effect != null) {
                        int duration = tryParseInt(parts.length >= 2 ? parts[1] : null, 600);
                        int amplifier = tryParseInt(parts.length >= 3 ? parts[2] : null, 0);
                        player.addEffect(new MobEffectInstance(effect, duration, amplifier));
                    } else {
                        HardcoreRevival.logger.info("Invalid rescue potion effect '{}'" + parts[0]);
                    }
                } else {
                    HardcoreRevival.logger.info("Invalid rescue potion effect '{}'" + parts[0]);
                }
            }
        }
    }

    private int tryParseInt(@Nullable String text, int defaultVal) {
        if (text != null) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                return defaultVal;
            }
        }
        return defaultVal;
    }

    public void finishRescue(Player player) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        Player rescueTarget = revivalData.getRescueTarget();
        if (rescueTarget != null) {
            MinecraftServer server = rescueTarget.getServer();
            if (server != null) {
                wakeup(rescueTarget);

                Balm.getNetworking().sendTo(player, new RevivalProgressMessage(rescueTarget.getId(), -1f));
                Balm.getNetworking().sendTo(rescueTarget, new RevivalSuccessMessage(rescueTarget.getId()));
                Balm.getNetworking().sendToTracking(rescueTarget, new RevivalSuccessMessage(rescueTarget.getId()));

                revivalData.setRescueTarget(null);
            }
        }

        player.setForcedPose(null);
    }

    public void abortRescue(Player player) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        Player rescueTarget = revivalData.getRescueTarget();
        if (rescueTarget != null) {
            revivalData.setRescueTime(0);
            revivalData.setRescueTarget(null);
            Balm.getNetworking().sendTo(player,new RevivalProgressMessage(-1, -1) );
            KnockoutSyncHandler.sendHardcoreRevivalData(rescueTarget, rescueTarget, getRevivalData(rescueTarget));
        }

        player.setForcedPose(null);
    }

    public void notRescuedInTime(Player player) {
        // Disable respawn invulnerability to prevent players from surviving knockout after login with offline timer enabled
        if (player instanceof ServerPlayerAccessor accessor) {
            accessor.setSpawnInvulnerableTime(0);
        }

        player.hurt(notRescuedInTime, player.getHealth());
        reset(player);
    }

    public void reset(Player player) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        revivalData.setKnockedOut(false);
        revivalData.setKnockoutTicksPassed(0);

        updateKnockoutEffects(player);
    }

    public void updateKnockoutEffects(Player player) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        if (HardcoreRevivalConfig.getActive().glowOnKnockout) {
            player.setGlowingTag(revivalData.isKnockedOut());
        }

        player.setForcedPose(revivalData.isKnockedOut() ? Pose.FALL_FLYING : null);

        KnockoutSyncHandler.sendHardcoreRevivalDataToWatching(player, revivalData);
    }

    public void startRescue(Player player, Player target) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        revivalData.setRescueTarget(target);
        revivalData.setRescueTime(0);
        Balm.getNetworking().sendTo(player, new RevivalProgressMessage(target.getId(), 0.1f));
        KnockoutSyncHandler.sendHardcoreRevivalData(target, target, getRevivalData(target), true);

        player.setForcedPose(Pose.CROUCHING);
    }
}
