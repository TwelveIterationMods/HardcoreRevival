package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.config.IHardcoreRevivalConfig;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class HardcoreRevivalConfigMessage implements IHardcoreRevivalConfig {
    private final int ticksUntilDeath;
    private final int rescueActionTicks;
    private final float rescueDistance;
    private final boolean allowUnarmedMelee;
    private final boolean allowBows;
    private final boolean allowPistols;

    public HardcoreRevivalConfigMessage(int ticksUntilDeath, int rescueActionTicks, float rescueDistance, boolean allowUnarmedMelee, boolean allowBows, boolean allowPistols) {
        this.ticksUntilDeath = ticksUntilDeath;
        this.rescueActionTicks = rescueActionTicks;
        this.rescueDistance = rescueDistance;
        this.allowUnarmedMelee = allowUnarmedMelee;
        this.allowBows = allowBows;
        this.allowPistols = allowPistols;
    }

    public static void encode(HardcoreRevivalConfigMessage message, PacketBuffer buf) {
        buf.writeInt(message.ticksUntilDeath);
        buf.writeInt(message.rescueActionTicks);
        buf.writeFloat(message.rescueDistance);
        buf.writeBoolean(message.allowUnarmedMelee);
        buf.writeBoolean(message.allowBows);
        buf.writeBoolean(message.allowPistols);
    }

    public static HardcoreRevivalConfigMessage decode(PacketBuffer buf) {
        int ticksUntilDeath = buf.readInt();
        int rescueActionTicks = buf.readInt();
        float rescueDistance = buf.readFloat();
        boolean allowUnarmedMelee = buf.readBoolean();
        boolean allowBows = buf.readBoolean();
        boolean allowPistols = buf.readBoolean();
        return new HardcoreRevivalConfigMessage(ticksUntilDeath, rescueActionTicks, rescueDistance, allowUnarmedMelee, allowBows, allowPistols);
    }

    public static void handle(HardcoreRevivalConfigMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        NetworkHandler.ensureClientSide(context);

        context.enqueueWork(() -> {
            HardcoreRevivalConfig.setActiveConfig(message);
        });

        context.setPacketHandled(true);
    }

    @Override
    public int getTicksUntilDeath() {
        return ticksUntilDeath;
    }

    @Override
    public int getRescueActionTicks() {
        return rescueActionTicks;
    }

    @Override
    public int getRescueRespawnHealth() {
        return HardcoreRevivalConfig.getFallback().getRescueRespawnHealth();
    }

    @Override
    public int getRescueRespawnFoodLevel() {
        return HardcoreRevivalConfig.getFallback().getRescueRespawnFoodLevel();
    }

    @Override
    public double getRescueRespawnFoodSaturation() {
        return HardcoreRevivalConfig.getFallback().getRescueRespawnFoodSaturation();
    }

    @Override
    public List<String> getRescueRespawnEffects() {
        return HardcoreRevivalConfig.getFallback().getRescueRespawnEffects();
    }

    @Override
    public double getRescueDistance() {
        return rescueDistance;
    }

    @Override
    public boolean isGlowOnKnockoutEnabled() {
        return HardcoreRevivalConfig.getFallback().isGlowOnKnockoutEnabled();
    }

    @Override
    public boolean isUnarmedMeleeAllowedWhileKnockedOut() {
        return allowUnarmedMelee;
    }

    @Override
    public boolean areBowsAllowedWhileKnockedOut() {
        return allowBows;
    }

    @Override
    public boolean arePistolsAllowedWhileKnockout() {
        return allowPistols;
    }

    @Override
    public boolean shouldContinueTimerWhileOffline() {
        return HardcoreRevivalConfig.getFallback().shouldContinueTimerWhileOffline();
    }
}
