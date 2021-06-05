package net.blay09.mods.hardcorerevival.config;

import java.util.List;

public interface IHardcoreRevivalConfig {
    int getTicksUntilDeath();
    int getRescueActionTicks();
    int getRescueRespawnHealth();
    int getRescueRespawnFoodLevel();
    double getRescueRespawnFoodSaturation();
    List<String> getRescueRespawnEffects();
    double getRescueDistance();
    boolean isGlowOnKnockoutEnabled();
    boolean isUnarmedMeleeAllowedWhileKnockedOut();
    boolean areBowsAllowedWhileKnockedOut();
    boolean arePistolsAllowedWhileKnockout();
}
