package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "isImmobile()Z", at = @At("HEAD"), cancellable = true)
    private void isImmobile(CallbackInfoReturnable<Boolean> cir) {
        final var livingEntity = (LivingEntity) (Object) this;
        if (MixinHooks.shouldCancelClientMovement(livingEntity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canBeSeenAsEnemy()Z", at = @At("HEAD"), cancellable = true)
    private void canBeSeenAsEnemy(CallbackInfoReturnable<Boolean> cir) {
        if (HardcoreRevival.getRevivalData((LivingEntity) (Object) this).isKnockedOut()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void canAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof Player && HardcoreRevival.getRevivalData(target).isKnockedOut()) {
            cir.setReturnValue(false);
        }
    }
}
