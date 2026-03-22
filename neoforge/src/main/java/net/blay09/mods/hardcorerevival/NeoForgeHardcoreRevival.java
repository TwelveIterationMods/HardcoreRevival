package net.blay09.mods.hardcorerevival;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(HardcoreRevival.MOD_ID)
public class NeoForgeHardcoreRevival {

    public NeoForgeHardcoreRevival(ModContainer modContainer, IEventBus eventBus) {
        final var context = new NeoForgeLoadContext(modContainer, eventBus);
        Balm.initializeMod(HardcoreRevival.MOD_ID, context, HardcoreRevival::initialize);
    }

}
