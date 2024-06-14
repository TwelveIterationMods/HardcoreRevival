package net.blay09.mods.hardcorerevival.client;

import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.balm.neoforge.NeoForgeLoadContext;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = HardcoreRevival.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeHardcoreRevivalClient {

    public NeoForgeHardcoreRevivalClient(IEventBus eventBus) {
        final var context = new NeoForgeLoadContext(eventBus);
        BalmClient.initialize(HardcoreRevival.MOD_ID, context, HardcoreRevivalClient::initialize);
    }
}
