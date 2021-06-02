package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RestrictionHandler {

    @SubscribeEvent
    public void onPlayerHeal(LivingHealEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            if (HardcoreRevivalManager.isKnockedOut(player)) {
                event.setCanceled(false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBreakProgress(PlayerEvent.BreakSpeed event) {
        PlayerEntity player = event.getPlayer();
        if (player != null && HardcoreRevivalManager.isKnockedOut(player)) {
            event.setNewSpeed(0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player != null && HardcoreRevivalManager.isKnockedOut(player)) {
            if (!(event instanceof PlayerInteractEvent.RightClickEmpty || event instanceof PlayerInteractEvent.LeftClickEmpty)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerUse(LivingEntityUseItemEvent event) {
        if (event.isCancelable() && event.getEntityLiving() instanceof PlayerEntity) {
            if (HardcoreRevivalConfig.COMMON.allowBows.get() && event.getItem().getItem() instanceof BowItem) {
                return;
            }

            if (HardcoreRevivalManager.isKnockedOut(((PlayerEntity) event.getEntityLiving()))) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerToss(ItemTossEvent event) {
        if (HardcoreRevivalManager.isKnockedOut(event.getPlayer())) {
            // We try to suppress the drop on the client too, but if that failed for some reason, just try to revert the action
            if (event.getPlayer().addItemStackToInventory(event.getEntityItem().getItem())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttack(AttackEntityEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player != null && HardcoreRevivalManager.isKnockedOut(player)) {
            if (HardcoreRevivalConfig.COMMON.allowUnarmedMelee.get() && player.getHeldItemMainhand().isEmpty()) {
                return;
            }
            event.setCanceled(true);
        }
    }
}
