package net.blay09.mods.hardcorerevival;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.client.CPlayerPacket;

import javax.annotation.Nullable;

public class MixinHooks {

    public static boolean shouldCancelMovement(Entity entity) {
        return entity instanceof PlayerEntity && HardcoreRevival.getRevivalData(entity).isKnockedOut();
    }

    public static boolean shouldCancelAttackTarget(@Nullable LivingEntity attacker, LivingEntity target) {
        return target instanceof PlayerEntity && HardcoreRevival.getRevivalData(target).isKnockedOut();
    }

    public static boolean shouldCancelHealing(PlayerEntity entity) {
        return HardcoreRevival.getRevivalData(entity).isKnockedOut();
    }

    public static void handleProcessPlayerRotation(ServerPlayerEntity player, CPlayerPacket packet) {
        float yaw = packet.getYaw(player.rotationYaw);
        float pitch = packet.getPitch(player.rotationPitch);
        player.setPositionAndRotation(player.getPosX(), player.getPosY(), player.getPosZ(), yaw, pitch);
    }
}
