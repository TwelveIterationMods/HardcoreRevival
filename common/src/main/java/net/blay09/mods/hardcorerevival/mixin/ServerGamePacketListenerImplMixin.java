package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Inject(method = "handlePlayerAction(Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = Shift.AFTER), cancellable = true)
    public void handlePlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        final var netHandler = (ServerGamePacketListenerImpl) (Object) this;
        switch (packet.getAction()) {
            case DROP_ALL_ITEMS:
                if (MixinHooks.shouldCancelTossAll(netHandler.player)) {
                    ci.cancel();
                }
                break;
            case DROP_ITEM:
                if (MixinHooks.shouldCancelToss(netHandler.player, netHandler.player.getInventory().getSelected())) {
                    ci.cancel();
                }
                break;
        }
    }

}
