package net.blay09.mods.hardcorerevival.config;

import com.google.common.collect.Lists;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.network.HardcoreRevivalConfigMessage;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Mod.EventBusSubscriber(modid = HardcoreRevival.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HardcoreRevivalConfig {

    public static class Common implements IHardcoreRevivalConfig {
        private final ForgeConfigSpec.IntValue ticksUntilDeath;
        private final ForgeConfigSpec.BooleanValue continueTimerWhileOffline;
        private final ForgeConfigSpec.IntValue rescueActionTicks;
        private final ForgeConfigSpec.IntValue rescueRespawnHealth;
        private final ForgeConfigSpec.IntValue rescueRespawnFoodLevel;
//        private final ForgeConfigSpec.DoubleValue rescueRespawnFoodSaturation;
        private final ForgeConfigSpec.ConfigValue<List<? extends String>> rescueRespawnEffects;
        private final ForgeConfigSpec.DoubleValue rescueDistance;
        private final ForgeConfigSpec.BooleanValue glowOnKnockout;
        private final ForgeConfigSpec.BooleanValue allowUnarmedMelee;
        private final ForgeConfigSpec.BooleanValue allowBows;
        private final ForgeConfigSpec.BooleanValue allowPistols;

        Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");

            glowOnKnockout = builder
                    .comment("If true, knocked out players will glow, making them visible through blocks.")
                    .translation("config.hardcorerevival.glowOnKnockout")
                    .define("glowOnKnockout", true);

            ticksUntilDeath = builder
                    .comment("The time in ticks in which a player can still be rescued from death. Set to 0 to disable the timer.")
                    .translation("config.hardcorerevival.ticksUntilDeath")
                    .defineInRange("ticksUntilDeath", 20 * 60 * 2, 0, Integer.MAX_VALUE);

            continueTimerWhileOffline = builder
                    .comment("If true, the timer until death continues even if the player logs out.")
                    .translation("config.hardcorerevival.continueTimerWhileOffline")
                    .define("continueTimerWhileOffline", false);

            builder.pop().push("rescue");

            rescueDistance = builder
                    .comment("The distance at which a player can rescue another.")
                    .translation("config.hardcorerevival.rescueDistance")
                    .defineInRange("rescueDistance", 5.0, 0f, Float.MAX_VALUE);

            rescueActionTicks = builder
                    .comment("The time in ticks it takes to rescue a player.")
                    .translation("config.hardcorerevival.rescueActionTicks")
                    .defineInRange("rescueActionTicks", 40, 0, Integer.MAX_VALUE);

            rescueRespawnHealth = builder
                    .comment("The amount of health to respawn with when a player was rescued, out of 20.")
                    .translation("config.hardcorerevival.rescueRespawnHealth")
                    .defineInRange("rescueRespawnHealth", 1, 1, Integer.MAX_VALUE);

            rescueRespawnFoodLevel = builder
                    .comment("The food level to respawn with when a player was rescued, out of 20.")
                    .translation("config.hardcorerevival.rescueRespawnFoodLevel")
                    .defineInRange("rescueRespawnFoodLevel", 5, 0, Integer.MAX_VALUE);

            /* not yet supported:
            rescueRespawnFoodSaturation = builder
                    .comment("The food saturation to respawn with when a player was rescued.")
                    .translation("config.hardcorerevival.rescueRespawnFoodSaturation")
                    .defineInRange("rescueRespawnFoodSaturation", 0, 0, Float.MAX_VALUE);*/

            rescueRespawnEffects = builder
                    .comment("Effects applied to a player when rescued, in the format \"effect|duration|amplifier\"")
                    .translation("config.hardcorerevival.rescueRespawnEffects")
                    .defineList("rescueRespawnEffects", Lists.newArrayList("minecraft:hunger|600|0", "minecraft:weakness|1200|0"), it -> it instanceof String);

            builder.pop().push("restrictions");

            allowUnarmedMelee = builder
                    .comment("If true, knocked out players are still able to punch nearby enemies.")
                    .translation("config.hardcorerevival.allowUnarmedMelee")
                    .define("allowUnarmedMelee", false);

            allowBows = builder
                    .comment("If true, knocked out players are still able to fire bows.")
                    .translation("config.hardcorerevival.allowBows")
                    .define("allowBows", false);

            allowPistols = builder
                    .comment("If true, knocked out players are still able to fire pistols from Mr Crayfish's Gun Mod.")
                    .translation("config.hardcorerevival.allowPistols")
                    .define("allowPistols", false);
        }

        @Override
        public int getTicksUntilDeath() {
            return ticksUntilDeath.get();
        }

        @Override
        public int getRescueActionTicks() {
            return rescueActionTicks.get();
        }

        @Override
        public int getRescueRespawnHealth() {
            return rescueRespawnHealth.get();
        }

        @Override
        public int getRescueRespawnFoodLevel() {
            return rescueRespawnFoodLevel.get();
        }

        @Override
        public double getRescueRespawnFoodSaturation() {
            return 0f;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> getRescueRespawnEffects() {
            return (List<String>) rescueRespawnEffects.get();
        }

        @Override
        public double getRescueDistance() {
            return rescueDistance.get();
        }

        @Override
        public boolean isGlowOnKnockoutEnabled() {
            return glowOnKnockout.get();
        }

        @Override
        public boolean isUnarmedMeleeAllowedWhileKnockedOut() {
            return allowUnarmedMelee.get();
        }

        @Override
        public boolean areBowsAllowedWhileKnockedOut() {
            return allowBows.get();
        }

        @Override
        public boolean arePistolsAllowedWhileKnockout() {
            return allowPistols.get();
        }

        @Override
        public boolean shouldContinueTimerWhileOffline() {
            return continueTimerWhileOffline.get();
        }
    }

    public static final ForgeConfigSpec commonSpec;
    private static final Common COMMON;
    private static IHardcoreRevivalConfig activeConfig;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
        activeConfig = COMMON;
    }

    public static IHardcoreRevivalConfig getActive() {
        return activeConfig;
    }

    public static IHardcoreRevivalConfig getFallback() {
        return COMMON;
    }

    public static void setActiveConfig(IHardcoreRevivalConfig config) {
        activeConfig = config;
    }

    @SubscribeEvent
    public static void onConfig(ModConfig.Reloading event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            NetworkHandler.channel.send(PacketDistributor.ALL.with(() -> null), getConfigSyncMessage());
        }
    }

    public static HardcoreRevivalConfigMessage getConfigSyncMessage() {
        return new HardcoreRevivalConfigMessage(
                COMMON.getTicksUntilDeath(),
                COMMON.getRescueActionTicks(),
                (float) COMMON.getRescueDistance(),
                COMMON.isUnarmedMeleeAllowedWhileKnockedOut(),
                COMMON.areBowsAllowedWhileKnockedOut(),
                COMMON.arePistolsAllowedWhileKnockout());
    }
}
