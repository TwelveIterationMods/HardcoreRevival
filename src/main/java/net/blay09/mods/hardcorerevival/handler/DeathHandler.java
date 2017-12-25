package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.ModConfig;
import net.blay09.mods.hardcorerevival.PlayerKnockedOutEvent;
import net.blay09.mods.hardcorerevival.capability.CapabilityHardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.IHardcoreRevival;
import net.blay09.mods.hardcorerevival.network.MessageDeathTime;
import net.blay09.mods.hardcorerevival.network.MessageDie;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DeathHandler {
	public static final String IGNORE_REVIVAL_DEATH = "IgnoreRevivalDeath";

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(LivingDeathEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayerMP) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();

			// If the player fell into the void, there's no rescuing
			if (event.getSource() == DamageSource.OUT_OF_WORLD) {
				player.getEntityData().setBoolean(IGNORE_REVIVAL_DEATH, true);
				NetworkHandler.instance.sendTo(new MessageDie(), (EntityPlayerMP) player);
				return;
			}

			// If IGNORE_REVIVAL_DEATH is set, this should be treated as a normal death
			if (event.getSource() == HardcoreRevival.notRescuedInTime || player.getEntityData().getBoolean(IGNORE_REVIVAL_DEATH)) {
				return;
			}

			// Fire event for compatibility addons
			MinecraftForge.EVENT_BUS.post(new PlayerKnockedOutEvent(player, event.getSource()));

			// Dead players glow
			if (ModConfig.glowOnDeath) {
				player.setGlowing(true);
			}

			// Cancel event - we're taking over from here
			event.setCanceled(true);

			// If enabled, show a death message
			if (player.world.getGameRules().getBoolean("showDeathMessages")) {
				MinecraftServer server = player.world.getMinecraftServer();
				if (server != null) {
					Team team = player.getTeam();
					if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
						if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
							server.getPlayerList().sendMessageToAllTeamMembers(player, player.getCombatTracker().getDeathMessage());
						} else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
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
				IHardcoreRevival revival = event.player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
				if (revival != null) {
					revival.setDeathTime(revival.getDeathTime() + 1);
					if (revival.getDeathTime() >= ModConfig.maxDeathTicks) {
						event.player.getEntityData().setBoolean(IGNORE_REVIVAL_DEATH, true);
						NetworkHandler.instance.sendTo(new MessageDie(), (EntityPlayerMP) event.player);
						event.player.getCombatTracker().trackDamage(HardcoreRevival.notRescuedInTime, 0, 0);
						event.player.onDeath(HardcoreRevival.notRescuedInTime);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		event.player.getEntityData().removeTag(IGNORE_REVIVAL_DEATH);
		if (ModConfig.glowOnDeath) {
			event.player.setGlowing(false);
		}
	}
}
