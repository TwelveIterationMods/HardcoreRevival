package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.api.PlayerKnockedOutEvent;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.network.RescueMessage;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HardcoreRevival.MOD_ID)
public class HardcoreRevivalClient {

    // Rescuing
    private static boolean isRescuing;
    private static int targetEntity = -1;
    private static float targetProgress;

    private static boolean isKnockedOut() {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        return player != null && HardcoreRevival.getClientRevivalData().isKnockedOut();
    }

    @SubscribeEvent
    public static void onOpenGui(GuiOpenEvent event) {
        if (isKnockedOut() && event.getGui() instanceof InventoryScreen) {
            event.setGui(new KnockoutScreen());
        }
    }

    @SubscribeEvent
    public static void onKnockout(PlayerKnockedOutEvent event) {
        if (event.getPlayer() == Minecraft.getInstance().player) {
            Minecraft.getInstance().displayGuiScreen(new KnockoutScreen());
        }
    }

    @SubscribeEvent
    public static void onFov(FOVUpdateEvent event) {
        if (isKnockedOut()) {
            event.setNewfov(MathHelper.lerp(Minecraft.getInstance().gameSettings.fovScaleEffect, 1f, 0.5f));
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.PORTAL) {
            Minecraft mc = Minecraft.getInstance();
            if (isKnockedOut()) {
                RenderSystem.pushMatrix();
                RenderSystem.translatef(0, 0, -300);
                GuiHelper.drawGradientRectW(event.getMatrixStack(), 0, 0, mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight(), 0x60500000, 0x90FF0000);
                RenderSystem.popMatrix();
                if (mc.currentScreen == null && mc.player != null) {
                    ITextComponent openDeathScreenKey = mc.gameSettings.keyBindChat.func_238171_j_(); // getDisplayName()
                    final TranslationTextComponent openDeathScreenText = new TranslationTextComponent("gui.hardcorerevival.open_death_screen", openDeathScreenKey);
                    mc.fontRenderer.func_238407_a_(event.getMatrixStack(), openDeathScreenText.func_241878_f(), 5, 5, 0xFFFFFFFF); // drawStringWithShadow
                    if (!HardcoreRevivalConfig.COMMON.disableDeathTimer.get()) {
                        int knockoutTicksPassed = HardcoreRevival.getRevivalData(mc.player).getKnockoutTicksPassed();
                        int deathSecondsLeft = Math.max(0, (HardcoreRevivalConfig.COMMON.maxDeathTicks.get() - knockoutTicksPassed) / 20);
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
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        // Suppress item drops and movement when knocked out
        if (isKnockedOut()) {
            //noinspection StatementWithEmptyBody
            while (mc.gameSettings.keyBindDrop.isPressed()) ;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (isKnockedOut()) {
                    HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(mc.player);
                    revivalData.setKnockoutTicksPassed(revivalData.getKnockoutTicksPassed() + 1);
                } else {
                    // If right mouse is held down, and player is not in spectator mode, send revival packet
                    if (mc.mouseHelper.isRightDown() && !mc.player.isSpectator()) {
                        if (!isRescuing) {
                            NetworkHandler.channel.sendToServer(new RescueMessage(true));
                            isRescuing = true;
                        }
                    } else {
                        if (isRescuing) {
                            NetworkHandler.channel.sendToServer(new RescueMessage(false));
                            isRescuing = false;
                        }
                    }
                }
            }
        }
    }

    public static void setRevivalProgress(int entityId, float progress) {
        if (progress < 0) {
            targetEntity = -1;
            targetProgress = 0f;
        } else {
            targetEntity = entityId;
            targetProgress = progress;
        }
    }

}
