package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(method = "dropItem(Lnet/minecraft/client/player/LocalPlayer;Z)V", at = @At("HEAD"), cancellable = true)
    public void drop(LocalPlayer player, boolean all, CallbackInfo ci) {
        if (all && MixinHooks.shouldCancelTossAll(player)) {
            ci.cancel();
        } else if (!all && MixinHooks.shouldCancelToss(player, player.getInventory().getSelectedItem())) {
            ci.cancel();
        }
    }
}
