package net.blay09.mods.hardcorerevival.stats;

import net.blay09.mods.balm.api.stats.BalmStats;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.resources.ResourceLocation;

public class ModStats {

    public static final ResourceLocation knockouts = ResourceLocation.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "knockouts");

    public static final ResourceLocation timesRescued = ResourceLocation.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "times_rescued");

    public static final ResourceLocation playersRevived = ResourceLocation.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "players_revived");

    public static void initialize(BalmStats stats) {
        stats.registerCustomStat(knockouts);
        stats.registerCustomStat(timesRescued);
        stats.registerCustomStat(playersRevived);

    }

}
