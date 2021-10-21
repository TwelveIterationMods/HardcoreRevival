package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
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
        if (event.getEntityLiving() instanceof Player player) {
            if (HardcoreRevival.getRevivalData(player).isKnockedOut()) {
                event.setCanceled(false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreakProgress(PlayerEvent.BreakSpeed event) {
        Player player = event.getPlayer();
        if (player != null && HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            event.setNewSpeed(0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player != null && HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            if (!(event instanceof PlayerInteractEvent.RightClickEmpty || event instanceof PlayerInteractEvent.LeftClickEmpty)) {
                if (!HardcoreRevivalConfig.getActive().allowBows || !(event.getItemStack().getItem() instanceof BowItem)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerUse(LivingEntityUseItemEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (event.isCancelable() && entity instanceof Player && HardcoreRevival.getRevivalData(entity).isKnockedOut()) {
            if (!HardcoreRevivalConfig.getActive().allowBows || !(event.getItem().getItem() instanceof BowItem)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerToss(ItemTossEvent event) {
        Player player = event.getPlayer();
        if (HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            // We try to suppress the drop on the client too, but if that failed for some reason, just try to revert the action
            if (player.addItem(event.getEntityItem().getItem())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getPlayer();
        if (player != null && HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            if (HardcoreRevivalConfig.getActive().allowUnarmedMelee && player.getMainHandItem().isEmpty()) {
                return;
            }
            event.setCanceled(true);
        }
    }
}
