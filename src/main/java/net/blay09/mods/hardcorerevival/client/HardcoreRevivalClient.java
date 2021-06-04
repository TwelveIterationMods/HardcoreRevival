package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.api.PlayerKnockedOutEvent;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.network.RescueMessage;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
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

    private static boolean wasKnockedOut;
    private static boolean isRescuing;
    private static int targetEntity = -1;
    private static float targetProgress;

    private static boolean isKnockedOut() {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        return HardcoreRevival.getClientRevivalData().isKnockedOut() && player != null && player.isAlive();
    }

    @SubscribeEvent
    public static void onOpenGui(GuiOpenEvent event) {
        if (isKnockedOut() && event.getGui() instanceof InventoryScreen) {
            event.setGui(new KnockoutScreen());
        }
    }

    @SubscribeEvent
    public static void onFov(FOVUpdateEvent event) {
        if (isKnockedOut()) {
            event.setNewfov(MathHelper.lerp(Minecraft.getInstance().gameSettings.fovScaleEffect, 1f, 0.5f));
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        // Flash the health bar red if the player is knocked out
        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH && isKnockedOut()) {
            int knockoutTicksPassed = HardcoreRevival.getClientRevivalData().getKnockoutTicksPassed();
            float redness = (float) Math.sin(knockoutTicksPassed / 2f);
            RenderSystem.color4f(1f, 1f - redness, 1 - redness, 1f);
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event) {
        RenderSystem.color4f(1f, 1f, 1f, 1f);

        if (event.getType() == RenderGameOverlayEvent.ElementType.PORTAL) {
            Minecraft mc = Minecraft.getInstance();
            if (isKnockedOut()) {
                RenderSystem.pushMatrix();
                RenderSystem.translatef(0, 0, -300);
                GuiHelper.drawGradientRectW(event.getMatrixStack(), 0, 0, mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight(), 0x60500000, 0x90FF0000);
                RenderSystem.popMatrix();

                if (mc.currentScreen == null || mc.currentScreen instanceof ChatScreen) {
                    int width = event.getWindow().getScaledWidth();
                    int height = event.getWindow().getScaledHeight();
                    GuiHelper.renderKnockedOutTitle(event.getMatrixStack(), width);
                    GuiHelper.renderDeathTimer(event.getMatrixStack(), width, height);

                    ITextComponent openDeathScreenKey = mc.gameSettings.keyBindInventory.func_238171_j_(); // getDisplayName()
                    final TranslationTextComponent openDeathScreenText = new TranslationTextComponent("gui.hardcorerevival.open_death_screen", openDeathScreenKey);
                    AbstractGui.drawCenteredString(event.getMatrixStack(), mc.fontRenderer, openDeathScreenText, width / 2, height / 2 + 25, 0xFFFFFFFF); // drawStringWithShadow

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
                    if (!wasKnockedOut) {
                        mc.player.setForcedPose(Pose.FALL_FLYING);
                        mc.displayGuiScreen(new KnockoutScreen());
                        wasKnockedOut = true;
                    }

                    HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(mc.player);
                    revivalData.setKnockoutTicksPassed(revivalData.getKnockoutTicksPassed() + 1);
                } else {
                    if (wasKnockedOut) {
                        mc.player.setForcedPose(null);
                        wasKnockedOut = false;
                    }

                    // If knockout screen is still shown, close it
                    if (mc.currentScreen instanceof KnockoutScreen) {
                        mc.displayGuiScreen(null);
                    }

                    // If right mouse is held down, and player is not in spectator mode, send rescue packet
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
