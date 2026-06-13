package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @ModifyVariable(
            method = "Lnet/minecraft/world/entity/player/Player;travel(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1
    )
    private Vec3 cancelMovement(Vec3 pTravelVector) {
        if (MixinHooks.shouldCancelMovement((Entity) (Object) this)) {
            return Vec3.ZERO;
        }
        return pTravelVector;
    }

    @Inject(method = "isHurt()Z", at = @At("HEAD"), cancellable = true)
    private void isHurt(CallbackInfoReturnable<Boolean> ci) {
        Player entity = (Player) (Object) this;
        if (MixinHooks.shouldCancelHealing(entity)) {
            ci.setReturnValue(false);
        }
    }

}
