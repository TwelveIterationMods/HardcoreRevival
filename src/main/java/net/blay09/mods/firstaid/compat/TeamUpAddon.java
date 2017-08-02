package net.blay09.mods.firstaid.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 1) isSneaking() will always be false when the player is dead, so check for the keybind instead
 * 2) automatically unsneak the player when they die, so the chat won't always open with /team prefix
 */
public class TeamUpAddon {

	public TeamUpAddon() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onOpenGui(GuiOpenEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player != null && mc.player.getHealth() <= 0f) {
			mc.player.setSneaking(false);
			if (event.getGui() instanceof GuiChat && mc.gameSettings.keyBindSneak.isKeyDown()) {
				event.setGui(new GuiChat("/team "));
			}
		}
	}

}
