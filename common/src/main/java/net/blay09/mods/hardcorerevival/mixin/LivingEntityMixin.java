package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "Lnet/minecraft/world/entity/LivingEntity;canBeSeenAsEnemy()Z", at = @At("HEAD"), cancellable = true)
    private void canBeSeenAsEnemy(CallbackInfoReturnable<Boolean> cir) {
        if (HardcoreRevival.getRevivalData((LivingEntity) (Object) this).isKnockedOut()) {
            cir.setReturnValue(false);
        }
    }
}
