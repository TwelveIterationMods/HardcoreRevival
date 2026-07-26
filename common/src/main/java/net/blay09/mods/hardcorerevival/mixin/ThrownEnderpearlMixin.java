package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEnderpearl.class)
public class ThrownEnderpearlMixin {

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void tick(CallbackInfo ci) {
        final var enderPearl = (ThrownEnderpearl) (Object) this;
        if (MixinHooks.shouldDiscardEnderPearl(enderPearl, enderPearl.getOwner())) {
            enderPearl.discard();
            ci.cancel();
        }
    }
}
