package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.minecraft.entity.LivingEntity;
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
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HardcoreRevival.MOD_ID)
public class KnockoutRestrictionHandler {

    @SubscribeEvent
    public static void onPlayerHeal(LivingHealEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            if (HardcoreRevival.getRevivalData(player).isKnockedOut()) {
                event.setCanceled(false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreakProgress(PlayerEvent.BreakSpeed event) {
        PlayerEntity player = event.getPlayer();
        if (player != null && HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            event.setNewSpeed(0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteract(PlayerInteractEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player != null && HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            if (!(event instanceof PlayerInteractEvent.RightClickEmpty || event instanceof PlayerInteractEvent.LeftClickEmpty)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerUse(LivingEntityUseItemEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (event.isCancelable() && entity instanceof PlayerEntity) {
            if (HardcoreRevivalConfig.COMMON.allowBows.get() && event.getItem().getItem() instanceof BowItem) {
                return;
            }

            if (HardcoreRevival.getRevivalData(entity).isKnockedOut()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerToss(ItemTossEvent event) {
        PlayerEntity player = event.getPlayer();
        if (HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            // We try to suppress the drop on the client too, but if that failed for some reason, just try to revert the action
            if (player.addItemStackToInventory(event.getEntityItem().getItem())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(AttackEntityEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player != null && HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            if (HardcoreRevivalConfig.COMMON.allowUnarmedMelee.get() && player.getHeldItemMainhand().isEmpty()) {
                return;
            }
            event.setCanceled(true);
        }
    }
}
