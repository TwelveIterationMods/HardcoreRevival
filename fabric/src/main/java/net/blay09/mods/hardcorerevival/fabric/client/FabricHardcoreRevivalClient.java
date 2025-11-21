package net.blay09.mods.hardcorerevival.fabric.client;

import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.fabricmc.api.ClientModInitializer;

public class FabricHardcoreRevivalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BalmClient.initializeMod(HardcoreRevival.MOD_ID, FabricLoadContext.INSTANCE, HardcoreRevivalClient::initialize);
    }
}
