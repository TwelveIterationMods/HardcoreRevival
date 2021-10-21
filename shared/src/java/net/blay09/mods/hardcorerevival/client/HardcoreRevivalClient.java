package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.balm.api.Balm;
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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
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
    private static boolean beingRescued;

    private static boolean isKnockedOut() {
        LocalPlayer player = Minecraft.getInstance().player;
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
            event.setNewfov(Mth.lerp(Minecraft.getInstance().options.fovEffectScale, 1f, 0.5f));
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        // Flash the health bar red if the player is knocked out
        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH && isKnockedOut()) {
            int knockoutTicksPassed = HardcoreRevival.getClientRevivalData().getKnockoutTicksPassed();
            float redness = (float) Math.sin(knockoutTicksPassed / 2f);
            RenderSystem.setShaderColor(1f, 1f - redness, 1 - redness, 1f);
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event) {
        PoseStack poseStack = event.getMatrixStack();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (event.getType() == RenderGameOverlayEvent.ElementType.PORTAL) {
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
                    final TranslatableComponent openDeathScreenText = new TranslatableComponent("gui.hardcorerevival.open_death_screen", openDeathScreenKey);
                    GuiComponent.drawCenteredString(poseStack, mc.font, openDeathScreenText, width / 2, height / 2 + 25, 0xFFFFFFFF);

                    RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
                }
            } else {
                if (targetEntity != -1 && targetProgress > 0) {
                    Entity entity = mc.level.getEntity(targetEntity);
                    if (entity instanceof Player) {
                        TranslatableComponent textComponent = new TranslatableComponent("gui.hardcorerevival.rescuing", entity.getDisplayName());
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
                        TranslatableComponent textComponent = new TranslatableComponent("gui.hardcorerevival.hold_to_rescue", rescueKeyText);
                        mc.font.drawShadow(poseStack, textComponent, mc.getWindow().getGuiScaledWidth() / 2f - mc.font.width(textComponent) / 2f, mc.getWindow().getGuiScaledHeight() / 2f + 30, 0xFFFFFFFF);
                        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
                    }
                }
            }

            // Other mods start rendering weirdly if blend is not enabled at the end
            RenderSystem.enableBlend();
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        // Suppress item drops and movement when knocked out
        if (isKnockedOut()) {
            //noinspection StatementWithEmptyBody
            while (mc.options.keyDrop.consumeClick()) ;
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
                        mc.setScreen(new KnockoutScreen());
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
                    if (mc.screen instanceof KnockoutScreen) {
                        mc.setScreen(null);
                    }

                    // If right mouse is held down, and player is not in spectator mode, send rescue packet
                    if (mc.options.keyUse.isDown() && !mc.player.isSpectator() && mc.player.isAlive() && !HardcoreRevival.getClientRevivalData().isKnockedOut()) {
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
    }

    public static void setRevivalProgress(int entityId, float progress) {
        if (progress < 0) {
            targetEntity = -1;
            targetProgress = 0f;

            Minecraft.getInstance().player.setForcedPose(null);
        } else {
            targetEntity = entityId;
            targetProgress = progress;

            Minecraft.getInstance().player.setForcedPose(Pose.CROUCHING);
        }
    }

    public static void setBeingRescued(boolean beingRescued) {
        HardcoreRevivalClient.beingRescued = beingRescued;
    }

    public static boolean isBeingRescued() {
        return beingRescued;
    }
}
