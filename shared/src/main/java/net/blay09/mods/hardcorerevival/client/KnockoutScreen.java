package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.hardcorerevival.network.AcceptFateMessage;
import net.minecraft.client.Minecraft;
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
        buttonDie = new Button(width / 2 - 100, height / 2 - 30, 200, 20, Component.translatable("gui.hardcorerevival.die", ""), it -> {
            buttonDie.playDownSound(Minecraft.getInstance().getSoundManager());
            Balm.getNetworking().sendToServer(new AcceptFateMessage());
        });
        buttonDie.active = false;
        addRenderableWidget(buttonDie);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
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

            GuiHelper.renderKnockedOutTitle(poseStack, width);
            GuiHelper.renderDeathTimer(poseStack, width, height, HardcoreRevivalClient.isBeingRescued());
        } else if (buttonDie != null) {
            buttonDie.visible = false;
        }

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
