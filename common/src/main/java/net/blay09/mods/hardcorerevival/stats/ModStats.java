package net.blay09.mods.hardcorerevival.stats;

import net.blay09.mods.balm.stats.BalmCustomStatRegistrar;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;

public class ModStats {

    public static final Identifier knockouts = Identifier.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "knockouts");
    public static final Identifier timesRescued = Identifier.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "times_rescued");
    public static final Identifier playersRevived = Identifier.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "players_revived");

    public static void initialize(BalmCustomStatRegistrar stats) {
        stats.register(knockouts, StatFormatter.DEFAULT);
        stats.register(timesRescued, StatFormatter.DEFAULT);
        stats.register(playersRevived, StatFormatter.DEFAULT);
    }

}
