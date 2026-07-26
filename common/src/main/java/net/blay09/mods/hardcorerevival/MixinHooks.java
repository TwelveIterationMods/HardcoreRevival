package net.blay09.mods.hardcorerevival;

import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.handler.KnockoutRestrictionHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class MixinHooks {

    public static boolean shouldCancelMovement(Entity entity) {
        return entity instanceof Player player && PlayerHardcoreRevivalManager.isKnockedOut(player);
    }

    public static boolean shouldCancelTeleport(Player player) {
        return !HardcoreRevivalConfig.getActive().allowTeleports && PlayerHardcoreRevivalManager.isKnockedOut(player);
    }

    public static boolean shouldCancelHealing(Player player) {
        return PlayerHardcoreRevivalManager.isKnockedOut(player);
    }

    public static boolean shouldCancelFire(Entity entity) {
        return entity instanceof Player player && PlayerHardcoreRevivalManager.isKnockedOut(player);
    }

    public static void handleProcessPlayerRotation(ServerPlayer player, ServerboundMovePlayerPacket packet) {
        float yaw = packet.getYRot(player.getYRot());
        float pitch = packet.getXRot(player.getXRot());
        player.absSnapRotationTo(yaw, pitch);
    }

    public static boolean shouldCancelToss(Player player, ItemStack itemStack) {
        return PlayerHardcoreRevivalManager.isKnockedOut(player) && !KnockoutRestrictionHandler.mayTossItemKnockedOut(itemStack);
    }

    public static boolean shouldCancelTossAll(Player player) {
        return PlayerHardcoreRevivalManager.isKnockedOut(player);
    }

    public static boolean shouldDiscardEnderPearl(Entity enderPearl, @Nullable Entity owner) {
        return !enderPearl.level().isClientSide()
                && HardcoreRevivalConfig.getActive().enderPearlsVanishOnKnockout
                && owner instanceof Player player
                && PlayerHardcoreRevivalManager.isKnockedOut(player);
    }
}
