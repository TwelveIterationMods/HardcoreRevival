package net.blay09.mods.hardcorerevival.handler;

import com.mojang.brigadier.ParseResults;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class KnockoutRestrictionHandler {

    public static void initialize() {
        BlockCallback.Use.EVENT.register(EventPhases.HIGHEST, KnockoutRestrictionHandler::onUseBlock);
        ItemCallback.Use.EVENT.register(EventPhases.HIGHEST, KnockoutRestrictionHandler::onUseItem);
        PlayerCallback.Attack.Before.EVENT.register(EventPhases.HIGHEST, KnockoutRestrictionHandler::onAttack);
        BlockCallback.DigSpeed.EVENT.register(EventPhases.HIGHEST, KnockoutRestrictionHandler::onDigSpeed);
        LivingEntityCallback.Heal.Before.EVENT.register(KnockoutRestrictionHandler::onHeal);
        CommandCallback.Before.EVENT.register(KnockoutRestrictionHandler::onCommand);
    }

    public static boolean onCommand(ParseResults<CommandSourceStack> parseResults) {
        if (HardcoreRevivalConfig.getActive().allowCommands) {
            return true;
        }

        final var player = parseResults.getContext().getSource().getPlayer();
        if (player == null) {
            return true;
        }

        final var server = player.level().getServer();
        if (server.isSingleplayer()) {
            return true;
        }

        if (server.getPlayerList().isOp(new NameAndId(player.getGameProfile()))) {
            return true;
        }

        if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            player.sendSystemMessage(Component.translatable("commands.disabled_when_knocked_out").withStyle(ChatFormatting.RED));
            return false;
        }

        return true;
    }

    public static float onHeal(LivingEntity entity, float amount) {
        if (entity instanceof Player player) {
            if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
                return 0f;
            }
        }

        return amount;
    }

    public static float onDigSpeed(BlockGetter blockGetter, BlockPos pos, BlockState state, @Nullable Player player, float speed) {
        if (player != null && PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            if (!mayBreakBlockKnockedOut(state)) {
                return 0f;
            }
        }
        return speed;
    }

    public static InteractionEventResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            ItemStack itemStack = player.getItemInHand(hand);
            final var pos = hitResult.getBlockPos();
            if (!mayUseItemKnockedOut(itemStack) && !mayUseBlockKnockedOut(level, pos, itemStack)) {
                return InteractionEventResult.FAIL;
            }
        }

        return InteractionEventResult.DEFAULT;
    }

    public static InteractionEventResult onUseItem(Player player, Level level, InteractionHand hand) {
        if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (!mayUseItemKnockedOut(itemStack)) {
                return InteractionEventResult.FAIL;
            }
        }

        return InteractionEventResult.DEFAULT;
    }

    public static boolean onAttack(@Nullable Player player, Entity entity) {
        if (player != null && PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            final var itemStack = player.getMainHandItem();
            if (HardcoreRevivalConfig.getActive().allowUnarmedMelee && itemStack.isEmpty()) {
                return true;
            }

            return mayAttackWithItemKnockedOut(itemStack);
        }

        return true;
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
