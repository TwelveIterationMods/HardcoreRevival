package net.blay09.mods.hardcorerevival.client;

import net.minecraft.client.gui.Gui;

public class GuiHelper extends Gui {

	private static final GuiHelper gui = new GuiHelper();

	public static void drawGradientRectW(int left, int top, int right, int bottom, int startColor, int endColor) {
		gui.drawGradientRect(left, top, right, bottom, startColor, endColor);
	}
}
