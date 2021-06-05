package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

public class GuiHelper extends AbstractGui {

    private static final GuiHelper gui = new GuiHelper();

    public static void drawGradientRectW(MatrixStack matrixStack, int left, int top, int right, int bottom, int startColor, int endColor) {
        gui.fillGradient(matrixStack, left, top, right, bottom, startColor, endColor);
    }

    public static void renderKnockedOutTitle(MatrixStack matrixStack, int width) {
        RenderSystem.pushMatrix();
        RenderSystem.scalef(2f, 2f, 2f);
        AbstractGui.drawCenteredString(matrixStack, Minecraft.getInstance().fontRenderer, I18n.format("gui.hardcorerevival.knocked_out"), width / 2 / 2, 30, 16777215);
        RenderSystem.popMatrix();
    }

    public static void renderDeathTimer(MatrixStack matrixStack, int width, int height, boolean beingRescued) {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        if (beingRescued) {
            AbstractGui.drawCenteredString(matrixStack, fontRenderer, I18n.format("gui.hardcorerevival.being_rescued"), width / 2, height / 2 + 10, 16777215);
        } else {
            int maxTicksUntilDeath = HardcoreRevivalConfig.getActive().getTicksUntilDeath();
            if (maxTicksUntilDeath > 0) {
                int deathSecondsLeft = Math.max(0, (maxTicksUntilDeath - HardcoreRevival.getClientRevivalData().getKnockoutTicksPassed()) / 20);
                AbstractGui.drawCenteredString(matrixStack, fontRenderer, I18n.format("gui.hardcorerevival.rescue_time_left", deathSecondsLeft), width / 2, height / 2 + 10, 16777215);
            } else {
                AbstractGui.drawCenteredString(matrixStack, fontRenderer, I18n.format("gui.hardcorerevival.wait_for_rescue"), width / 2, height / 2 + 10, 16777215);
            }
        }
    }
}
