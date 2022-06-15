package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.TickPhase;
import net.blay09.mods.balm.api.event.TickType;
import net.blay09.mods.balm.api.event.client.FovUpdateEvent;
import net.blay09.mods.balm.api.event.client.GuiDrawEvent;
import net.blay09.mods.balm.api.event.client.KeyInputEvent;
import net.blay09.mods.balm.api.event.client.OpenScreenEvent;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.RescueMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

public class HardcoreRevivalClient {

    private static boolean wasKnockedOut;
    private static boolean isRescuing;
    private static int targetEntity = -1;
    private static float targetProgress;
    private static boolean beingRescued;

    public static void initialize() {
        Balm.getEvents().onEvent(OpenScreenEvent.class, HardcoreRevivalClient::onOpenScreen);
        Balm.getEvents().onEvent(FovUpdateEvent.class, HardcoreRevivalClient::onFovUpdate);
        Balm.getEvents().onEvent(KeyInputEvent.class, HardcoreRevivalClient::onKeyInput);
        Balm.getEvents().onEvent(GuiDrawEvent.Pre.class, HardcoreRevivalClient::onGuiDrawPre);
        Balm.getEvents().onEvent(GuiDrawEvent.Post.class, HardcoreRevivalClient::onGuiDrawPost);

        Balm.getEvents().onTickEvent(TickType.Client, TickPhase.Start, HardcoreRevivalClient::onClientTick);
    }

    private static boolean isKnockedOut() {
        LocalPlayer player = Minecraft.getInstance().player;
        return HardcoreRevival.getClientRevivalData().isKnockedOut() && player != null && player.isAlive();
    }

    public static void onOpenScreen(OpenScreenEvent event) {
        if (isKnockedOut() && event.getScreen() instanceof InventoryScreen) {
            event.setScreen(new KnockoutScreen());
        }
    }

    public static void onFovUpdate(FovUpdateEvent event) {
        if (isKnockedOut()) {
            event.setFov((float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1f, 0.5f));
        }
    }

    public static void onGuiDrawPre(GuiDrawEvent.Pre event) {
        // Flash the health bar red if the player is knocked out
        if (event.getElement() == GuiDrawEvent.Element.HEALTH && isKnockedOut()) {
            int knockoutTicksPassed = HardcoreRevival.getClientRevivalData().getKnockoutTicksPassed();
            float redness = (float) Math.sin(knockoutTicksPassed / 2f);
            RenderSystem.setShaderColor(1f, 1f - redness, 1 - redness, 1f);
        }
    }

    public static void onGuiDrawPost(GuiDrawEvent.Post event) {
        PoseStack poseStack = event.getPoseStack();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (event.getElement() == GuiDrawEvent.Element.ALL) {
            Minecraft mc = Minecraft.getInstance();
            if (isKnockedOut()) {
                poseStack.pushPose();
                poseStack.translate(0, 0, -300);
                GuiHelper.drawGradientRectW(poseStack, 0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight(), 0x60500000, 0x90FF0000);
                poseStack.popPose();

                if (mc.screen == null || mc.screen instanceof ChatScreen) {
                    int width = event.getWindow().getGuiScaledWidth();
                    int height = event.getWindow().getGuiScaledHeight();
                    GuiHelper.renderKnockedOutTitle(poseStack, width);
                    GuiHelper.renderDeathTimer(poseStack, width, height, beingRescued);

                    Component openDeathScreenKey = mc.options.keyInventory.getTranslatedKeyMessage(); // getDisplayName()
                    final var openDeathScreenText = Component.translatable("gui.hardcorerevival.open_death_screen", openDeathScreenKey);
                    GuiComponent.drawCenteredString(poseStack, mc.font, openDeathScreenText, width / 2, height / 2 + 25, 0xFFFFFFFF);

                    RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
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
                        mc.font.drawShadow(poseStack, textComponent, mc.getWindow().getGuiScaledWidth() / 2f - mc.font.width(textComponent) / 2f, mc.getWindow().getGuiScaledHeight() / 2f + 30, 0xFFFFFFFF);
                        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
                    }
                }

                if (!HardcoreRevival.getClientRevivalData().isKnockedOut() && mc.player != null && !mc.player.isSpectator() && mc.player.isAlive() && !isRescuing) {
                    Entity pointedEntity = Minecraft.getInstance().crosshairPickEntity;
                    if (pointedEntity != null && HardcoreRevival.getRevivalData(pointedEntity).isKnockedOut() && mc.player.distanceTo(pointedEntity) <= HardcoreRevivalConfig.getActive().rescueDistance) {
                        Component rescueKeyText = mc.options.keyUse.getTranslatedKeyMessage();
                        var textComponent = Component.translatable("gui.hardcorerevival.hold_to_rescue", rescueKeyText);
                        mc.font.drawShadow(poseStack, textComponent, mc.getWindow().getGuiScaledWidth() / 2f - mc.font.width(textComponent) / 2f, mc.getWindow().getGuiScaledHeight() / 2f + 30, 0xFFFFFFFF);
                        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
                    }
                }
            }

            // Other mods start rendering weirdly if blend is not enabled at the end
            RenderSystem.enableBlend();
        }
    }

    public static void onKeyInput(KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        // Suppress item drops and movement when knocked out
        if (isKnockedOut()) {
            //noinspection StatementWithEmptyBody
            while (mc.options.keyDrop.consumeClick()) ;
        }
    }

    public static void onClientTick(Minecraft client) {
        if (client.player != null) {
            if (isKnockedOut()) {
                if (!wasKnockedOut) {
                    Balm.getHooks().setForcedPose(client.player, Pose.FALL_FLYING);
                    client.setScreen(new KnockoutScreen());
                    wasKnockedOut = true;
                }

                HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(client.player);
                revivalData.setKnockoutTicksPassed(revivalData.getKnockoutTicksPassed() + 1);
            } else {
                if (wasKnockedOut) {
                    Balm.getHooks().setForcedPose(client.player, null);
                    wasKnockedOut = false;
                }

                // If knockout screen is still shown, close it
                if (client.screen instanceof KnockoutScreen) {
                    client.setScreen(null);
                }

                // If right mouse is held down, and player is not in spectator mode, send rescue packet
                if (client.options.keyUse.isDown() && !client.player.isSpectator() && client.player.isAlive() && !HardcoreRevival.getClientRevivalData().isKnockedOut()) {
                    if (!isRescuing) {
                        Balm.getNetworking().sendToServer(new RescueMessage(true));
                        isRescuing = true;
                    }
                } else {
                    if (isRescuing) {
                        Balm.getNetworking().sendToServer(new RescueMessage(false));
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

            Balm.getHooks().setForcedPose(Minecraft.getInstance().player, null);
        } else {
            targetEntity = entityId;
            targetProgress = progress;

            Balm.getHooks().setForcedPose(Minecraft.getInstance().player, Pose.CROUCHING);
        }
    }

    public static void setBeingRescued(boolean beingRescued) {
        HardcoreRevivalClient.beingRescued = beingRescued;
    }

    public static boolean isBeingRescued() {
        return beingRescued;
    }
}
