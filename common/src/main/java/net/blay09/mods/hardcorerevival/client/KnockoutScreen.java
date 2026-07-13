package net.blay09.mods.hardcorerevival.client;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.AcceptFateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class KnockoutScreen extends Screen {

    private @Nullable Button buttonDie;
    private float enableButtonTimer;

    protected KnockoutScreen() {
        super(Component.translatable("gui.hardcorerevival.knocked_out"));
    }

    @Override
    protected void init() {
        buttonDie = Button.builder(Component.translatable("gui.hardcorerevival.die", ""), it -> {
            it.playDownSound(Minecraft.getInstance().getSoundManager());
            Balm.networking().sendToServer(AcceptFateMessage.INSTANCE);
        }).pos(width / 2 - 100, height / 2 - 30).size(200, 20).build();
        buttonDie.active = false;
        buttonDie.visible = HardcoreRevivalConfig.getActive().allowAcceptingFate;
        addRenderableWidget(buttonDie);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

        if (buttonDie != null) {
            final var client = Minecraft.getInstance();
            if (client.player != null) {
                enableButtonTimer += partialTicks;
                if (enableButtonTimer >= 40) {
                    buttonDie.active = true;
                    buttonDie.setMessage(Component.translatable("gui.hardcorerevival.die", ""));
                } else if (enableButtonTimer >= 30) {
                    buttonDie.setMessage(Component.translatable("gui.hardcorerevival.die", "..."));
                } else if (enableButtonTimer >= 20) {
                    buttonDie.setMessage(Component.translatable("gui.hardcorerevival.die", ".."));
                } else if (enableButtonTimer >= 10) {
                    buttonDie.setMessage(Component.translatable("gui.hardcorerevival.die", "."));
                }

                GuiHelper.renderKnockedOutTitle(guiGraphics, width);
                GuiHelper.renderDeathTimer(guiGraphics, width, height, HardcoreRevivalClient.isBeingRescued());

                final var closeDeathScreenText = Component.translatable("gui.hardcorerevival.close_death_screen");
                guiGraphics.centeredText(client.font, closeDeathScreenText, width / 2, height / 2 + 25, 0xFFFFFFFF);
            } else {
                buttonDie.visible = false;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
