package net.blay09.mods.hardcorerevival;

import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataCapability;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(HardcoreRevival.MOD_ID)
public class ForgeHardcoreRevival {

    public ForgeHardcoreRevival() {
        HardcoreRevivalDataCapability.register();

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> HardcoreRevival::initialize);
    }

}
