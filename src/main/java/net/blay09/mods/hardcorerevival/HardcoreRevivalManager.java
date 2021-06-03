package net.blay09.mods.hardcorerevival;

import net.blay09.mods.hardcorerevival.api.PlayerKnockedOutEvent;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataCapability;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.network.HardcoreRevivalDataMessage;
import net.blay09.mods.hardcorerevival.network.RevivalProgressMessage;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.blay09.mods.hardcorerevival.network.RevivalSuccessMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Objects;

public class HardcoreRevivalManager implements IHardcoreRevivalManager {
    public static final DamageSource notRescuedInTime = new DamageSource("not_rescued_in_time");

    @Override
    public HardcoreRevivalData getRevivalData(PlayerEntity player) {
        LazyOptional<HardcoreRevivalData> revivalData = player.getCapability(HardcoreRevivalDataCapability.REVIVAL_CAPABILITY);
        return revivalData.orElseGet(() -> Objects.requireNonNull(HardcoreRevivalDataCapability.REVIVAL_CAPABILITY.getDefaultInstance()));
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

        updateKnockoutEffects(player, true);
    }

    public static void wakeup(PlayerEntity player) {
        player.setHealth(HardcoreRevivalConfig.COMMON.rescueRespawnHealth.get());
        player.getFoodStats().setFoodLevel(HardcoreRevivalConfig.COMMON.rescueRespawnFoodLevel.get());
        player.addPotionEffect(new EffectInstance(Effects.HUNGER, 20 * 30)); // Hunger
        player.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 20 * 60)); // Weakness
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
    }

    public void abortRescue(PlayerEntity player) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        if (revivalData.getRescueTarget() != null) {
            revivalData.setRescueTime(0);
            revivalData.setRescueTarget(null);
            NetworkHandler.sendToPlayer(player, new RevivalProgressMessage(-1, -1));
        }
    }

    public void notRescuedInTime(PlayerEntity player) {
        player.getCombatTracker().trackDamage(notRescuedInTime, 0, 0);
        player.onDeath(notRescuedInTime);

        reset(player);
    }

    public void reset(PlayerEntity player) {
        updateKnockoutEffects(player, false);

        HardcoreRevivalData revivalData = getRevivalData(player);
        revivalData.setKnockoutTicksPassed(0);
    }

    public void updateKnockoutEffects(PlayerEntity player, boolean knockedOut) {
        if (HardcoreRevivalConfig.COMMON.glowOnDeath.get()) {
            player.setGlowing(knockedOut);
        }

        HardcoreRevivalData revivalData = getRevivalData(player);
        NetworkHandler.sendToPlayer(player, new HardcoreRevivalDataMessage(knockedOut, revivalData.getKnockoutTicksPassed()));
    }

    public void startRescue(PlayerEntity player, PlayerEntity target) {
        HardcoreRevivalData revivalData = getRevivalData(player);
        revivalData.setRescueTarget(target);
        revivalData.setRescueTime(0);
        NetworkHandler.sendToPlayer(player, new RevivalProgressMessage(target.getEntityId(), 0.1f));
    }
}
