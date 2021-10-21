package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;

public class GuiHelper extends GuiComponent {

    private static final GuiHelper gui = new GuiHelper();

    public static void drawGradientRectW(PoseStack poseStack, int left, int top, int right, int bottom, int startColor, int endColor) {
        gui.fillGradient(poseStack, left, top, right, bottom, startColor, endColor);
    }

    public static void renderKnockedOutTitle(PoseStack poseStack, int width) {
        poseStack.pushPose();
        poseStack.scale(2f, 2f, 2f);
        drawCenteredString(poseStack, Minecraft.getInstance().font, I18n.get("gui.hardcorerevival.knocked_out"), width / 2 / 2, 30, 16777215);
        poseStack.popPose();
    }

    public static void renderDeathTimer(PoseStack poseStack, int width, int height, boolean beingRescued) {
        Font fontRenderer = Minecraft.getInstance().font;
        if (beingRescued) {
            drawCenteredString(poseStack, fontRenderer, I18n.get("gui.hardcorerevival.being_rescued"), width / 2, height / 2 + 10, 16777215);
        } else {
            int maxTicksUntilDeath = HardcoreRevivalConfig.getActive().ticksUntilDeath;
            if (maxTicksUntilDeath > 0) {
                int deathSecondsLeft = Math.max(0, (maxTicksUntilDeath - HardcoreRevival.getClientRevivalData().getKnockoutTicksPassed()) / 20);
                drawCenteredString(poseStack, fontRenderer, I18n.get("gui.hardcorerevival.rescue_time_left", deathSecondsLeft), width / 2, height / 2 + 10, 16777215);
            } else {
                drawCenteredString(poseStack, fontRenderer, I18n.get("gui.hardcorerevival.wait_for_rescue"), width / 2, height / 2 + 10, 16777215);
            }
        }
    }
}
