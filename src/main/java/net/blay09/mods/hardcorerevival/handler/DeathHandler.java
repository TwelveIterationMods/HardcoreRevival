package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.PlayerKnockedOutEvent;
import net.blay09.mods.hardcorerevival.capability.CapabilityHardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.IHardcoreRevival;
import net.blay09.mods.hardcorerevival.network.MessageDie;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class DeathHandler {
    public static final String IGNORE_REVIVAL_DEATH = "IgnoreRevivalDeath";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();

            // If the player fell into the void, there's no rescuing
            if (event.getSource() == DamageSource.OUT_OF_WORLD) {
                player.getEntityData().putBoolean(IGNORE_REVIVAL_DEATH, true);
                NetworkHandler.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new MessageDie());
                return;
            }

            // If IGNORE_REVIVAL_DEATH is set, this should be treated as a normal death
            if (event.getSource() == HardcoreRevival.notRescuedInTime || player.getEntityData().getBoolean(IGNORE_REVIVAL_DEATH)) {
                return;
            }

            // Fire event for compatibility addons
            MinecraftForge.EVENT_BUS.post(new PlayerKnockedOutEvent(player, event.getSource()));

            // Dead players glow
            if (HardcoreRevivalConfig.COMMON.glowOnDeath.get()) {
                player.setGlowing(true);
            }

            // Cancel event - we're taking over from here
            event.setCanceled(true);

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
                        server.getPlayerList().sendMessage(player.getCombatTracker().getDeathMessage());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onDeathUpdate(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (event.player.getHealth() <= 0f && !event.player.getEntityData().getBoolean(IGNORE_REVIVAL_DEATH)) {
                // Prevent deathTime from removing the entity from the world
                if (event.player.deathTime == 19) {
                    event.player.deathTime = 18;
                }
                // Update our death timer instead
                LazyOptional<IHardcoreRevival> revival = event.player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
                revival.ifPresent(it -> {
                    it.setDeathTime(it.getDeathTime() + 1);
                    if (it.getDeathTime() >= HardcoreRevivalConfig.COMMON.maxDeathTicks.get()) {
                        event.player.getEntityData().putBoolean(IGNORE_REVIVAL_DEATH, true);
                        NetworkHandler.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.player), new MessageDie());
                        event.player.getCombatTracker().trackDamage(HardcoreRevival.notRescuedInTime, 0, 0);
                        event.player.onDeath(HardcoreRevival.notRescuedInTime);
                        it.setDeathTime(0);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        event.getPlayer().getEntityData().remove(IGNORE_REVIVAL_DEATH);

        if (HardcoreRevivalConfig.COMMON.glowOnDeath.get()) {
            event.getPlayer().setGlowing(false);
        }

        LazyOptional<IHardcoreRevival> revival = event.getPlayer().getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
        revival.ifPresent(it -> it.setDeathTime(0));
    }
}
