package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.*;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.tag.ModBlockTags;
import net.blay09.mods.hardcorerevival.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class KnockoutRestrictionHandler {

    public static void initialize() {
        Balm.getEvents().onEvent(UseBlockEvent.class, KnockoutRestrictionHandler::onUseBlock, EventPriority.Highest);
        Balm.getEvents().onEvent(UseItemEvent.class, KnockoutRestrictionHandler::onUseItem, EventPriority.Highest);
        Balm.getEvents().onEvent(TossItemEvent.class, KnockoutRestrictionHandler::onTossItem, EventPriority.Highest);
        Balm.getEvents().onEvent(PlayerAttackEvent.class, KnockoutRestrictionHandler::onAttack, EventPriority.Highest);
        Balm.getEvents().onEvent(DigSpeedEvent.class, KnockoutRestrictionHandler::onDigSpeed, EventPriority.Highest);
        Balm.getEvents().onEvent(LivingHealEvent.class, KnockoutRestrictionHandler::onHeal);
    }

    public static void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (HardcoreRevival.getRevivalData(player).isKnockedOut()) {
                event.setCanceled(false);
            }
        }
    }

    public static void onDigSpeed(DigSpeedEvent event) {
        Player player = event.getPlayer();
        if (player != null && HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            if (!mayBreakBlockKnockedOut(event.getState())) {
                event.setSpeedOverride(0f);
                event.setCanceled(true);
            }
        }
    }

    public static void onUseBlock(UseBlockEvent event) {
        Player player = event.getPlayer();
        if (HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            ItemStack itemStack = player.getItemInHand(event.getHand());
            final var level = event.getLevel();
            final var pos = event.getHitResult().getBlockPos();
            if (!mayUseItemKnockedOut(itemStack) && !mayUseBlockKnockedOut(level, pos, itemStack)) {
                event.setCanceled(true);
            }
        }
    }

    public static void onUseItem(UseItemEvent event) {
        LivingEntity player = event.getPlayer();
        if (HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            ItemStack itemStack = player.getItemInHand(event.getHand());
            if (!mayUseItemKnockedOut(itemStack)) {
                event.setCanceled(true);
            }
        }
    }

    public static void onTossItem(TossItemEvent event) {
        Player player = event.getPlayer();
        if (HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            if (!mayTossItemKnockedOut(event.getItemStack())) {
                // We try to suppress the drop on the client too, but if that failed for some reason, just try to revert the action
                if (player.addItem(event.getItemStack())) {
                    event.setCanceled(true);
                }
            }
        }
    }

    public static void onAttack(PlayerAttackEvent event) {
        Player player = event.getPlayer();
        if (player != null && HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            final var itemStack = player.getMainHandItem();
            if (HardcoreRevivalConfig.getActive().allowUnarmedMelee && itemStack.isEmpty()) {
                return;
            }

            if (!mayAttackWithItemKnockedOut(itemStack)) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean mayUseItemKnockedOut(ItemStack itemStack) {
        if (HardcoreRevivalConfig.getActive().allowBows && (itemStack.getItem() instanceof BowItem)) {
            return true;
        }

        return itemStack.is(ModItemTags.ALLOW_USE_WHILE_KNOCKED_OUT);
    }

    private static boolean mayTossItemKnockedOut(ItemStack itemStack) {
        return itemStack.is(ModItemTags.ALLOW_TOSS_WHILE_KNOCKED_OUT);
    }

    private static boolean mayAttackWithItemKnockedOut(ItemStack itemStack) {
        return itemStack.is(ModItemTags.ALLOW_ATTACK_WHILE_KNOCKED_OUT);
    }

    private static boolean mayUseBlockKnockedOut(Level level, BlockPos pos, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            return false;
        }

        return level.getBlockState(pos).is(ModBlockTags.ALLOW_USE_WHILE_KNOCKED_OUT);
    }

    private static boolean mayBreakBlockKnockedOut(BlockState state) {
        return state.is(ModBlockTags.ALLOW_BREAK_WHILE_KNOCKED_OUT);
    }
}
