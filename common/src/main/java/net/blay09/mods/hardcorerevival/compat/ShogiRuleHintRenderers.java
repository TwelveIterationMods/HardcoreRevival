package net.blay09.mods.hardcorerevival.compat;

import net.blay09.mods.hardcorerevival.client.hint.HintOverlay;
import net.blay09.mods.shogi.common.effect.cost.ExperienceLevelCostInformation;
import net.blay09.mods.shogi.common.effect.cost.ExperiencePointsCostInformation;
import net.blay09.mods.shogi.common.effect.cost.ItemCostInformation;
import net.blay09.mods.shogi.common.effect.failure.FailureInformation;
import net.blay09.mods.shogi.common.effect.failure.RefusalInformation;
import net.blay09.mods.shogi.common.effect.server.cooldown.CooldownInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static net.blay09.mods.hardcorerevival.client.hint.HintOverlay.outcomeStyled;

@SuppressWarnings("unused")
public class ShogiRuleHintRenderers {
    public ShogiRuleHintRenderers() {
        HintOverlay.registerHintRenderer(RefusalInformation.class, (guiGraphics, x, y, success, info) -> HintOverlay.renderText(guiGraphics, info.message().copy().withStyle(ChatFormatting.RED), x, y));
        HintOverlay.registerHintRenderer(FailureInformation.class, (guiGraphics, x, y, success, info) -> HintOverlay.renderText(guiGraphics, info.message().copy().withStyle(ChatFormatting.RED), x, y));
        HintOverlay.registerHintRenderer(ExperienceLevelCostInformation.class, (guiGraphics, x, y, success, info) -> HintOverlay.renderText(guiGraphics,
                outcomeStyled(Component.translatable("gui.hardcorerevival.xp_levels", info.required()), info.available() >= info.required()), x, y));
        HintOverlay.registerHintRenderer(ExperiencePointsCostInformation.class, (guiGraphics, x, y, success, info) -> HintOverlay.renderText(guiGraphics,
                outcomeStyled(Component.translatable("gui.hardcorerevival.xp_points", info.required()), info.available() >= info.required()), x, y));
        HintOverlay.registerHintRenderer(ItemCostInformation.class, (guiGraphics, x, y, success, info) -> {
            final var itemStack = info.item().stream()
                    .findFirst()
                    .map(it -> new ItemStack(it.value()))
                    .orElse(ItemStack.EMPTY);
            return HintOverlay.renderItem(guiGraphics, itemStack, info.required(), info.available() >= info.required(), x, y);
        });
        HintOverlay.registerHintRenderer(CooldownInformation.class, (guiGraphics, x, y, success, info) -> HintOverlay.renderText(guiGraphics,
                Component.translatable("gui.hardcorerevival.cooldown", (int) Math.ceil(info.remainingTicks() / 20f)).withStyle(ChatFormatting.RED),
                x, y));
    }
}
