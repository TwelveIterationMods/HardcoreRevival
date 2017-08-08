package net.blay09.mods.hardcorerevival.compat;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.ModConfig;
import net.blay09.mods.hardcorerevival.PlayerKnockedOutEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PingAddon {

	private boolean isEnabled;
	private GuiButton buttonPing;
	private Object pingType;
	private Method sendPingMethod;

	public PingAddon() {
		MinecraftForge.EVENT_BUS.register(this);

		try {
			Class<?> pingTypeClass = Class.forName("dmillerw.ping.data.PingType");
			pingType = pingTypeClass.getEnumConstants()[1];
			Class<?> clientProxyClass = Class.forName("dmillerw.ping.proxy.ClientProxy");
			sendPingMethod = clientProxyClass.getMethod("sendPing", pingTypeClass);
			if (!Modifier.isPublic(sendPingMethod.getModifiers()) || !Modifier.isStatic(sendPingMethod.getModifiers())) {
				throw new Exception("sendPing is no longer accessible or not static");
			}
			isEnabled = true;
		} catch (Exception e) {
			HardcoreRevival.logger.error("Internal names for Ping have changed - disabled Ping integration.", e);
		}
	}

	@SubscribeEvent
	public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiScreen gui = event.getGui();
		if(isEnabled && gui.mc.player != null && gui.mc.player.getHealth() <= 0f && gui instanceof GuiChat) {
			buttonPing = new GuiButton(-999, gui.width / 2 - 100, gui.height / 2 + 30, I18n.format("gui.hardcorerevival.send_ping"));
			event.getButtonList().add(buttonPing);
		}
	}

	@SubscribeEvent
	public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
		if(isEnabled && event.getButton() == buttonPing) {
			Minecraft mc = Minecraft.getMinecraft();
			buttonPing.playPressSound(mc.getSoundHandler());
			if(mc.player != null) {
				sendPingBelowPlayer(mc.player);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerKnockedOut(PlayerKnockedOutEvent event) {
		if(ModConfig.autoPingOnDeath) {
			sendPingBelowPlayer(event.getPlayer());
		}
	}

	private void sendPingBelowPlayer(EntityPlayer player) {
		float prevPitch = player.rotationPitch;
		player.rotationPitch = 90; // TODO doesn't work on autoPing?
		try {
			sendPingMethod.invoke(null, pingType);
		} catch (IllegalAccessException | InvocationTargetException e) {
			HardcoreRevival.logger.error("An exception occurred when sending the ping - disabled Ping integration.", e);
			isEnabled = false;
		}
		player.rotationPitch = prevPitch;
	}
}
