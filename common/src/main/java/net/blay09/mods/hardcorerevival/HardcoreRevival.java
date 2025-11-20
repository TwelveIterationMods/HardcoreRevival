package net.blay09.mods.hardcorerevival;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.core.BalmRegistrars;
import net.blay09.mods.hardcorerevival.command.ReviveCommand;
import net.blay09.mods.hardcorerevival.compat.Compat;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.handler.*;
import net.blay09.mods.hardcorerevival.network.ModNetworking;
import net.blay09.mods.hardcorerevival.stats.ModStats;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HardcoreRevival {
    public static final String MOD_ID = "hardcorerevival";

    public static final Logger logger = LogManager.getLogger();

    public static void initialize(BalmRegistrars registrars) {
        HardcoreRevivalConfig.initialize();

        ModNetworking.initialize(Balm.networking());
        registrars.customStats(ModStats::initialize);

        Balm.commands().register(ReviveCommand::register);

        KnockoutHandler.initialize();
        KnockoutSyncHandler.initialize();
        KnockoutRestrictionHandler.initialize();
        LoginLogoutHandler.initialize();
        RescueHandler.initialize();

        Balm.initializeIfLoaded(Compat.MR_CRAYFISHS_GUN_MOD, "net.blay09.mods.hardcorerevival.compat.MrCrayfishsGunModAddon");
        Balm.initializeIfLoaded(Compat.INVENTORY_TOTEM, "net.blay09.mods.hardcorerevival.compat.InventoryTotemAddon");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
