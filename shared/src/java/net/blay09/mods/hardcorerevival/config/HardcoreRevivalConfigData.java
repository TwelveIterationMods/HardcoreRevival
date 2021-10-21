package net.blay09.mods.hardcorerevival.config;

import com.google.common.collect.Lists;
import me.shedaniel.autoconfig.annotation.Config;
import net.blay09.mods.balm.api.config.BalmConfigData;
import net.blay09.mods.balm.api.config.Comment;
import net.blay09.mods.balm.api.config.Synced;
import net.blay09.mods.hardcorerevival.HardcoreRevival;

import java.util.List;

@Config(name = HardcoreRevival.MOD_ID)
public class HardcoreRevivalConfigData implements BalmConfigData {

    @Comment("The time in ticks in which a player can still be rescued from death. Set to 0 to disable the timer.")
    @Synced
    public int ticksUntilDeath = 20 * 60 * 2;

    @Comment("If true, the timer until death continues even if the player logs out.")
    public boolean continueTimerWhileOffline = false;

    @Comment("The time in ticks it takes to rescue a player.")
    @Synced
    public int rescueActionTicks = 40;

    @Comment("The amount of health to respawn with when a player was rescued, out of 20.")
    public int rescueRespawnHealth = 1;

    @Comment("The food level to respawn with when a player was rescued, out of 20.")
    public int rescueRespawnFoodLevel = 5;

    @Comment("Effects applied to a player when rescued, in the format \"effect|duration|amplifier\"")
    public List<String> rescueRespawnEffects = Lists.newArrayList("minecraft:hunger|600|0", "minecraft:weakness|1200|0");

    @Comment("The distance at which a player can rescue another.")
    @Synced
    public double rescueDistance = 5;

    @Comment("If true, knocked out players will glow, making them visible through blocks.")
    public boolean glowOnKnockout = true;

    @Comment("If true, knocked out players are still able to punch nearby enemies.")
    @Synced
    public boolean allowUnarmedMelee = false;

    @Comment("If true, knocked out players are still able to fire bows.")
    @Synced
    public boolean allowBows = false;

    @Comment("If true, knocked out players are still able to fire pistols from Mr Crayfish's Gun Mod.")
    @Synced
    public boolean allowPistols = false;

}
