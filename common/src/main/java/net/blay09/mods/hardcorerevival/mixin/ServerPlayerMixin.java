package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"), cancellable = true)
    private void cancelTeleport(TeleportTransition transition, CallbackInfoReturnable<@Nullable ServerPlayer> ci) {
        final var player = (ServerPlayer) (Object) this;
        if (MixinHooks.shouldCancelTeleport(player)) {
            ci.setReturnValue(null);
        }
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z", at = @At("HEAD"), cancellable = true)
    private void cancelTeleportTo(ServerLevel level, double x, double y, double z, Set<?> relatives, float newYRot, float newXRot, boolean resetCamera, CallbackInfoReturnable<Boolean> ci) {
        final var player = (ServerPlayer) (Object) this;
        if (MixinHooks.shouldCancelTeleport(player)) {
            ci.setReturnValue(false);
        }
    }
}
