package net.blay09.mods.hardcorerevival.mixin;

import net.minecraft.client.gui.IngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IngameGui.class)
public interface IngameGuiAccessor {

    @Accessor
    void setPlayerHealth(int playerHealth);
}
