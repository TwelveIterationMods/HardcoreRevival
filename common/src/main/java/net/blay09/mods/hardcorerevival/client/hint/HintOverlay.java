package net.blay09.mods.hardcorerevival.client.hint;

import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HintOverlay {
    private static final Map<Class<?>, HintRenderer<?>> HINT_RENDERERS = new LinkedHashMap<>();
    private static Either<?, ?> activeHint = Either.left(true);
    private static int activeHintTicks;

    static {
        registerDefaultHintRenderers();
    }

    public static <T> void registerHintRenderer(Class<T> hintType, HintRenderer<? super T> renderer) {
        HINT_RENDERERS.put(hintType, renderer);
    }

    public static void setActiveHint(Either<?, ?> hint) {
        activeHint = hint;
        activeHintTicks = 80;
    }

    public static void tick() {
        if (activeHintTicks > 0) {
            activeHintTicks--;
            if (activeHintTicks == 0) {
                activeHint = Either.left(true);
            }
        }
    }

    public static void renderHint(GuiGraphicsExtractor guiGraphics) {
        if (hasActiveHint()) {
            renderHint(guiGraphics, activeHint);
        }
    }

    private static boolean hasActiveHint() {
        return activeHintTicks > 0;
    }

    private static void registerDefaultHintRenderers() {
        registerHintRenderer(List.class, (guiGraphics, x, y, success, hint) -> {
            int height = 0;
            for (final var item : hint) {
                height += renderHint(guiGraphics, item, success, x, y + height);
            }
            return height;
        });
        registerHintRenderer(Component.class, (guiGraphics, x, y, success, component) -> renderText(guiGraphics, outcomeStyled(component, success), x, y));
        registerHintRenderer(String.class, (guiGraphics, x, y, success, message) -> renderText(guiGraphics, outcomeStyled(Component.literal(message), success), x, y));
        registerHintRenderer(Throwable.class, (guiGraphics, x, y, success, throwable) -> {
            final var message = throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getSimpleName();
            return renderText(guiGraphics, outcomeStyled(Component.literal(message), false), x, y);
        });
    }

    @SuppressWarnings("unchecked")
    private static HintRenderer<Object> findHintRenderer(Object hint) {
        if (hint == null) {
            return null;
        }

        for (final var entry : HINT_RENDERERS.entrySet()) {
            if (entry.getKey().isInstance(hint)) {
                return (HintRenderer<Object>) entry.getValue();
            }
        }

        return null;
    }

    public static void renderHoldToRescue(GuiGraphicsExtractor guiGraphics) {
        final var mc = Minecraft.getInstance();
        final var rescueKeyText = mc.options.keyUse.getTranslatedKeyMessage();
        final var textComponent = Component.translatable("gui.hardcorerevival.hold_to_rescue", rescueKeyText);
        guiGraphics.text(mc.font,
                textComponent,
                guiGraphics.guiWidth() / 2 - mc.font.width(textComponent) / 2,
                guiGraphics.guiHeight() / 2 + 30,
                0xFFFFFFFF,
                true);
    }

    private static int renderHint(GuiGraphicsExtractor guiGraphics, Either<?, ?> hint) {
        return renderHint(guiGraphics, Either.unwrap(hint), hint.left().isPresent(), guiGraphics.guiWidth() / 2, guiGraphics.guiHeight() / 2 + 46);
    }

    private static int renderHint(GuiGraphicsExtractor guiGraphics, Object hint, boolean canRevive, int x, int y) {
        final var renderer = findHintRenderer(hint);
        return renderer != null ? renderer.render(guiGraphics, x, y, canRevive, hint) : 0;
    }

    public static int renderText(GuiGraphicsExtractor guiGraphics, Component component, int x, int y) {
        final var font = Minecraft.getInstance().font;
        final var width = font.width(component);
        guiGraphics.text(font, component, x - width / 2, y, 0xFFFFFFFF, true);
        return font.lineHeight;
    }

    public static int renderItem(GuiGraphicsExtractor guiGraphics, ItemStack itemStack, int count, boolean canAfford, int x, int y) {
        final var font = Minecraft.getInstance().font;
        final var component = Component.translatable("gui.hardcorerevival.item", count, itemStack.getHoverName())
                .withStyle(canAfford ? ChatFormatting.GREEN : ChatFormatting.RED);
        final var width = 20 + font.width(component);
        final var left = x - width / 2;
        guiGraphics.item(itemStack, left, y);
        guiGraphics.text(font, component, left + 20, y + 4, 0xFFFFFFFF, true);
        return 20;
    }

    public static Component outcomeStyled(Component component, boolean success) {
        return component.copy().withStyle(success ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    public interface HintRenderer<T> {
        int render(GuiGraphicsExtractor guiGraphics, int x, int y, boolean success, T hint);
    }
}
