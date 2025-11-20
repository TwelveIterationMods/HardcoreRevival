package net.blay09.mods.hardcorerevival.config;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.config.reflection.Comment;
import net.blay09.mods.balm.platform.config.reflection.Config;
import net.blay09.mods.balm.platform.config.reflection.NestedType;
import net.blay09.mods.balm.platform.config.reflection.Synced;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Set;

@Config(HardcoreRevival.MOD_ID)
public class HardcoreRevivalConfig {

    @Comment("The time in seconds in which a player can still be rescued from death. Set to 0 to disable the timer.")
    @Synced
    public int secondsUntilDeath = 120;

    @Comment("If true, the timer until death continues even if the player logs out.")
    public boolean continueTimerWhileOffline = false;

    @Comment("The threshold of seconds that knockout will be treated as a consecutive knockout. See resumeTimerOnConsecutiveKnockout and multiplyTimerOnConsecutiveKnockout.")
    public int consecutiveKnockoutThresholdSeconds = 40;

    @Comment("If true, the timer until death resumes from its last time on consecutive knockouts.")
    public boolean resumeTimerOnConsecutiveKnockout = false;

    @Comment("The multiplier to apply to the time remaining on consecutive knockouts.")
    public float multiplyTimerOnConsecutiveKnockout = 1f;

    @Comment("The time in ticks it takes to rescue a player. 20 ticks are one second.")
    @Synced
    public int rescueActionTicks = 40;

    @Comment("The amount of health to respawn with when a player was rescued, out of 20.")
    public int rescueRespawnHealth = 1;

    @Comment("The food level to respawn with when a player was rescued, out of 20.")
    public int rescueRespawnFoodLevel = 5;

    @NestedType(String.class)
    @Comment("Effects applied to a player when rescued, in the format \"effect|duration|amplifier\"")
    public List<String> rescueRespawnEffects = List.of("minecraft:hunger|600|0", "minecraft:weakness|1200|0");

    @Comment("The distance at which a player can rescue another.")
    @Synced
    public double rescueDistance = 3;

    @Comment("If true, knocked out players will glow, making them visible through blocks.")
    public boolean glowOnKnockout = true;

    @Comment("If true, knocked out players are still able to run commands. OPs are always able to run commands.")
    @Synced
    public boolean allowCommands = true;

    @Comment("If true, knocked out players are still able to punch nearby enemies.")
    @Synced
    public boolean allowUnarmedMelee = false;

    @Comment("If true, knocked out players are still able to fire bows.")
    @Synced
    public boolean allowBows = false;

    @Comment("If true, knocked out players are still able to fire pistols from Mr Crayfish's Gun Mod.")
    @Synced
    public boolean allowPistols = false;

    @Comment("Set to false to remove the Accept your Fate button and force players to wait out the timer.")
    @Synced
    public boolean allowAcceptingFate = true;

    @Comment("If true, Hardcore Revival will not be active in singleplayer.")
    public boolean disableInSingleplayer = false;

    @Comment("If true, Hardcore Revival will not be active when playing alone in multiplayer.")
    public boolean disableInLonelyMultiplayer = false;

    @Comment("The damage sources that kill a player instantly, without a K.O. period.")
    @NestedType(Identifier.class)
    public Set<Identifier> instantDeathSources = Set.of(Identifier.withDefaultNamespace("lava"));

    @Comment("The source entity types that kill a player instantly, without a K.O. period.")
    @NestedType(Identifier.class)
    public Set<Identifier> instantDeathEntityTypes = Set.of();

    public static HardcoreRevivalConfig getActive() {
        return Balm.config().getActiveConfig(HardcoreRevivalConfig.class);
    }

    public static void initialize() {
        Balm.config().registerConfig(HardcoreRevivalConfig.class);
    }
}
