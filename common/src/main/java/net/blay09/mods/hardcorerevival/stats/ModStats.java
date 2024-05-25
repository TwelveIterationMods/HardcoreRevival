package net.blay09.mods.hardcorerevival.stats;

import net.blay09.mods.balm.api.stats.BalmStats;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.resources.ResourceLocation;

public class ModStats {

    public static final ResourceLocation knockouts = new ResourceLocation(HardcoreRevival.MOD_ID, "knockouts");

    public static void initialize(BalmStats stats) {
        stats.registerCustomStat(knockouts);
    }
}
