package net.blay09.mods.hardcorerevival.mixin;

import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerEntityAccessor {

    @Accessor
    void setRespawnInvulnerabilityTicks(int respawnInvulnerabilityTicks);

}
