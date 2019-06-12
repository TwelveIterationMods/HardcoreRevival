package net.blay09.mods.hardcorerevival;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HardcoreRevivalConfig {

    public static class Common {
        public final ForgeConfigSpec.ConfigValue<Integer> maxDeathTicks;
        public final ForgeConfigSpec.ConfigValue<Integer> rescueTime;
        public final ForgeConfigSpec.ConfigValue<Double> maxRescueDist;
        public final ForgeConfigSpec.BooleanValue glowOnDeath;

        Common(ForgeConfigSpec.Builder builder) {
            maxDeathTicks = builder
                    .comment("The time in ticks in which a player can still be rescued from death.")
                    .translation("hardcorerevival.config.maxDeathTicks")
                    .define("maxDeathTicks", 20 * 60 * 2);

            maxRescueDist = builder
                    .comment("The distance at which a player can rescue another.")
                    .translation("hardcorerevival.config.maxRescueDist")
                    .define("maxRescueDist", 5.0);

            rescueTime = builder
                    .comment("The time in ticks it takes to rescue a player.")
                    .translation("hardcorerevival.config.rescueTime")
                    .define("rescueTime", 40);

            glowOnDeath = builder
                    .comment("If true, knocked out players will glow, making them visible through blocks.")
                    .translation("hardcorerevival.config.glowOnDeath")
                    .define("glowOnDeath", true);
        }
    }

    static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

}
