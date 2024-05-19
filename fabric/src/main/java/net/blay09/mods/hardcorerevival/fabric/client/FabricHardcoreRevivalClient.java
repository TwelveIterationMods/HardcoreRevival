package net.blay09.mods.hardcorerevival.fabric.client;

import net.blay09.mods.balm.api.EmptyLoadContext;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.fabricmc.api.ClientModInitializer;

public class FabricHardcoreRevivalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BalmClient.initialize(HardcoreRevival.MOD_ID, EmptyLoadContext.INSTANCE, HardcoreRevivalClient::initialize);
    }
}
