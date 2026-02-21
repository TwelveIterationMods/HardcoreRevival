package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.TickPhase;
import net.blay09.mods.balm.api.event.TickType;
import net.blay09.mods.balm.api.event.client.FovUpdateEvent;
import net.blay09.mods.balm.api.event.client.GuiDrawEvent;
import net.blay09.mods.balm.api.event.client.OpenScreenEvent;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.RescueMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HardcoreRevivalClient {

    private static boolean wasKnockedOut;
    private static boolean isRescuing;
    private static int targetEntity = -1;
    private static float targetProgress;
    private static boolean beingRescued;
    private static final double FALLBACK_MIN_DOT = 0.85;
    private static final double FALLBACK_EPSILON = 1.0E-6;

    public static void initialize() {
        Balm.getEvents().onEvent(OpenScreenEvent.class, HardcoreRevivalClient::onOpenScreen);
        Balm.getEvents().onEvent(FovUpdateEvent.class, HardcoreRevivalClient::onFovUpdate);
        Balm.getEvents().onEvent(GuiDrawEvent.Pre.class, HardcoreRevivalClient::onGuiDrawPre);
        Balm.getEvents().onEvent(GuiDrawEvent.Post.class, HardcoreRevivalClient::onGuiDrawPost);

        Balm.getEvents().onTickEvent(TickType.Client, TickPhase.Start, HardcoreRevivalClient::onClientTick);
    }

    private static boolean isKnockedOut() {
        LocalPlayer player = Minecraft.getInstance().player;
        return HardcoreRevival.getClientRevivalData().isKnockedOut() && player != null && player.isAlive();
    }

    private static boolean canRescueOthers(Player player) {
        return player != null && player.isAlive() && !player.isSpectator() && !HardcoreRevival.getClientRevivalData().isKnockedOut();
    }

    private static Player getRescueTarget(Player player) {
        Player crosshairTarget = getCrosshairRescueTarget(player);
        if (crosshairTarget != null) {
            return crosshairTarget;
        }

        return getFallbackFrontRescueTarget(player);
    }

    private static Player getCrosshairRescueTarget(Player player) {
        Entity pointedEntity = Minecraft.getInstance().crosshairPickEntity;
        if (!(pointedEntity instanceof Player target) || !HardcoreRevival.getRevivalData(target).isKnockedOut()) {
            return null;
        }

        if (player.distanceTo(target) > HardcoreRevivalConfig.getActive().rescueDistance) {
            return null;
        }

        return target;
    }

    private static Player getFallbackFrontRescueTarget(Player player) {
        double rescueDistance = HardcoreRevivalConfig.getActive().rescueDistance;
        AABB searchBounds = player.getBoundingBox().inflate(rescueDistance);
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle().normalize();

        Player bestTarget = null;
        double bestDot = FALLBACK_MIN_DOT;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (Player candidate : player.level().getEntitiesOfClass(Player.class, searchBounds, entity -> entity != player && entity.isAlive())) {
            if (!HardcoreRevival.getRevivalData(candidate).isKnockedOut()) {
                continue;
            }

            double distanceSqr = player.distanceToSqr(candidate);
            if (distanceSqr > rescueDistance * rescueDistance) {
                continue;
            }

            if (!player.hasLineOfSight(candidate)) {
                continue;
            }

            Vec3 toTarget = candidate.getBoundingBox().getCenter().subtract(eyePosition);
            if (toTarget.lengthSqr() < 1.0E-6) {
                continue;
            }

            double dot = lookVector.dot(toTarget.normalize());
            if (dot < FALLBACK_MIN_DOT) {
                continue;
            }

            if (dot > bestDot + FALLBACK_EPSILON
                    || (Math.abs(dot - bestDot) <= FALLBACK_EPSILON && (distanceSqr < bestDistanceSqr - FALLBACK_EPSILON
                    || (Math.abs(distanceSqr - bestDistanceSqr) <= FALLBACK_EPSILON && (bestTarget == null || candidate.getId() < bestTarget.getId()))))) {
                bestTarget = candidate;
                bestDot = dot;
                bestDistanceSqr = distanceSqr;
            }
        }

        return bestTarget;
    }

    private static void stopRescuing() {
        if (isRescuing) {
            Balm.getNetworking().sendToServer(new RescueMessage(-1));
            isRescuing = false;
            targetEntity = -1;
        }
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
        GuiGraphics guiGraphics = event.getGuiGraphics();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (event.getElement() == GuiDrawEvent.Element.ALL) {
            Minecraft mc = Minecraft.getInstance();
            if (isKnockedOut()) {
                var poseStack = guiGraphics.pose();
                poseStack.pushPose();
                poseStack.translate(0, 0, -300);
                GuiHelper.drawGradientRectW(guiGraphics, 0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight(), 0x60500000, 0x90FF0000);
                poseStack.popPose();

                if (mc.screen == null || mc.screen instanceof ChatScreen) {
                    int width = event.getWindow().getGuiScaledWidth();
                    int height = event.getWindow().getGuiScaledHeight();
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
                        guiGraphics.drawString(mc.font,
                                textComponent,
                                mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(textComponent) / 2,
                                mc.getWindow().getGuiScaledHeight() / 2 + 30,
                                0xFFFFFFFF,
                                true);
                    }
                }

                if (mc.player != null && canRescueOthers(mc.player) && !isRescuing && getRescueTarget(mc.player) != null) {
                    Component rescueKeyText = mc.options.keyUse.getTranslatedKeyMessage();
                    var textComponent = Component.translatable("gui.hardcorerevival.hold_to_rescue", rescueKeyText);
                    guiGraphics.drawString(mc.font,
                            textComponent,
                            mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(textComponent) / 2,
                            mc.getWindow().getGuiScaledHeight() / 2 + 30,
                            0xFFFFFFFF,
                            true);
                }
            }

            // Other mods start rendering weirdly if blend is not enabled at the end
            RenderSystem.enableBlend();
        }
    }

    public static void onClientTick(Minecraft client) {
        if (client.player != null) {
            if (isKnockedOut()) {
                stopRescuing();
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

                if (client.options.keyUse.isDown() && canRescueOthers(client.player)) {
                    if (!isRescuing) {
                        Player target = getRescueTarget(client.player);
                        if (target != null) {
                            targetEntity = target.getId();
                            Balm.getNetworking().sendToServer(new RescueMessage(targetEntity));
                            isRescuing = true;
                        }
                    }
                } else {
                    stopRescuing();
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
