package net.blay09.mods.hardcorerevival.client;

import net.minecraft.client.gui.AbstractGui;

public class GuiHelper extends AbstractGui {

	private static final GuiHelper gui = new GuiHelper();

	public static void drawGradientRectW(int left, int top, int right, int bottom, int startColor, int endColor) {
		gui.fillGradient(left, top, right, bottom, startColor, endColor);
	}
}
