package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.AcceptFateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class KnockoutScreen extends Screen {

    private Button buttonDie;
    private float enableButtonTimer;

    protected KnockoutScreen() {
        super(Component.translatable("gui.hardcorerevival.knocked_out"));
    }

    @Override
    protected void init() {
        buttonDie = Button.builder(Component.translatable("gui.hardcorerevival.die", ""), it -> {
            buttonDie.playDownSound(Minecraft.getInstance().getSoundManager());
            Balm.getNetworking().sendToServer(new AcceptFateMessage());
        }).pos(width / 2 - 100, height / 2 - 30).size(200, 20).build();
        buttonDie.active = false;
        buttonDie.visible = HardcoreRevivalConfig.getActive().allowAcceptingFate;
        addRenderableWidget(buttonDie);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
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
        } else if (buttonDie != null) {
            buttonDie.visible = false;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
