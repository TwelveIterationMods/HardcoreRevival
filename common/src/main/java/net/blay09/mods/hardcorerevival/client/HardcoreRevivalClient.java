package net.blay09.mods.hardcorerevival.client;

import com.mojang.blaze3d.platform.Window;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.balm.client.platform.event.callback.ClientTickCallback;
import net.blay09.mods.balm.client.platform.event.callback.RenderCallback;
import net.blay09.mods.balm.client.platform.event.callback.ScreenCallback;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.RescueMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class HardcoreRevivalClient {

    private static boolean wasKnockedOut;
    private static boolean isRescuing;
    private static int targetEntity = -1;
    private static float targetProgress;
    private static boolean beingRescued;
    private static final double FALLBACK_MIN_DOT = 0.85;
    private static final double FALLBACK_EPSILON = 1.0E-6;

    public static void initialize(BalmClientRegistrars registrars) {
        ScreenCallback.Opening.EVENT.register(HardcoreRevivalClient::onOpenScreen);
        RenderCallback.UpdateFov.EVENT.register(HardcoreRevivalClient::onFovUpdate);
        RenderCallback.Gui.AFTER.register(HardcoreRevivalClient::onGuiDrawPost);

        ClientTickCallback.BEFORE.register(HardcoreRevivalClient::onClientTick);
    }

    private static boolean isKnockedOut() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && PlayerHardcoreRevivalManager.isKnockedOut(player) && player.isAlive();
    }

    private static boolean canRescueOthers(@Nullable Player player) {
        return player != null && player.isAlive() && !player.isSpectator() && !PlayerHardcoreRevivalManager.isKnockedOut(player);
    }

    private static @Nullable Player getRescueTarget(Player player) {
        Player crosshairTarget = getCrosshairRescueTarget(player);
        if (crosshairTarget != null) {
            return crosshairTarget;
        }

        return getFallbackFrontRescueTarget(player);
    }

    private static @Nullable Player getCrosshairRescueTarget(Player player) {
        Entity pointedEntity = Minecraft.getInstance().crosshairPickEntity;
        if (!(pointedEntity instanceof Player target) || !PlayerHardcoreRevivalManager.isKnockedOut(target)) {
            return null;
        }

        if (player.distanceTo(target) > HardcoreRevivalConfig.getActive().rescueDistance) {
            return null;
        }

        return target;
    }

    private static @Nullable Player getFallbackFrontRescueTarget(Player player) {
        double rescueDistance = HardcoreRevivalConfig.getActive().rescueDistance;
        AABB searchBounds = player.getBoundingBox().inflate(rescueDistance);
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle().normalize();

        Player bestTarget = null;
        double bestDot = FALLBACK_MIN_DOT;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (Player candidate : player.level().getEntitiesOfClass(Player.class, searchBounds, entity -> entity != player && entity.isAlive())) {
            if (!PlayerHardcoreRevivalManager.isKnockedOut(candidate)) {
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
            Balm.networking().sendToServer(new RescueMessage(-1));
            isRescuing = false;
            targetEntity = -1;
        }
    }

    public static Screen onOpenScreen(Screen screen) {
        return isKnockedOut() && screen instanceof InventoryScreen ? new KnockoutScreen() : screen;
    }

    public static float onFovUpdate(LivingEntity entity, float fov) {
        return isKnockedOut() ? 0.5f : fov;
    }

    public static void onGuiDrawPost(GuiGraphicsExtractor guiGraphics, Window window) {
        Minecraft mc = Minecraft.getInstance();
        if (isKnockedOut()) {
            var poseStack = guiGraphics.pose();
            poseStack.pushMatrix();
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
                    guiGraphics.centeredText(mc.font, openDeathScreenText, width / 2, height / 2 + 25, 0xFFFFFFFF);
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
                    guiGraphics.text(mc.font,
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
                guiGraphics.text(mc.font,
                        textComponent,
                        mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(textComponent) / 2,
                        mc.getWindow().getGuiScaledHeight() / 2 + 30,
                        0xFFFFFFFF,
                        true);
            }
        }
    }

    public static void onClientTick(Minecraft client) {
        if (client.player != null) {
            if (isKnockedOut()) {
                stopRescuing();
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

                if (client.options.keyUse.isDown() && canRescueOthers(client.player)) {
                    if (!isRescuing) {
                        final var target = getRescueTarget(client.player);
                        if (target != null) {
                            targetEntity = target.getId();
                            Balm.networking().sendToServer(new RescueMessage(targetEntity));
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
        final var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (progress < 0) {
            targetEntity = -1;
            targetProgress = 0f;

            Balm.hooks().setForcedPose(player, null);
        } else {
            targetEntity = entityId;
            targetProgress = progress;

            Balm.hooks().setForcedPose(player, Pose.CROUCHING);
        }
    }

    public static void setBeingRescued(boolean beingRescued) {
        HardcoreRevivalClient.beingRescued = beingRescued;
    }

    public static boolean isBeingRescued() {
        return beingRescued;
    }
}
