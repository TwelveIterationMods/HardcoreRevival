package net.blay09.mods.hardcorerevival.mixin;

import net.blay09.mods.hardcorerevival.MixinHooks;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
    public void drop(boolean all, CallbackInfoReturnable<Boolean> cir) {
        final var player = (Player) (Object) this;
        if (all && MixinHooks.shouldCancelTossAll(player)) {
            cir.setReturnValue(false);
        } else if (!all && MixinHooks.shouldCancelToss(player, player.getInventory().getSelected())) {
            cir.setReturnValue(false);
        }
    }
}
