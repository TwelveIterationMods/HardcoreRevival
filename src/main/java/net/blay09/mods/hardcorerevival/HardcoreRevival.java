package net.blay09.mods.hardcorerevival;

import net.blay09.mods.hardcorerevival.capability.CapabilityHardcoreRevival;
import net.blay09.mods.hardcorerevival.handler.DeathHandler;
import net.blay09.mods.hardcorerevival.handler.PlayerHandler;
import net.blay09.mods.hardcorerevival.handler.RescueHandler;
import net.blay09.mods.hardcorerevival.handler.RestrictionHandler;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HardcoreRevival.MOD_ID)
@Mod.EventBusSubscriber
public class HardcoreRevival {
    public static final String MOD_ID = "hardcorerevival";

    public HardcoreRevival() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RestrictionHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerHandler());
        MinecraftForge.EVENT_BUS.register(new DeathHandler());
        MinecraftForge.EVENT_BUS.register(new RescueHandler());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HardcoreRevivalConfig.commonSpec);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.init();
            CapabilityHardcoreRevival.register();
        });
    }

}
