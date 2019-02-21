package net.blay09.mods.hardcorerevival.client;

import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.MessageDie;
import net.blay09.mods.hardcorerevival.network.MessageRevival;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class HardcoreRevivalClient {

    private boolean isKnockedOut;
    private boolean acceptedDeath;
    private int deathTime;

    // GUI things
    private float enableButtonTimer;
    private GuiButton buttonDie;
    private double prevChatHeight = -1;

    // Rescuing
    private boolean isRescuing;
    private int targetEntity = -1;
    private float targetProgress;

    @SubscribeEvent
    public void onOpenGui(GuiOpenEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (event.getGui() instanceof GuiGameOver && isKnockedOut && !acceptedDeath) { // Minor hack: isKnockedOut is always set AFTER the game over screen pops up, so we can abuse that here
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
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && isKnockedOut && mc.currentScreen != null) {
            prevChatHeight = mc.gameSettings.chatHeightFocused;
            mc.gameSettings.chatHeightFocused = 0.1f;
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.PORTAL) {
            Minecraft mc = Minecraft.getInstance();
            if (isKnockedOut) {
                GlStateManager.pushMatrix();
                GlStateManager.translatef(0, 0, -300);
                GuiHelper.drawGradientRectW(0, 0, mc.mainWindow.getWidth(), mc.mainWindow.getHeight(), 0x60500000, 0x90FF0000);
                GlStateManager.popMatrix();
                if (mc.currentScreen == null) {
                    mc.fontRenderer.drawStringWithShadow(I18n.format("gui.hardcorerevival.open_death_screen", mc.gameSettings.keyBindChat.getTranslationKey()), 5, 5, 0xFFFFFFFF);
                    mc.fontRenderer.drawString(I18n.format("gui.hardcorerevival.rescue_time_left", Math.max(0, (HardcoreRevivalConfig.COMMON.maxDeathTicks.get() - deathTime) / 20)), 5, 7 + mc.fontRenderer.FONT_HEIGHT, 16777215);
                    mc.getTextureManager().bindTexture(Gui.ICONS);
                }
            } else {
                if (targetEntity != -1) {
                    Entity entity = mc.world.getEntityByID(targetEntity);
                    if (entity instanceof EntityPlayer) {
                        String s = I18n.format("gui.hardcorerevival.rescuing", entity.getDisplayName()); // TODO getUnformattedString necessary?
                        if (targetProgress >= 0.75f) {
                            s += " ...";
                        } else if (targetProgress >= 0.5f) {
                            s += " ..";
                        } else if (targetProgress >= 0.25f) {
                            s += " .";
                        }
                        mc.fontRenderer.drawString(s, mc.mainWindow.getScaledWidth() / 2f - mc.fontRenderer.getStringWidth(s) / 2f, mc.mainWindow.getScaledHeight() / 2f + 30, 0xFFFFFFFF);
                        mc.getTextureManager().bindTexture(Gui.ICONS);
                    }
                }
            }
        } else if (event.getType() == RenderGameOverlayEvent.ElementType.CHAT) {
            if (prevChatHeight != -1f) {
                Minecraft.getInstance().gameSettings.chatHeightFocused = prevChatHeight;
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        // Suppress item drops when knocked out
        if (mc.player != null && mc.player.getHealth() <= 0f) {
            //noinspection StatementWithEmptyBody
            while (mc.gameSettings.keyBindDrop.isPressed()) ;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (mc.player.getHealth() <= 0f) {
                    if (!isKnockedOut && !acceptedDeath) {
                        // The player is now knocked out
                        deathTime = 0;
                        isKnockedOut = true;
                        mc.displayGuiScreen(new GuiChat());
//						if(mc.currentScreen != null) {
//							 Re-initialize the GUI to fire button hooks
//							ScaledResolution resolution = new ScaledResolution(mc);
//							mc.currentScreen.setWorldAndResolution(mc, resolution.getScaledWidth(), resolution.getScaledHeight());
//						}
                    }
                    // Prevent deathTime from removing the entity from the world
                    if (mc.player.deathTime == 19) {
                        mc.player.deathTime = 18;
                    }
                    // Instead, increase our own counter
                    deathTime++;
                } else {
                    isKnockedOut = false;
                    acceptedDeath = false;
                    deathTime = 0;

                    // If right mouse is held down, send revival packet
                    if (mc.mouseHelper.isRightDown()) {
                        if (!isRescuing) {
                            NetworkHandler.channel.sendToServer(new MessageRevival(true));
                            isRescuing = true;
                        }
                    } else {
                        if (isRescuing) {
                            NetworkHandler.channel.sendToServer(new MessageRevival(false));
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
            buttonDie = new GuiButton(-2, gui.width / 2 - 100, gui.height / 2 - 30, I18n.format("gui.hardcorerevival.die")) {
                @Override
                public void onClick(double mouseX, double mouseY) {
                    playPressSound(Minecraft.getInstance().getSoundHandler());
                    NetworkHandler.channel.sendToServer(new MessageDie());
                    acceptedDeath = true;
                }
            };
            buttonDie.enabled = false;
            event.getButtonList().add(buttonDie);
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        GuiScreen gui = event.getGui();
        Minecraft mc = gui.mc;
        if (mc.player != null && isKnockedOut && gui instanceof GuiChat) {
            enableButtonTimer += event.getRenderPartialTicks();
            if (buttonDie != null) {
                if (enableButtonTimer >= 40) {
                    buttonDie.enabled = true;
                    buttonDie.displayString = I18n.format("gui.hardcorerevival.die");
                } else if (enableButtonTimer >= 30) {
                    buttonDie.displayString = "... " + I18n.format("gui.hardcorerevival.die") + " ...";
                } else if (enableButtonTimer >= 20) {
                    buttonDie.displayString = ".. " + I18n.format("gui.hardcorerevival.die") + " ..";
                } else if (enableButtonTimer >= 10) {
                    buttonDie.displayString = ". " + I18n.format("gui.hardcorerevival.die") + " .";
                }
            }

            GlStateManager.pushMatrix();
            GlStateManager.scalef(2f, 2f, 2f);
            gui.drawCenteredString(mc.fontRenderer, I18n.format("gui.hardcorerevival.knocked_out"), gui.width / 2 / 2, 30, 16777215);
            GlStateManager.popMatrix();

            gui.drawCenteredString(mc.fontRenderer, I18n.format("gui.hardcorerevival.rescue_time_left", Math.max(0, (HardcoreRevivalConfig.COMMON.maxDeathTicks.get() - deathTime) / 20)), gui.width / 2, gui.height / 2 + 10, 16777215);
        } else if (buttonDie != null) {
            buttonDie.visible = false;
        }
    }

    public void setDeathTime(int deathTime) {
        this.deathTime = deathTime;
    }

    public void setRevivalProgress(int entityId, float progress) {
        targetEntity = entityId;
        targetProgress = progress;
    }

    public void onFinalDeath() {
        isKnockedOut = true;
        acceptedDeath = true;

        // Manually mark the player as dead to have JourneyMap create a death point
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (HardcoreRevivalConfig.COMMON.glowOnDeath.get()) {
                mc.player.setGlowing(false);
                mc.player.setFlag(6, false); // glowing flag
            }
            mc.player.removed = true;
        }
    }
}
