package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "shouldHeal()Z", at = @At("HEAD"), cancellable = true)
    private void shouldHeal(CallbackInfoReturnable<Boolean> ci) {
        PlayerEntity entity = (PlayerEntity) (Object) this;
        if (MixinHooks.shouldCancelHealing(entity)) {
            ci.setReturnValue(false);
        }
    }

}
