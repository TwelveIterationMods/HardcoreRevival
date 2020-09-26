package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.MessageDie;
import net.blay09.mods.hardcorerevival.network.MessageRevival;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HardcoreRevivalClient {

    private boolean isKnockedOut;
    private boolean acceptedDeath;
    private int deathTime;

    // GUI things
    private float enableButtonTimer;
    private Button buttonDie;
    private double prevChatHeight = -1;

    // Rescuing
    private boolean isRescuing;
    private int targetEntity = -1;
    private float targetProgress;

    @SubscribeEvent
    public void onOpenGui(GuiOpenEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (event.getGui() instanceof DeathScreen && isKnockedOut && !acceptedDeath) { // Minor hack: isKnockedOut is always set AFTER the game over screen pops up, so we can abuse that here
                event.setGui(null);
            } else if (isKnockedOut && event.getGui() instanceof InventoryScreen) {
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
                RenderSystem.pushMatrix();
                RenderSystem.translatef(0, 0, -300);
                GuiHelper.drawGradientRectW(event.getMatrixStack(), 0, 0, mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight(), 0x60500000, 0x90FF0000);
                RenderSystem.popMatrix();
                if (mc.currentScreen == null) {
                    ITextComponent openDeathScreenKey = mc.gameSettings.keyBindChat.func_238171_j_(); // getDisplayName()
                    final TranslationTextComponent openDeathScreenText = new TranslationTextComponent("gui.hardcorerevival.open_death_screen", openDeathScreenKey);
                    mc.fontRenderer.func_238407_a_(event.getMatrixStack(), openDeathScreenText.func_241878_f(), 5, 5, 0xFFFFFFFF); // drawStringWithShadow
                    if (!HardcoreRevivalConfig.SERVER.disableDeathTimer.get()) {
                        int deathSecondsLeft = Math.max(0, (HardcoreRevivalConfig.SERVER.maxDeathTicks.get() - deathTime) / 20);
                        mc.fontRenderer.drawString(event.getMatrixStack(), I18n.format("gui.hardcorerevival.rescue_time_left", deathSecondsLeft), 5, 7 + mc.fontRenderer.FONT_HEIGHT, 16777215);
                    } else {
                        mc.fontRenderer.drawString(event.getMatrixStack(), I18n.format("gui.hardcorerevival.wait_for_rescue"), 5, 7 + mc.fontRenderer.FONT_HEIGHT, 16777215);
                    }
                    mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
                }
            } else {
                if (targetEntity != -1 && targetProgress > 0) {
                    Entity entity = mc.world.getEntityByID(targetEntity);
                    if (entity instanceof PlayerEntity) {
                        TextComponent s = new TranslationTextComponent("gui.hardcorerevival.rescuing", entity.getDisplayName());
                        if (targetProgress >= 0.75f) {
                            s.appendString(" ...");
                        } else if (targetProgress >= 0.5f) {
                            s.appendString(" ..");
                        } else if (targetProgress >= 0.25f) {
                            s.appendString(" .");
                        }
                        mc.fontRenderer.func_238422_b_(event.getMatrixStack(), s.func_241878_f(), mc.getMainWindow().getScaledWidth() / 2f - mc.fontRenderer.getStringPropertyWidth(s) / 2f, mc.getMainWindow().getScaledHeight() / 2f + 30, 0xFFFFFFFF); // drawString, getStringWidth
                        mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
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
                        mc.displayGuiScreen(new ChatScreen(""));
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

                    // If right mouse is held down, and player is not in spectator mode, send revival packet
                    if (mc.mouseHelper.isRightDown() && !mc.player.isSpectator()) {
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
        Minecraft mc = event.getGui().getMinecraft();
        if (mc.player != null && isKnockedOut && event.getGui() instanceof ChatScreen) {
            Screen gui = event.getGui();

            enableButtonTimer = 0;
            buttonDie = new Button(gui.width / 2 - 100, gui.height / 2 - 30, 200, 20, new TranslationTextComponent("gui.hardcorerevival.die", ""), it -> {
                buttonDie.playDownSound(Minecraft.getInstance().getSoundHandler());
                NetworkHandler.channel.sendToServer(new MessageDie());
                acceptedDeath = true;
            });
            buttonDie.active = false;
            event.addWidget(buttonDie);
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        Screen gui = event.getGui();
        Minecraft mc = gui.getMinecraft();
        if (mc.player != null && isKnockedOut && gui instanceof ChatScreen) {
            enableButtonTimer += event.getRenderPartialTicks();
            if (buttonDie != null) {
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
            }

            RenderSystem.pushMatrix();
            RenderSystem.scalef(2f, 2f, 2f);
            AbstractGui.drawCenteredString(event.getMatrixStack(), mc.fontRenderer, I18n.format("gui.hardcorerevival.knocked_out"), gui.width / 2 / 2, 30, 16777215);
            RenderSystem.popMatrix();

            if (!HardcoreRevivalConfig.SERVER.disableDeathTimer.get()) {
                int deathSecondsLeft = Math.max(0, (HardcoreRevivalConfig.SERVER.maxDeathTicks.get() - deathTime) / 20);
                AbstractGui.drawCenteredString(event.getMatrixStack(), mc.fontRenderer, I18n.format("gui.hardcorerevival.rescue_time_left", deathSecondsLeft), gui.width / 2, gui.height / 2 + 10, 16777215);
            } else {
                AbstractGui.drawCenteredString(event.getMatrixStack(), mc.fontRenderer, I18n.format("gui.hardcorerevival.wait_for_rescue"), gui.width / 2, gui.height / 2 + 10, 16777215);
            }
        } else if (buttonDie != null) {
            buttonDie.visible = false;
        }
    }

    public void setDeathTime(int deathTime) {
        this.deathTime = deathTime;
        if (deathTime > 0) {
            isKnockedOut = true;
            Minecraft.getInstance().displayGuiScreen(new ChatScreen(""));
        }
    }

    public void setRevivalProgress(int entityId, float progress) {
        if (progress < 0) {
            targetEntity = -1;
            targetProgress = 0f;
        } else {
            targetEntity = entityId;
            targetProgress = progress;
        }
    }

    public void onFinalDeath() {
        isKnockedOut = true;
        acceptedDeath = true;

        // Manually mark the player as dead to have JourneyMap create a death point
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (HardcoreRevivalConfig.SERVER.glowOnDeath.get()) {
                mc.player.setGlowing(false);
                mc.player.setFlag(6, false); // glowing flag
            }
            mc.player.remove(true);
        }
        mc.displayGuiScreen(null);
    }
}
