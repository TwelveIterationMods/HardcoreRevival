package net.blay09.mods.hardcorerevival.capability;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface HardcoreRevivalData {
    void setKnockedOut(boolean knockedOut);

    boolean isKnockedOut();

    void setKnockoutTicksPassed(int knockoutTicksPassed);

    int getKnockoutTicksPassed();

    void setKnockoutAttackerId(@Nullable UUID knockoutAttackerId);

    @Nullable
    UUID getKnockoutAttackerId();

    void setLogoutWorldTime(long logoutWorldTime);

    long getLogoutWorldTime();

    void setRescueTime(int rescueTime);

    int getRescueTime();

    void setRescueTargetId(@Nullable UUID rescueTargetId);

    @Nullable
    UUID getRescueTargetId();

    CompoundTag serialize();

    void deserialize(CompoundTag tag);
}
