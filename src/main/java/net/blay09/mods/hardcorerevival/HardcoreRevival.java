package net.blay09.mods.hardcorerevival;

import net.blay09.mods.hardcorerevival.capability.CapabilityHardcoreRevival;
import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.blay09.mods.hardcorerevival.handler.DeathHandler;
import net.blay09.mods.hardcorerevival.handler.PlayerHandler;
import net.blay09.mods.hardcorerevival.handler.RescueHandler;
import net.blay09.mods.hardcorerevival.handler.RestrictionHandler;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.util.DamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Optional;

@Mod(HardcoreRevival.MOD_ID)
@Mod.EventBusSubscriber
public class HardcoreRevival {
    public static final String MOD_ID = "hardcorerevival";

    public static final DamageSource notRescuedInTime = new DamageSource("not_rescued_in_time");

    public static Optional<HardcoreRevivalClient> client = Optional.empty();

    public HardcoreRevival() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RestrictionHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerHandler());
        MinecraftForge.EVENT_BUS.register(new DeathHandler());
        MinecraftForge.EVENT_BUS.register(new RescueHandler());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HardcoreRevivalConfig.commonSpec);
    }

    private void setup(FMLCommonSetupEvent event) {
        DeferredWorkQueue.runLater(() -> {
            NetworkHandler.init();
            CapabilityHardcoreRevival.register();
        });
    }

    private void setupClient(FMLClientSetupEvent event) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            client = Optional.of(new HardcoreRevivalClient());
            DeferredWorkQueue.runLater(() -> client.ifPresent(MinecraftForge.EVENT_BUS::register));
        });
    }

}
