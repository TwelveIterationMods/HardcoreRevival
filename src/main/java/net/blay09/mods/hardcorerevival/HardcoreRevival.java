package net.blay09.mods.hardcorerevival;

import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataCapability;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataImpl;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.capability.InvalidHardcoreRevivalData;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HardcoreRevival.MOD_ID)
@Mod.EventBusSubscriber
public class HardcoreRevival {
    public static final String MOD_ID = "hardcorerevival";

    private static final HardcoreRevivalManager manager = new HardcoreRevivalManager();
    private static final HardcoreRevivalData clientRevivalData = new HardcoreRevivalDataImpl();

    public HardcoreRevival() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HardcoreRevivalConfig.commonSpec);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.init();
            HardcoreRevivalDataCapability.register();
        });
    }

    public static HardcoreRevivalManager getManager() {
        return manager;
    }

    public static HardcoreRevivalData getRevivalData(Entity entity) {
        if (entity.world.isRemote) {
            return clientRevivalData;
        }

        return entity instanceof PlayerEntity ? manager.getRevivalData(((PlayerEntity) entity)) : InvalidHardcoreRevivalData.INSTANCE;
    }

    public static HardcoreRevivalData getClientRevivalData() {
        return clientRevivalData;
    }

}
