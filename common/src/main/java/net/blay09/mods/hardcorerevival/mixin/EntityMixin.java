package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

//    @ModifyVariable(
//            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
//            at = @At("HEAD"),
//            argsOnly = true,
//            index = 2 // 0 = this, 1 = MoverType, 2 = Vec3
//    )
//    private Vec3 cancelMovement(Vec3 pos) {
//        Entity entity = (Entity) (Object) this;
//        if (MixinHooks.shouldCancelMovement(entity)) {
//            return Vec3.ZERO;
//        }
//        return pos;
//    }

    @Inject(method = "fireImmune()Z", at = @At("HEAD"), cancellable = true)
    private void fireImmune(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (MixinHooks.shouldCancelFire(entity)) {
            cir.setReturnValue(true);
        }
    }
}
