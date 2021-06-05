package net.blay09.mods.hardcorerevival;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class HardcoreRevivalConfig { // TODO sync to client

    public static class Common {
        public final ForgeConfigSpec.IntValue ticksUntilDeath;
        public final ForgeConfigSpec.IntValue rescueActionTicks;
        public final ForgeConfigSpec.IntValue rescueRespawnHealth;
        public final ForgeConfigSpec.IntValue rescueRespawnFoodLevel;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> rescueRespawnEffects;
        public final ForgeConfigSpec.DoubleValue rescueDistance;
        public final ForgeConfigSpec.BooleanValue glowOnDeath;
        public final ForgeConfigSpec.BooleanValue allowUnarmedMelee;
        public final ForgeConfigSpec.BooleanValue allowBows;
        public final ForgeConfigSpec.BooleanValue allowPistols;

        Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");

            glowOnDeath = builder
                    .comment("If true, knocked out players will glow, making them visible through blocks.")
                    .translation("hardcorerevival.config.glowOnDeath")
                    .define("glowOnDeath", true);

            ticksUntilDeath = builder
                    .comment("The time in ticks in which a player can still be rescued from death. Set to 0 to disable the timer.")
                    .translation("hardcorerevival.config.ticksUntilDeath")
                    .defineInRange("ticksUntilDeath", 20 * 60 * 2, 0, Integer.MAX_VALUE);

            builder.pop().push("rescue");

            rescueDistance = builder
                    .comment("The distance at which a player can rescue another.")
                    .translation("hardcorerevival.config.rescueDistance")
                    .defineInRange("rescueDistance", 5.0, 0f, Float.MAX_VALUE);

            rescueActionTicks = builder
                    .comment("The time in ticks it takes to rescue a player.")
                    .translation("hardcorerevival.config.rescueActionTicks")
                    .defineInRange("rescueActionTicks", 40, 0, Integer.MAX_VALUE);

            rescueRespawnHealth = builder
                    .comment("The amount of health to respawn with when a player was rescued, out of 20.")
                    .translation("hardcorerevival.config.rescueRespawnHealth")
                    .defineInRange("rescueRespawnHealth", 1, 1, Integer.MAX_VALUE);

            rescueRespawnFoodLevel = builder
                    .comment("The food level to respawn with when a player was rescued, out of 20.")
                    .translation("hardcorerevival.config.rescueRespawnFoodLevel")
                    .defineInRange("rescueRespawnFoodLevel", 5, 0, Integer.MAX_VALUE);

            rescueRespawnEffects = builder
                    .comment("Effects applied to a player when rescued, in the format \"effect|duration|amplifier\"")
                    .translation("config.waystones.rescueRespawnEffects")
                    .defineList("rescueRespawnEffects", Lists.newArrayList("minecraft:hunger|600|0", "minecraft:weakness|1200|0"), it -> it instanceof String);

            builder.pop().push("restrictions");

            allowUnarmedMelee = builder
                    .comment("If true, knocked out players are still able to punch nearby enemies.")
                    .translation("hardcorerevival.config.allowUnarmedMelee")
                    .define("allowUnarmedMelee", false);

            allowBows = builder
                    .comment("If true, knocked out players are still able to fire bows.")
                    .translation("hardcorerevival.config.allowBows")
                    .define("allowBows", false);

            allowPistols = builder
                    .comment("If true, knocked out players are still able to fire pistols from Mr Crayfish's Gun Mod.")
                    .translation("hardcorerevival.config.allowPistols")
                    .define("allowPistols", false);
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
