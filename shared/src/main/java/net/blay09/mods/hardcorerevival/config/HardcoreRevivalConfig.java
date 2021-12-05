package net.blay09.mods.hardcorerevival.config;

import net.blay09.mods.balm.api.Balm;

public class HardcoreRevivalConfig {

    public static HardcoreRevivalConfigData getActive() {
        return Balm.getConfig().getActive(HardcoreRevivalConfigData.class);
    }

    public static void initialize() {
        Balm.getConfig().registerConfig(HardcoreRevivalConfigData.class, null);
    }

}
