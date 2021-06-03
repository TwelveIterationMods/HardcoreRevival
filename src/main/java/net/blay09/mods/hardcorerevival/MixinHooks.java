package net.blay09.mods.hardcorerevival;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class MixinHooks {

    public static boolean shouldCancelMovement(Entity entity, MoverType type, Vector3d pos) {
        return entity instanceof PlayerEntity && HardcoreRevival.getRevivalData(entity).isKnockedOut();
    }

    public static boolean shouldCancelTarget(@Nullable LivingEntity attacker, LivingEntity target) {
        return target instanceof PlayerEntity && HardcoreRevival.getRevivalData(target).isKnockedOut();
    }
}
