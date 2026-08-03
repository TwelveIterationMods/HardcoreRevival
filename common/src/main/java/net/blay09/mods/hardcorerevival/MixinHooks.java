package net.blay09.mods.hardcorerevival;

import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.handler.KnockoutRestrictionHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MixinHooks {

    public static boolean shouldCancelMovement(Entity entity) {
        return entity instanceof Player && HardcoreRevival.getRevivalData(entity).isKnockedOut();
    }

    public static boolean shouldCancelClientMovement(Entity entity) {
        return entity.level().isClientSide() && shouldCancelMovement(entity);
    }

    public static boolean shouldCancelHealing(Player entity) {
        return HardcoreRevival.getRevivalData(entity).isKnockedOut();
    }

    public static boolean shouldCancelTeleport(Player player) {
        return !HardcoreRevivalConfig.getActive().allowTeleports && HardcoreRevival.getRevivalData(player).isKnockedOut();
    }

    public static void handleProcessPlayerRotation(ServerPlayer player, ServerboundMovePlayerPacket packet) {
        float yaw = packet.getYRot(player.getYRot());
        float pitch = packet.getXRot(player.getXRot());
        player.absMoveTo(player.getX(), player.getY(), player.getZ(), yaw, pitch);
    }

    public static boolean shouldCancelToss(Player player, ItemStack itemStack) {
        return HardcoreRevival.getRevivalData(player).isKnockedOut() && !KnockoutRestrictionHandler.mayTossItemKnockedOut(itemStack);
    }

    public static boolean shouldCancelTossAll(Player player) {
        return HardcoreRevival.getRevivalData(player).isKnockedOut();
    }

    public static boolean shouldDiscardEnderPearl(Entity enderPearl, @Nullable Entity owner) {
        return !enderPearl.level().isClientSide()
                && HardcoreRevivalConfig.getActive().enderPearlsVanishOnKnockout
                && owner instanceof Player player
                && HardcoreRevival.getManager().isKnockedOut(player);
    }
}
