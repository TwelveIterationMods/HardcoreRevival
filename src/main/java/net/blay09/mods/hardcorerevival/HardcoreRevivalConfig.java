package net.blay09.mods.hardcorerevival;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HardcoreRevivalConfig {

    public static class Common {
        public final ForgeConfigSpec.ConfigValue<Integer> maxDeathTicks;
        public final ForgeConfigSpec.ConfigValue<Integer> rescueTime;
        public final ForgeConfigSpec.ConfigValue<Integer> rescueRespawnHealth;
        public final ForgeConfigSpec.ConfigValue<Integer> rescueRespawnFoodLevel;
        public final ForgeConfigSpec.ConfigValue<Double> maxRescueDist;
        public final ForgeConfigSpec.BooleanValue glowOnDeath;
        public final ForgeConfigSpec.BooleanValue disableDeathTimer;
        public final ForgeConfigSpec.BooleanValue allowUnarmedMelee;
        public final ForgeConfigSpec.BooleanValue allowBows;

        Common(ForgeConfigSpec.Builder builder) {
            maxDeathTicks = builder
                    .comment("The time in ticks in which a player can still be rescued from death.")
                    .translation("hardcorerevival.config.maxDeathTicks")
                    .define("maxDeathTicks", 20 * 60 * 2);

            disableDeathTimer = builder
                    .comment("Set to true to completely disable the death timer, meaning you will have infinite time to save others.")
                    .translation("hardcorerevival.config.disableDeathTimer")
                    .define("disableDeathTimer", false);

            maxRescueDist = builder
                    .comment("The distance at which a player can rescue another.")
                    .translation("hardcorerevival.config.maxRescueDist")
                    .define("maxRescueDist", 5.0);

            rescueTime = builder
                    .comment("The time in ticks it takes to rescue a player.")
                    .translation("hardcorerevival.config.rescueTime")
                    .define("rescueTime", 40);

            rescueRespawnHealth = builder
                    .comment("The amount of health to respawn with when a player was rescued, out of 20.")
                    .translation("hardcorerevival.config.rescueRespawnHealth")
                    .define("rescueRespawnHealth", 1);

            rescueRespawnFoodLevel = builder
                    .comment("The food level to respawn with when a player was rescued, out of 20.")
                    .translation("hardcorerevival.config.rescueRespawnFoodLevel")
                    .define("rescueRespawnFoodLevel", 5);

            glowOnDeath = builder
                    .comment("If true, knocked out players will glow, making them visible through blocks.")
                    .translation("hardcorerevival.config.glowOnDeath")
                    .define("glowOnDeath", true);

            allowUnarmedMelee = builder
                    .comment("If true, knocked out players are still able to punch nearby enemies.")
                    .translation("hardcorerevival.config.allowUnarmedMelee")
                    .define("allowUnarmedMelee", false);

            allowBows = builder
                    .comment("If true, knocked out players are still able to fire bows.")
                    .translation("hardcorerevival.config.allowBows")
                    .define("allowBows", false);
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
