package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blay09.mods.hardcorerevival.network.AcceptFateMessage;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

public class KnockoutScreen extends Screen {

    private Button buttonDie;
    private float enableButtonTimer;

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

            GuiHelper.renderKnockedOutTitle(matrixStack, width);
            GuiHelper.renderDeathTimer(matrixStack, width, height);
        } else if (buttonDie != null) {
            buttonDie.visible = false;
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
