package net.blay09.mods.hardcorerevival;

import net.blay09.mods.balm.api.Balm;
import net.fabricmc.api.ModInitializer;

public class FabricHardcoreRevival implements ModInitializer {
    @Override
    public void onInitialize() {
        Balm.initialize(HardcoreRevival.MOD_ID, HardcoreRevival::initialize);
    }
}
