package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.platform.Window;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.client.platform.event.callback.ClientTickCallback;
import net.blay09.mods.balm.client.platform.event.callback.RenderCallback;
import net.blay09.mods.balm.client.platform.event.callback.ScreenCallback;
import net.blay09.mods.balm.platform.event.EventHandling;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.RescueMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

public class HardcoreRevivalClient {

    private static boolean wasKnockedOut;
    private static boolean isRescuing;
    private static int targetEntity = -1;
    private static float targetProgress;
    private static boolean beingRescued;

    public static void initialize() {
        ScreenCallback.Open.EVENT.register(HardcoreRevivalClient::onOpenScreen);
        RenderCallback.UpdateFov.EVENT.register(HardcoreRevivalClient::onFovUpdate);
        RenderCallback.Gui.Health.BEFORE.register(HardcoreRevivalClient::onRenderGuiHealthBefore);
        RenderCallback.Gui.Health.AFTER.register(HardcoreRevivalClient::onRenderGuiHealthAfter);
        RenderCallback.Gui.AFTER.register(HardcoreRevivalClient::onGuiDrawPost);

        ClientTickCallback.BEFORE.register(HardcoreRevivalClient::onClientTick);
    }

    private static boolean isKnockedOut() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && PlayerHardcoreRevivalManager.isKnockedOut(player) && player.isAlive();
    }

    public static Screen onOpenScreen(Screen screen) {
        return isKnockedOut() && screen instanceof InventoryScreen ? new KnockoutScreen() : screen;
    }

    public static float onFovUpdate(LivingEntity entity, float fov) {
        return isKnockedOut() ? (float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1f, 0.5f) : fov;
    }

    public static EventHandling onRenderGuiHealthBefore(GuiGraphics guiGraphics, Window window) {
        // Flash the health bar red if the player is knocked out
        if (isKnockedOut()) {
            int knockoutTicksPassed = PlayerHardcoreRevivalManager.getKnockoutTicksPassed(Minecraft.getInstance().player);
            float redness = (float) Math.sin(knockoutTicksPassed / 2f);
            // TODO 1.21.6: RenderSystem.setShaderColor(1f, 1f - redness, 1 - redness, 1f);
        }
        return EventHandling.RESUME;
    }

    public static void onRenderGuiHealthAfter(GuiGraphics guiGraphics, Window window) {
        if (isKnockedOut()) {
            // TODO 1.21.6: RenderSystem.setShaderColor(1f, 1f, 1, 1f);
        }
    }

    public static void onGuiDrawPost(GuiGraphics guiGraphics, Window window) {
        Minecraft mc = Minecraft.getInstance();
        if (isKnockedOut()) {
            var poseStack = guiGraphics.pose();
            poseStack.pushMatrix();
            // TODO 1.21.6: poseStack.translate(0, 0, -300);
            // TODO 1.21.6: RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            GuiHelper.drawGradientRectW(guiGraphics, 0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight(), 0x60500000, 0x90FF0000);
            poseStack.popMatrix();

            if (mc.screen == null || mc.screen instanceof ChatScreen) {
                int width = window.getGuiScaledWidth();
                int height = window.getGuiScaledHeight();
                GuiHelper.renderKnockedOutTitle(guiGraphics, width);
                GuiHelper.renderDeathTimer(guiGraphics, width, height, beingRescued);

                if (HardcoreRevivalConfig.getActive().allowAcceptingFate) {
                    Component openDeathScreenKey = mc.options.keyInventory.getTranslatedKeyMessage();
                    final var openDeathScreenText = Component.translatable("gui.hardcorerevival.open_death_screen", openDeathScreenKey);
                    guiGraphics.drawCenteredString(mc.font, openDeathScreenText, width / 2, height / 2 + 25, 0xFFFFFFFF);
                }
            }
        } else {
            if (targetEntity != -1 && targetProgress > 0) {
                Entity entity = mc.level.getEntity(targetEntity);
                if (entity instanceof Player) {
                    var textComponent = Component.translatable("gui.hardcorerevival.rescuing", entity.getDisplayName());
                    if (targetProgress >= 0.75f) {
                        textComponent.append(" ...");
                    } else if (targetProgress >= 0.5f) {
                        textComponent.append(" ..");
                    } else if (targetProgress >= 0.25f) {
                        textComponent.append(" .");
                    }
                    // TODO 1.21.6: RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                    guiGraphics.drawString(mc.font,
                            textComponent,
                            mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(textComponent) / 2,
                            mc.getWindow().getGuiScaledHeight() / 2 + 30,
                            0xFFFFFFFF,
                            true);
                }
            }

            if (!PlayerHardcoreRevivalManager.isKnockedOut(mc.player) && mc.player != null && !mc.player.isSpectator() && mc.player.isAlive() && !isRescuing) {
                Entity pointedEntity = Minecraft.getInstance().crosshairPickEntity;
                if (pointedEntity instanceof Player pointedPlayer && PlayerHardcoreRevivalManager.isKnockedOut(pointedPlayer) && mc.player.distanceTo(
                        pointedEntity) <= HardcoreRevivalConfig.getActive().rescueDistance) {
                    Component rescueKeyText = mc.options.keyUse.getTranslatedKeyMessage();
                    var textComponent = Component.translatable("gui.hardcorerevival.hold_to_rescue", rescueKeyText);
                    // TODO 1.21.6: RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                    guiGraphics.drawString(mc.font,
                            textComponent,
                            mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(textComponent) / 2,
                            mc.getWindow().getGuiScaledHeight() / 2 + 30,
                            0xFFFFFFFF,
                            true);
                }
            }
        }
    }

    public static void onClientTick(Minecraft client) {
        if (client.player != null) {
            if (isKnockedOut()) {
                if (!wasKnockedOut) {
                    Balm.hooks().setForcedPose(client.player, Pose.FALL_FLYING);
                    client.setScreen(new KnockoutScreen());
                    wasKnockedOut = true;
                }

                PlayerHardcoreRevivalManager.setKnockoutTicksPassed(client.player, PlayerHardcoreRevivalManager.getKnockoutTicksPassed(client.player) + 1);
            } else {
                if (wasKnockedOut) {
                    Balm.hooks().setForcedPose(client.player, null);
                    wasKnockedOut = false;
                }

                // If knockout screen is still shown, close it
                if (client.screen instanceof KnockoutScreen) {
                    client.setScreen(null);
                }

                // If right mouse is held down, and player is not in spectator mode, send rescue packet
                if (client.options.keyUse.isDown() && !client.player.isSpectator() && client.player.isAlive() && !PlayerHardcoreRevivalManager.isKnockedOut(
                        client.player)) {
                    if (!isRescuing) {
                        Balm.networking().sendToServer(new RescueMessage(true));
                        isRescuing = true;
                    }
                } else {
                    if (isRescuing) {
                        Balm.networking().sendToServer(new RescueMessage(false));
                        isRescuing = false;
                    }
                }
            }
        }
    }

    public static void setRevivalProgress(int entityId, float progress) {
        if (progress < 0) {
            targetEntity = -1;
            targetProgress = 0f;

            Balm.hooks().setForcedPose(Minecraft.getInstance().player, null);
        } else {
            targetEntity = entityId;
            targetProgress = progress;

            Balm.hooks().setForcedPose(Minecraft.getInstance().player, Pose.CROUCHING);
        }
    }

    public static void setBeingRescued(boolean beingRescued) {
        HardcoreRevivalClient.beingRescued = beingRescued;
    }

    public static boolean isBeingRescued() {
        return beingRescued;
    }
}
