package net.blay09.mods.hardcorerevival.neoforge.client;

import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = HardcoreRevival.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeHardcoreRevivalClient {

    public NeoForgeHardcoreRevivalClient(ModContainer modContainer, IEventBus eventBus) {
        final var context = new NeoForgeLoadContext(modContainer, eventBus);
        BalmClient.initializeMod(HardcoreRevival.MOD_ID, context, HardcoreRevivalClient::initialize);
    }
}
