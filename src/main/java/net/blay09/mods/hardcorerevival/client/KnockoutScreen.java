package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.AcceptFateMessage;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

public class KnockoutScreen extends Screen {

    private Button buttonDie;
    private int enableButtonTimer;

    protected KnockoutScreen() {
        super(new TranslationTextComponent("gui.hardcorerevival.knocked_out"));
    }

    @Override
    protected void init() {
        buttonDie = new Button(width / 2 - 100, height / 2 - 30, 200, 20, new TranslationTextComponent("gui.hardcorerevival.die", ""), it -> {
            buttonDie.playDownSound(Minecraft.getInstance().getSoundHandler());
            NetworkHandler.channel.sendToServer(new AcceptFateMessage());
        });
        buttonDie.active = false;
        addButton(buttonDie);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            enableButtonTimer += partialTicks;
            if (buttonDie != null) {
                if (enableButtonTimer >= 40) {
                    buttonDie.active = true;
                    buttonDie.setMessage(new TranslationTextComponent("gui.hardcorerevival.die", ""));
                } else if (enableButtonTimer >= 30) {
                    buttonDie.setMessage(new TranslationTextComponent("gui.hardcorerevival.die", "..."));
                } else if (enableButtonTimer >= 20) {
                    buttonDie.setMessage(new TranslationTextComponent("gui.hardcorerevival.die", ".."));
                } else if (enableButtonTimer >= 10) {
                    buttonDie.setMessage(new TranslationTextComponent("gui.hardcorerevival.die", "."));
                }
            }

            RenderSystem.pushMatrix();
            RenderSystem.scalef(2f, 2f, 2f);
            AbstractGui.drawCenteredString(matrixStack, mc.fontRenderer, I18n.format("gui.hardcorerevival.knocked_out"), width / 2 / 2, 30, 16777215);
            RenderSystem.popMatrix();

            if (!HardcoreRevivalConfig.COMMON.disableDeathTimer.get()) {
                int deathSecondsLeft = Math.max(0, (HardcoreRevivalConfig.COMMON.maxDeathTicks.get() - HardcoreRevivalClient.getKnockoutTicksPassed()) / 20);
                AbstractGui.drawCenteredString(matrixStack, mc.fontRenderer, I18n.format("gui.hardcorerevival.rescue_time_left", deathSecondsLeft), width / 2, height / 2 + 10, 16777215);
            } else {
                AbstractGui.drawCenteredString(matrixStack, mc.fontRenderer, I18n.format("gui.hardcorerevival.wait_for_rescue"), width / 2, height / 2 + 10, 16777215);
            }
        } else if (buttonDie != null) {
            buttonDie.visible = false;
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
