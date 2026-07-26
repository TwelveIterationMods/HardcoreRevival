package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true)
    private void cancelTeleportTo(ServerLevel level, double x, double y, double z, Set<RelativeMovement> relatives, float newYRot, float newXRot, CallbackInfoReturnable<Boolean> ci) {
        final var player = (ServerPlayer) (Object) this;
        if (MixinHooks.shouldCancelTeleport(player)) {
            ci.setReturnValue(false);
        }
    }
}
