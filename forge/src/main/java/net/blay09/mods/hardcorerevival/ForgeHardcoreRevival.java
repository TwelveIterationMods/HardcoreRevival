package net.blay09.mods.hardcorerevival;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.balm.forge.ForgeLoadContext;
import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(HardcoreRevival.MOD_ID)
public class ForgeHardcoreRevival {

    public ForgeHardcoreRevival(FMLJavaModLoadingContext context) {
        final var loadContext = new ForgeLoadContext(context.getModBusGroup());
        Balm.initializeMod(HardcoreRevival.MOD_ID, loadContext, HardcoreRevival::initialize);
        if (FMLEnvironment.dist.isClient()) {
            BalmClient.initializeMod(HardcoreRevival.MOD_ID, loadContext, HardcoreRevivalClient::initialize);
        }
    }

}
