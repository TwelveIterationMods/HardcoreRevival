package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(EntityPredicate.class)
public class EntityPredicateMixin {

    @Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    public void canTarget(@Nullable LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> ci) {
        if (MixinHooks.shouldCancelTarget(attacker, target)) {
            ci.setReturnValue(false);
        }
    }
}
