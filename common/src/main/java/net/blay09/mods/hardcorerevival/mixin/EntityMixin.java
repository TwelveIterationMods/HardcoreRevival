package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "fireImmune()Z", at = @At("HEAD"), cancellable = true)
    private void fireImmune(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (MixinHooks.shouldCancelFire(entity)) {
            cir.setReturnValue(true);
        }
    }
}
