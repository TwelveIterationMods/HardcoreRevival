package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CPlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetHandler.class)
public class ServerPlayNetHandlerMixin {

    @Inject(method = "processPlayer(Lnet/minecraft/network/play/client/CPlayerPacket;)V", at = @At("HEAD"), cancellable = true)
    public void processPlayer(CPlayerPacket packet, CallbackInfo ci) {
        ServerPlayNetHandler netHandler = (ServerPlayNetHandler) (Object) this;
        if (MixinHooks.shouldCancelMovement(netHandler.player)) {
            MixinHooks.handleProcessPlayerRotation(netHandler.player, packet);
            ci.cancel();
        }
    }

}
