package net.blay09.mods.hardcorerevival;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class HardcoreRevivalConfig { // TODO sync to client

    public static class Common {
        public final ForgeConfigSpec.ConfigValue<Integer> ticksUntilDeath;
        public final ForgeConfigSpec.ConfigValue<Integer> rescueActionTicks;
        public final ForgeConfigSpec.ConfigValue<Integer> rescueRespawnHealth;
        public final ForgeConfigSpec.ConfigValue<Integer> rescueRespawnFoodLevel;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> rescueRespawnEffects;
        public final ForgeConfigSpec.ConfigValue<Double> rescueDistance;
        public final ForgeConfigSpec.BooleanValue glowOnDeath;
        public final ForgeConfigSpec.BooleanValue allowUnarmedMelee;
        public final ForgeConfigSpec.BooleanValue allowBows;

        Common(ForgeConfigSpec.Builder builder) {
            ticksUntilDeath = builder
                    .comment("The time in ticks in which a player can still be rescued from death. Set to 0 to disable the timer.")
                    .translation("hardcorerevival.config.ticksUntilDeath")
                    .defineInRange("ticksUntilDeath", 20 * 60 * 2, 0, Integer.MAX_VALUE);

            rescueDistance = builder
                    .comment("The distance at which a player can rescue another.")
                    .translation("hardcorerevival.config.rescueDistance")
                    .define("rescueDistance", 5.0);

            rescueActionTicks = builder
                    .comment("The time in ticks it takes to rescue a player.")
                    .translation("hardcorerevival.config.rescueActionTicks")
                    .define("rescueActionTicks", 40);

            rescueRespawnHealth = builder
                    .comment("The amount of health to respawn with when a player was rescued, out of 20.")
                    .translation("hardcorerevival.config.rescueRespawnHealth")
                    .define("rescueRespawnHealth", 1);

            rescueRespawnFoodLevel = builder
                    .comment("The food level to respawn with when a player was rescued, out of 20.")
                    .translation("hardcorerevival.config.rescueRespawnFoodLevel")
                    .define("rescueRespawnFoodLevel", 5);

            rescueRespawnEffects = builder
                    .comment("Effects applied to a player when rescued, in the format \"effect|duration|amplifier\"")
                    .translation("config.waystones.rescueRespawnEffects")
                    .defineList("rescueRespawnEffects", Lists.newArrayList("minecraft:hunger|600|0", "minecraft:weakness|1200|0"), it -> it instanceof String);

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
