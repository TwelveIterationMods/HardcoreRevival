package net.blay09.mods.hardcorerevival.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RestrictionHandler {
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBreakProgress(PlayerEvent.BreakSpeed event) {
		EntityPlayer player = event.getEntityPlayer();
		if (player != null && player.getHealth() <= 0f) {
			event.setNewSpeed(0f);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		if (player != null && player.getHealth() <= 0f) {
			if (!(event instanceof PlayerInteractEvent.RightClickEmpty || event instanceof PlayerInteractEvent.LeftClickEmpty)) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerUse(LivingEntityUseItemEvent event) {
		if (event.isCancelable() && event.getEntityLiving() instanceof EntityPlayer) {
//			if(ModConfig.allowBows && event.getItem().getItem() instanceof ItemBow) {
//				return;
//			}
			if (event.getEntityLiving().getHealth() <= 0f) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onAttack(AttackEntityEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		if (player != null && player.getHealth() <= 0f) {
//			if(ModConfig.allowUnarmedMelee && player.getHeldItemMainhand().isEmpty()) {
//				return;
//			}
			event.setCanceled(true);
		}
	}
}
