package net.blay09.mods.hardcorerevival;

import net.blay09.mods.hardcorerevival.api.PlayerKnockedOutEvent;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataCapability;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.config.IHardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.handler.KnockoutSyncHandler;
import net.blay09.mods.hardcorerevival.network.RevivalProgressMessage;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.blay09.mods.hardcorerevival.network.RevivalSuccessMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

public class HardcoreRevivalManager implements IHardcoreRevivalManager {
    public static final DamageSource notRescuedInTime = new DamageSource("not_rescued_in_time")
            .setDamageIsAbsolute()
            .setDamageBypassesArmor();

    @Override
    public HardcoreRevivalData getRevivalData(PlayerEntity player) {
        return DistExecutor.runForDist(() -> () -> {
            if (Minecraft.getInstance().player == player) {
                return HardcoreRevival.getClientRevivalData();
            }

            LazyOptional<HardcoreRevivalData> revivalData = player.getCapability(HardcoreRevivalDataCapability.REVIVAL_CAPABILITY);
            return revivalData.orElseGet(() -> Objects.requireNonNull(HardcoreRevivalDataCapability.REVIVAL_CAPABILITY.getDefaultInstance()));
        }, () -> () -> {
            LazyOptional<HardcoreRevivalData> revivalData = player.getCapability(HardcoreRevivalDataCapability.REVIVAL_CAPABILITY);
            return revivalData.orElseGet(() -> Objects.requireNonNull(HardcoreRevivalDataCapability.REVIVAL_CAPABILITY.getDefaultInstance()));
        });
    }

    public void knockout(PlayerEntity player, DamageSource source) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        if (revivalData.isKnockedOut()) {
            return;
        }

        revivalData.setKnockedOut(true);

        // Fire event for compatibility addons
        MinecraftForge.EVENT_BUS.post(new PlayerKnockedOutEvent(player, source));

        // If enabled, show a death message
        if (player.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
            MinecraftServer server = player.world.getServer();
            if (server != null) {
                Team team = player.getTeam();
                if (team != null && team.getDeathMessageVisibility() != Team.Visible.ALWAYS) {
                    if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OTHER_TEAMS) {
                        server.getPlayerList().sendMessageToAllTeamMembers(player, player.getCombatTracker().getDeathMessage());
                    } else if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OWN_TEAM) {
                        server.getPlayerList().sendMessageToTeamOrAllPlayers(player, player.getCombatTracker().getDeathMessage());
                    }
                } else {
                    server.getPlayerList().func_232641_a_(player.getCombatTracker().getDeathMessage(), ChatType.SYSTEM, Util.DUMMY_UUID); // sendMessage
                }
            }
        }

        updateKnockoutEffects(player);
    }

    public void wakeup(PlayerEntity player) {
        wakeup(player, true);
    }

    public void wakeup(PlayerEntity player, boolean applyEffects) {
        reset(player);

        if (applyEffects) {
            IHardcoreRevivalConfig config = HardcoreRevivalConfig.getActive();
            player.setHealth(config.getRescueRespawnHealth());
            player.getFoodStats().setFoodLevel(config.getRescueRespawnFoodLevel());
            player.getFoodStats().setFoodSaturationLevel((float) config.getRescueRespawnFoodSaturation());

            for (String effectString : config.getRescueRespawnEffects()) {
                String[] parts = effectString.split("\\|");
                ResourceLocation registryName = ResourceLocation.tryCreate(parts[0]);
                if (registryName != null) {
                    Effect effect = ForgeRegistries.POTIONS.getValue(registryName);
                    if (effect != null) {
                        int duration = tryParseInt(parts.length >= 2 ? parts[1] : null, 600);
                        int amplifier = tryParseInt(parts.length >= 3 ? parts[2] : null, 0);
                        player.addPotionEffect(new EffectInstance(effect, duration, amplifier));
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

    public void finishRescue(PlayerEntity player) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        PlayerEntity rescueTarget = revivalData.getRescueTarget();
        if (rescueTarget != null) {
            MinecraftServer server = rescueTarget.getServer();
            if (server != null) {
                wakeup(rescueTarget);

                NetworkHandler.sendToPlayer(player, new RevivalProgressMessage(rescueTarget.getEntityId(), -1f));
                NetworkHandler.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> rescueTarget), new RevivalSuccessMessage(rescueTarget.getEntityId()));

                revivalData.setRescueTarget(null);
            }
        }

        player.setForcedPose(null);
    }

    public void abortRescue(PlayerEntity player) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        PlayerEntity rescueTarget = revivalData.getRescueTarget();
        if (rescueTarget != null) {
            revivalData.setRescueTime(0);
            revivalData.setRescueTarget(null);
            NetworkHandler.sendToPlayer(player, new RevivalProgressMessage(-1, -1));
            KnockoutSyncHandler.sendHardcoreRevivalData(rescueTarget, rescueTarget, getRevivalData(rescueTarget));
        }

        player.setForcedPose(null);
    }

    public void notRescuedInTime(PlayerEntity player) {
        player.attackEntityFrom(notRescuedInTime, player.getHealth());
        reset(player);
    }

    public void reset(PlayerEntity player) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        revivalData.setKnockedOut(false);
        revivalData.setKnockoutTicksPassed(0);

        updateKnockoutEffects(player);

        KnockoutSyncHandler.sendHardcoreRevivalDataToWatching(player, revivalData);
    }

    public void updateKnockoutEffects(PlayerEntity player) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        if (HardcoreRevivalConfig.getActive().isGlowOnKnockoutEnabled()) {
            player.setGlowing(revivalData.isKnockedOut());
        }

        player.setForcedPose(revivalData.isKnockedOut() ? Pose.FALL_FLYING : null);

        KnockoutSyncHandler.sendHardcoreRevivalDataToWatching(player, revivalData);
    }

    public void startRescue(PlayerEntity player, PlayerEntity target) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        revivalData.setRescueTarget(target);
        revivalData.setRescueTime(0);
        NetworkHandler.sendToPlayer(player, new RevivalProgressMessage(target.getEntityId(), 0.1f));
        KnockoutSyncHandler.sendHardcoreRevivalData(target, target, getRevivalData(target), true);

        player.setForcedPose(Pose.CROUCHING);
    }
}
