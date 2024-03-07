package net.blay09.mods.hardcorerevival;

import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataCapability;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataImpl;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.capability.InvalidHardcoreRevivalData;
import net.blay09.mods.hardcorerevival.command.ReviveCommand;
import net.blay09.mods.hardcorerevival.compat.Compat;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(HardcoreRevival.MOD_ID)
public class HardcoreRevival {
    public static final String MOD_ID = "hardcorerevival";

    private static final HardcoreRevivalManager manager = new HardcoreRevivalManager();
    private static final HardcoreRevivalData clientRevivalData = new HardcoreRevivalDataImpl();

    public static final Logger logger = LogManager.getLogger();

    public HardcoreRevival() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HardcoreRevivalConfig.commonSpec);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.init();
            HardcoreRevivalDataCapability.register();

            if (ModList.get().isLoaded(Compat.MR_CRAYFISHS_GUN_MOD)) {
                try {
                    Class.forName("net.blay09.mods.hardcorerevival.compat.MrCrayfishsGunModAddon").newInstance();
                } catch (Throwable e) {
                    logger.warn("Could not load MrCrayfish's Gun Mod compatibility for Hardcore Revival");
                }
            }
            if (ModList.get().isLoaded(Compat.TIMELESS_CLASSICS_GUNS_MOD)) {
                try {
                    Class.forName("net.blay09.mods.hardcorerevival.compat.TimelessClassicsGunsModAddon").newInstance();
                } catch (Throwable e) {
                    logger.warn("Could not load Timeless Classics Guns Mod compatibility for Hardcore Revival");
                }
            }
        });
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ReviveCommand.register(event.getDispatcher());
    }

    public static HardcoreRevivalManager getManager() {
        return manager;
    }

    public static HardcoreRevivalData getRevivalData(Entity entity) {
        return entity instanceof PlayerEntity ? manager.getRevivalData(((PlayerEntity) entity)) : InvalidHardcoreRevivalData.INSTANCE;
    }

    public static HardcoreRevivalData getClientRevivalData() {
        return clientRevivalData;
    }

}
