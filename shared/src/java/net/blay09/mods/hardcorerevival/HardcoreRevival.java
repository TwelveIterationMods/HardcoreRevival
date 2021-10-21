package net.blay09.mods.hardcorerevival;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataImpl;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.capability.InvalidHardcoreRevivalData;
import net.blay09.mods.hardcorerevival.command.ReviveCommand;
import net.blay09.mods.hardcorerevival.compat.Compat;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.ModNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HardcoreRevival {
    public static final String MOD_ID = "hardcorerevival";

    private static final HardcoreRevivalManager manager = new HardcoreRevivalManager();
    private static final HardcoreRevivalData clientRevivalData = new HardcoreRevivalDataImpl();

    public static final Logger logger = LogManager.getLogger();

    public static void initialize() {
        HardcoreRevivalConfig.initialize();

        ModNetworking.initialize(Balm.getNetworking());

        Balm.getCommands().register(ReviveCommand::register);

        Balm.initializeIfLoaded(Compat.MR_CRAYFISHS_GUN_MOD, "net.blay09.mods.hardcorerevival.compat.MrCrayfishsGunModAddon");
    }

    public static HardcoreRevivalManager getManager() {
        return manager;
    }

    public static HardcoreRevivalData getRevivalData(Entity entity) {
        return entity instanceof Player player ? manager.getRevivalData(player) : InvalidHardcoreRevivalData.INSTANCE;
    }

    public static HardcoreRevivalData getClientRevivalData() {
        return clientRevivalData;
    }

}
