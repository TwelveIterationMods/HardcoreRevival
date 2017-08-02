package net.blay09.mods.firstaid.handler;

import net.blay09.mods.firstaid.ModConfig;
import net.blay09.mods.firstaid.capability.CapabilityFirstAid;
import net.blay09.mods.firstaid.capability.IFirstAid;
import net.blay09.mods.firstaid.network.MessageFirstAidProgress;
import net.blay09.mods.firstaid.network.MessageFirstAidSuccess;
import net.blay09.mods.firstaid.network.NetworkHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class RescueHandler {

	@SubscribeEvent
	public void onItemUse(LivingEntityUseItemEvent event) {
		if(event.getEntityLiving() instanceof EntityPlayer) {
			// Stop rescuing if the player does something other than rescuing
			abortRescue((EntityPlayer) event.getEntityLiving());
		}
	}

	@SubscribeEvent
	public void onAttack(AttackEntityEvent event) {
		// Stop rescuing if the player does something other than rescuing
		abortRescue(event.getEntityPlayer());
	}

	public static void startRescue(EntityPlayer player, EntityPlayer target) {
		IFirstAid firstAid = player.getCapability(CapabilityFirstAid.FIRST_AID_CAPABILITY, null);
		if(firstAid != null) {
			firstAid.setRescueTarget(target);
			firstAid.setRescueTime(0);
			NetworkHandler.instance.sendTo(new MessageFirstAidProgress(target.getEntityId(), 0f), (EntityPlayerMP) player);
		}
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if(event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {
			IFirstAid firstAid = event.player.getCapability(CapabilityFirstAid.FIRST_AID_CAPABILITY, null);
			if(firstAid != null && firstAid.getRescueTarget() != null) {
				// Stop rescuing if the target logged out
				if(firstAid.getRescueTarget().isDead) {
					abortRescue(event.player);
				} else {
					// Stop rescuing if the player is out of range
					float dist = event.player.getDistanceToEntity(firstAid.getRescueTarget());
					if (dist > ModConfig.maxRescueDist) {
						abortRescue(event.player);
					} else {
						int rescueTime = firstAid.getRescueTime() + 1;
						firstAid.setRescueTime(rescueTime);
						int step = ModConfig.rescueTime / 4;
						if(rescueTime >= ModConfig.rescueTime) {
							finishRescue(event.player);
						} else if(rescueTime % step == 0) {
							NetworkHandler.instance.sendTo(new MessageFirstAidProgress(firstAid.getRescueTarget().getEntityId(), (float) rescueTime / (float) ModConfig.rescueTime), (EntityPlayerMP) event.player);
						}
					}
				}
			}
		}
	}

	public static void finishRescue(EntityPlayer player) {
		IFirstAid firstAid = player.getCapability(CapabilityFirstAid.FIRST_AID_CAPABILITY, null);
		if(firstAid != null) {
			EntityPlayer target = firstAid.getRescueTarget();
			if(target != null) {
				MinecraftServer server = target.getServer();
				if (server != null) {
					target.setSpawnPoint(target.getPosition(), true); // TODO test spawnpoint issues
					EntityPlayerMP newPlayer = server.getPlayerList().recreatePlayerEntity((EntityPlayerMP) target, target.dimension, false);
					((EntityPlayerMP) target).connection.player = newPlayer;
					newPlayer.setHealth(1f);
					newPlayer.getFoodStats().setFoodLevel(5);
					newPlayer.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 20 * 30));
					newPlayer.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 20 * 60));
					newPlayer.inventory.copyInventory(target.inventory);
					newPlayer.experienceLevel = target.experienceLevel;
					newPlayer.experienceTotal = target.experienceTotal;
					newPlayer.experience = target.experience;
					newPlayer.setScore(target.getScore());
					NetworkHandler.instance.sendToAllAround(new MessageFirstAidSuccess(newPlayer.getPosition()), new NetworkRegistry.TargetPoint(newPlayer.dimension, newPlayer.posX, newPlayer.posY, newPlayer.posZ, 32));
				}
			}
		}
	}

	public static void abortRescue(EntityPlayer player) {
		IFirstAid firstAid = player.getCapability(CapabilityFirstAid.FIRST_AID_CAPABILITY, null);
		if(firstAid != null) {
			firstAid.setRescueTime(0);
			firstAid.setRescueTarget(null);
			NetworkHandler.instance.sendTo(new MessageFirstAidProgress(-1, -1), (EntityPlayerMP) player);
		}
	}

}
