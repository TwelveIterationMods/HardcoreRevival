package net.blay09.mods.hardcorerevival.handler;

import com.mojang.brigadier.ParseResults;
import net.blay09.mods.balm.platform.event.EventHandling;
import net.blay09.mods.balm.platform.event.EventPhases;
import net.blay09.mods.balm.platform.event.callback.*;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.blay09.mods.hardcorerevival.tag.ModBlockTags;
import net.blay09.mods.hardcorerevival.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class KnockoutRestrictionHandler {

    public static void initialize() {
        BlockCallback.Use.EVENT.register(EventPhases.HIGHEST, KnockoutRestrictionHandler::onUseBlock);
        ItemCallback.Use.EVENT.register(EventPhases.HIGHEST, KnockoutRestrictionHandler::onUseItem);
        PlayerCallback.Attack.EVENT.register(EventPhases.HIGHEST, KnockoutRestrictionHandler::onAttack);
        BlockCallback.DigSpeed.EVENT.register(EventPhases.HIGHEST, KnockoutRestrictionHandler::onDigSpeed);
        LivingEntityCallback.Heal.EVENT.register(KnockoutRestrictionHandler::onHeal);
        CommandCallback.EVENT.register(KnockoutRestrictionHandler::onCommand);
    }

    public static EventHandling onCommand(ParseResults<CommandSourceStack> parseResults) {
        if (HardcoreRevivalConfig.getActive().allowCommands) {
            return EventHandling.RESUME;
        }

        final var player = parseResults.getContext().getSource().getPlayer();
        if (player == null) {
            return EventHandling.RESUME;
        }

        final var server = player.level().getServer();
        if (server != null && server.isSingleplayer()) {
            return EventHandling.RESUME;
        }

        if (server != null && server.getPlayerList().isOp(new NameAndId(player.getGameProfile()))) {
            return EventHandling.RESUME;
        }

        if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            player.sendSystemMessage(Component.translatable("commands.disabled_when_knocked_out").withStyle(ChatFormatting.RED));
            return EventHandling.CANCEL;
        }

        return EventHandling.RESUME;
    }

    public static float onHeal(LivingEntity entity, float amount) {
        if (entity instanceof Player player) {
            if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
                return 0f;
            }
        }

        return amount;
    }

    public static float onDigSpeed(BlockGetter blockGetter, BlockPos pos, BlockState state, Player player, float speed) {
        if (player != null && PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            if (!mayBreakBlockKnockedOut(state)) {
                return 0f;
            }
        }
        return speed;
    }

    public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            ItemStack itemStack = player.getItemInHand(hand);
            final var pos = hitResult.getBlockPos();
            if (!mayUseItemKnockedOut(itemStack) && !mayUseBlockKnockedOut(level, pos, itemStack)) {
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    public static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
        if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (!mayUseItemKnockedOut(itemStack)) {
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    public static EventHandling onAttack(Player player, Entity entity) {
        if (player != null && PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            final var itemStack = player.getMainHandItem();
            if (HardcoreRevivalConfig.getActive().allowUnarmedMelee && itemStack.isEmpty()) {
                return EventHandling.RESUME;
            }

            if (!mayAttackWithItemKnockedOut(itemStack)) {
                return EventHandling.CANCEL;
            }
        }

        return EventHandling.RESUME;
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
