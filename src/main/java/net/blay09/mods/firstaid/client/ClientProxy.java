package net.blay09.mods.firstaid.client;

import net.blay09.mods.firstaid.CommonProxy;
import net.blay09.mods.firstaid.ModConfig;
import net.blay09.mods.firstaid.network.MessageDie;
import net.blay09.mods.firstaid.network.MessageFirstAid;
import net.blay09.mods.firstaid.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Mouse;

public class ClientProxy extends CommonProxy {

	private float enableButtonTimer;
	private GuiButton buttonDie;
	private DamageSource damageSource;
	private boolean isKnockedOut;
	private int deathTime;
	private boolean hasGivenUp;
	private boolean isRescuing;
	private float prevChatHeight = -1f;

	private int targetEntity = -1;
	private float targetProgress;

	@SubscribeEvent
	public void onOpenGui(GuiOpenEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player != null) {
			if (event.getGui() instanceof GuiGameOver && deathTime < ModConfig.maxDeathTicks && !hasGivenUp) {
				event.setGui(null);
			} else if (isKnockedOut && event.getGui() instanceof GuiInventory) {
				event.setGui(null);
			}
		}
	}

	@SubscribeEvent
	public void onFov(FOVUpdateEvent event) {
		if (isKnockedOut) {
			event.setNewfov(0.5f);
		}
	}

	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Chat event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player != null && isKnockedOut && mc.currentScreen != null) {
			prevChatHeight = mc.gameSettings.chatHeightFocused;
			mc.gameSettings.chatHeightFocused = 0.1f;
		}
	}

	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() == RenderGameOverlayEvent.ElementType.PORTAL) {
			Minecraft mc = Minecraft.getMinecraft();
			if (isKnockedOut) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, -300);
				GuiHelper.drawGradientRectW(0, 0, mc.displayWidth, mc.displayHeight, 0x60500000, 0x90FF0000);
				GlStateManager.popMatrix();
				if (mc.currentScreen == null) {
					mc.fontRenderer.drawStringWithShadow(I18n.format("gui.firstaid.open_death_screen"), 5, 5, 0xFFFFFFFF);
					mc.fontRenderer.drawString(I18n.format("gui.firstaid.rescue_time_left", (ModConfig.maxDeathTicks - deathTime) / 20), 5, 7 + mc.fontRenderer.FONT_HEIGHT, 16777215);
					mc.getTextureManager().bindTexture(Gui.ICONS);
				}
			} else {
				if(targetEntity != -1) {
					Entity entity = mc.world.getEntityByID(targetEntity);
					if (entity instanceof EntityPlayer) {
						String s = I18n.format("gui.firstaid.rescuing", ((EntityPlayer) entity).getDisplayNameString());
						if(targetProgress >= 0.75f) {
							s += " ...";
						} else if(targetProgress >= 0.5f) {
							s += " ..";
						} else if(targetProgress >= 0.25f) {
							s += " .";
						}
						mc.fontRenderer.drawString(s, event.getResolution().getScaledWidth() / 2 - mc.fontRenderer.getStringWidth(s) / 2, event.getResolution().getScaledHeight() / 2 + 30, 0xFFFFFFFF);
						mc.getTextureManager().bindTexture(Gui.ICONS);
					}
				}
			}
		} else if(event.getType() == RenderGameOverlayEvent.ElementType.CHAT) {
			if(prevChatHeight != -1f) {
				Minecraft.getMinecraft().gameSettings.chatHeightFocused = prevChatHeight;
			}
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.side == Side.CLIENT/* && event.phase == TickEvent.Phase.START*/) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.player != null) {
				if(mc.player.getHealth() <= 0f) {
					if(!isKnockedOut) {
						deathTime = 0;
						isKnockedOut = true;
						if(mc.currentScreen != null) {
							ScaledResolution resolution = new ScaledResolution(mc);
							mc.currentScreen.setWorldAndResolution(mc, resolution.getScaledWidth(), resolution.getScaledHeight());
						}
					}
					// Prevent deathTime from removing the entity from the world
					if(mc.player.deathTime == 19) {
						mc.player.deathTime = 18;
					}
					deathTime++;
//					if ((deathTime >= ModConfig.maxDeathTicks || hasGivenUp) && (mc.currentScreen == null || mc.currentScreen instanceof GuiChat)) {
//						mc.displayGuiScreen(new GuiGameOver(new TextComponentTranslation("gui.firstaid.no_rescue")));
//					}
				} else {
					isKnockedOut = false;
					hasGivenUp = false;
					deathTime = 0;

					// If right mouse is held down, send first aid packet
					if(Mouse.isButtonDown(1)) {
						if(!isRescuing) {
							NetworkHandler.instance.sendToServer(new MessageFirstAid(true));
							isRescuing = true;
						}
					} else {
						if(isRescuing) {
							NetworkHandler.instance.sendToServer(new MessageFirstAid(false));
							isRescuing = false;
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
		Minecraft mc = event.getGui().mc;
		if (mc.player != null && isKnockedOut && event.getGui() instanceof GuiChat) {
			GuiScreen gui = event.getGui();
			enableButtonTimer = 0;
			buttonDie = new GuiButton(-2, gui.width / 2 - 100, gui.height / 2 - 30, I18n.format("gui.firstaid.die"));
			buttonDie.enabled = false;
			event.getButtonList().add(buttonDie);
		}
	}

	@SubscribeEvent
	public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
		if(event.getButton() == buttonDie) {
			event.getButton().playPressSound(Minecraft.getMinecraft().getSoundHandler());
			hasGivenUp = true;
			NetworkHandler.instance.sendToServer(new MessageDie());
		}
	}

	@SubscribeEvent
	public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
		GuiScreen gui = event.getGui();
		Minecraft mc = gui.mc;
		if (mc.player != null && isKnockedOut && gui instanceof GuiChat) {
			enableButtonTimer += event.getRenderPartialTicks();
			if(buttonDie != null) {
				if (enableButtonTimer >= 40) {
					buttonDie.enabled = true;
					buttonDie.displayString = I18n.format("gui.firstaid.die");
				} else if (enableButtonTimer >= 30) {
					buttonDie.displayString = "... " + I18n.format("gui.firstaid.die") + " ...";
				} else if (enableButtonTimer >= 20) {
					buttonDie.displayString = ".. " + I18n.format("gui.firstaid.die") + " ..";
				} else if (enableButtonTimer >= 10) {
					buttonDie.displayString = ". " + I18n.format("gui.firstaid.die") + " .";
				}
			}

			GlStateManager.pushMatrix();
			GlStateManager.scale(2f, 2f, 2f);
			gui.drawCenteredString(mc.fontRenderer, I18n.format("gui.firstaid.knocked_out"), gui.width / 2 / 2, 30, 16777215);
			GlStateManager.popMatrix();

			gui.drawCenteredString(mc.fontRenderer, I18n.format("gui.firstaid.rescue_time_left", (ModConfig.maxDeathTicks - deathTime) / 20), gui.width / 2, gui.height / 2 + 10, 16777215);
		} else if(buttonDie != null) {
			buttonDie.visible = false;
		}
	}

	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		if(event.getEntityLiving() instanceof EntityPlayerSP) {
			if(false || event.getSource() == DamageSource.OUT_OF_WORLD) { // TODO testing code
				hasGivenUp = true;
			} else {
				isKnockedOut = true;
				damageSource = event.getSource();
				deathTime = 0;
			}
		}
	}

	@Override
	public void receiveDeathTime(int deathTime) {
		this.deathTime = deathTime;
	}

	@Override
	public void receiveFirstAidProgress(int entityId, float progress) {
		targetEntity = entityId;
		targetProgress = progress;
	}
}
