package net.blay09.mods.firstaid.handler;

import net.blay09.mods.firstaid.FirstAid;
import net.blay09.mods.firstaid.ModConfig;
import net.blay09.mods.firstaid.PlayerKnockedOutEvent;
import net.blay09.mods.firstaid.capability.CapabilityFirstAid;
import net.blay09.mods.firstaid.capability.IFirstAid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DeathHandler {
	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayerMP) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();

			// If the player fell into the void, there's no rescuing
			if(event.getSource() == DamageSource.OUT_OF_WORLD) {
				player.getEntityData().setBoolean("IgnoreFirstAidDeath", true);
				return;
			}

			// If FirstAidDeath is set, this should be treated as a normal death
			if(event.getSource() == FirstAid.notRescuedInTime || player.getEntityData().getBoolean("IgnoreFirstAidDeath")) {
				return;
			}

			// Fire event for compatibility addons
			MinecraftForge.EVENT_BUS.post(new PlayerKnockedOutEvent(player, event.getSource()));

//			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
//				player.removeActivePotionEffect(potionEffect.getPotion());
//			}
//			player.setSneaking(false);

			// Cancel event - we're taking over from here
			event.setCanceled(true);

			// If enabled, show a death message
			if (player.world.getGameRules().getBoolean("showDeathMessages")) {
				MinecraftServer server = player.world.getMinecraftServer();
				if(server != null) {
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
		if(event.phase == TickEvent.Phase.START) {
			if(event.player.getHealth() <= 0f && !event.player.getEntityData().getBoolean("IgnoreFirstAidDeath")) {
				// Prevent deathTime from removing the entity from the world
				if(event.player.deathTime == 19) {
					event.player.deathTime = 18;
				}
				// Update our death timer instead
				IFirstAid firstAid = event.player.getCapability(CapabilityFirstAid.FIRST_AID_CAPABILITY, null);
				if(firstAid != null) {
					firstAid.setDeathTime(firstAid.getDeathTime() + 1);
					if(firstAid.getDeathTime() >= ModConfig.maxDeathTicks) {
						event.player.getEntityData().setBoolean("IgnoreFirstAidDeath", true);
						event.player.getCombatTracker().trackDamage(FirstAid.notRescuedInTime, 0, 0);
						event.player.onDeath(FirstAid.notRescuedInTime);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		event.player.getEntityData().removeTag("IgnoreFirstAidDeath");
	}
}
