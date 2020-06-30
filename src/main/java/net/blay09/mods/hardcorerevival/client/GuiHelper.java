package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;

public class GuiHelper extends AbstractGui {

    private static final GuiHelper gui = new GuiHelper();

    public static void drawGradientRectW(MatrixStack matrixStack, int left, int top, int right, int bottom, int startColor, int endColor) {
        gui.func_238468_a_(matrixStack, left, top, right, bottom, startColor, endColor); // fillGradient
    }
}
