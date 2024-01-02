package net.blay09.mods.hardcorerevival.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class InvalidHardcoreRevivalData implements HardcoreRevivalData {

    public static final HardcoreRevivalData INSTANCE = new InvalidHardcoreRevivalData();

    @Override
    public void setKnockedOut(boolean knockedOut) {
    }

    @Override
    public boolean isKnockedOut() {
        return false;
    }

    @Override
    public void setKnockoutTicksPassed(int knockoutTicksPassed) {
    }

    @Override
    public int getKnockoutTicksPassed() {
        return 0;
    }

    @Override
    public void setLastKnockoutTicksPassed(int lastKnockoutTicksPassed) {
    }

    @Override
    public int getLastKnockoutTicksPassed() {
        return 0;
    }

    @Override
    public void setLastRescuedAt(long lastRescuedAt) {
    }

    @Override
    public long getLastRescuedAt() {
        return 0;
    }

    @Override
    public void setLastKnockoutAt(long lastKnockoutAt) {
    }

    @Override
    public long getLastKnockoutAt() {
        return 0;
    }

    @Override
    public void setLastLogoutAt(long lastLogoutAt) {
    }

    @Override
    public long getLastLogoutAt() {
        return 0;
    }

    @Override
    public void setRescueTime(int rescueTime) {
    }

    @Override
    public int getRescueTime() {
        return 0;
    }

    @Override
    public void setRescueTarget(@Nullable Player rescueTarget) {
    }

    @Nullable
    @Override
    public Player getRescueTarget() {
        return null;
    }

    @Override
    public CompoundTag serialize() {
        return new CompoundTag();
    }

    @Override
    public void deserialize(CompoundTag tag) {
    }
}
