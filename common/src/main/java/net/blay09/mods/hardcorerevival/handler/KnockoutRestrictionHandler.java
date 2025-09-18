package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.*;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.blay09.mods.hardcorerevival.tag.ModBlockTags;
import net.blay09.mods.hardcorerevival.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class KnockoutRestrictionHandler {

    public static void initialize() {
        Balm.getEvents().onEvent(UseBlockEvent.class, KnockoutRestrictionHandler::onUseBlock, EventPriority.Highest);
        Balm.getEvents().onEvent(UseItemEvent.class, KnockoutRestrictionHandler::onUseItem, EventPriority.Highest);
        Balm.getEvents().onEvent(PlayerAttackEvent.class, KnockoutRestrictionHandler::onAttack, EventPriority.Highest);
        Balm.getEvents().onEvent(DigSpeedEvent.class, KnockoutRestrictionHandler::onDigSpeed, EventPriority.Highest);
        Balm.getEvents().onEvent(LivingHealEvent.class, KnockoutRestrictionHandler::onHeal);
        Balm.getEvents().onEvent(CommandEvent.class, KnockoutRestrictionHandler::onCommand);
    }

    public static void onCommand(CommandEvent event) {
        if (HardcoreRevivalConfig.getActive().allowCommands) {
            return;
        }

        final var player = event.getParseResults().getContext().getSource().getPlayer();
        if (player == null) {
            return;
        }

        final var server = player.level().getServer();
        if (server != null && server.isSingleplayer()) {
            return;
        }

        if (server != null && server.getPlayerList().isOp(new NameAndId(player.getGameProfile()))) {
            return;
        }

        if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            player.sendSystemMessage(Component.translatable("commands.disabled_when_knocked_out").withStyle(ChatFormatting.RED));
            event.setCanceled(true);
        }
    }

    public static void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
                event.setCanceled(false);
            }
        }
    }

    public static void onDigSpeed(DigSpeedEvent event) {
        Player player = event.getPlayer();
        if (player != null && PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            if (!mayBreakBlockKnockedOut(event.getState())) {
                event.setSpeedOverride(0f);
                event.setCanceled(true);
            }
        }
    }

    public static void onUseBlock(UseBlockEvent event) {
        Player player = event.getPlayer();
        if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            ItemStack itemStack = player.getItemInHand(event.getHand());
            final var level = event.getLevel();
            final var pos = event.getHitResult().getBlockPos();
            if (!mayUseItemKnockedOut(itemStack) && !mayUseBlockKnockedOut(level, pos, itemStack)) {
                event.setCanceled(true);
            }
        }
    }

    public static void onUseItem(UseItemEvent event) {
        final var player = event.getPlayer();
        if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            ItemStack itemStack = player.getItemInHand(event.getHand());
            if (!mayUseItemKnockedOut(itemStack)) {
                event.setCanceled(true);
            }
        }
    }

    public static void onAttack(PlayerAttackEvent event) {
        Player player = event.getPlayer();
        if (player != null && PlayerHardcoreRevivalManager.isKnockedOut(player)) {
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

    public static boolean mayTossItemKnockedOut(ItemStack itemStack) {
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
