package net.blay09.mods.firstaid;

import net.minecraftforge.common.config.Config;

@Config(modid = FirstAid.MOD_ID)
public class ModConfig {

	@Config.Name("Max Death Ticks")
	@Config.Comment("The time in ticks in which a player can still be rescued from death.")
	public static int maxDeathTicks = 20 * 60 * 2;

	@Config.Name("Team Up Compatibility")
	@Config.Comment("This makes Team Up's chat features work on the death screen as well.")
	public static boolean teamUpIntegration = true;

	@Config.Name("Ping Compatibility")
	@Config.Comment("This adds a 'Send Ping' button to the death screen.")
	public static boolean pingIntegration = true;

	@Config.Name("Send Ping on Death")
	@Config.Comment("Requires Ping Compatibility to be enabled. Automatically sends a ping upon your death.")
	public static boolean autoPingOnDeath = true;

	@Config.Name("Max Rescue Distance")
	@Config.Comment("The distance at which a player can rescue another.")
	public static float maxRescueDist = 5f;

	@Config.Name("Rescue Ticks")
	@Config.Comment("The time in ticks it takes to rescue a player.")
	public static int rescueTime = 40;

	// These would be cool but they'd require coremod hacks and it's not worth it:

//	@Config.Name("Allow Unarmed Melee when knocked out")
//	@Config.Comment("Whether knocked out players can still perform unarmed melee attacks.")
//	public static boolean allowUnarmedMelee = true;
//
//	@Config.Name("Allow Bows when knocked out")
//	@Config.Comment("Whether knocked out players can still fire arrows.")
//	public static boolean allowBows = true;
}
