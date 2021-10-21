package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class HardcoreRevivalDataMessage {
    private final int entityId;
    private final boolean knockedOut;
    private final int knockoutTicksPassed;
    private final boolean beingRescued;

    public HardcoreRevivalDataMessage(int entityId, boolean knockedOut, int knockoutTicksPassed, boolean beingRescued) {
        this.entityId = entityId;
        this.knockedOut = knockedOut;
        this.knockoutTicksPassed = knockoutTicksPassed;
        this.beingRescued = beingRescued;
    }

    public static void encode(HardcoreRevivalDataMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
        buf.writeBoolean(message.knockedOut);
        buf.writeInt(message.knockoutTicksPassed);
        buf.writeBoolean(message.beingRescued);
    }

    public static HardcoreRevivalDataMessage decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        boolean knockedOut = buf.readBoolean();
        int knockoutTicksPassed = buf.readInt();
        boolean beingRescued = buf.readBoolean();
        return new HardcoreRevivalDataMessage(entityId, knockedOut, knockoutTicksPassed, beingRescued);
    }

    public static void handle(Player player, HardcoreRevivalDataMessage message) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                Entity entity = mc.level.getEntity(message.entityId);
                if (entity != null) {
                    HardcoreRevivalData revivalData = entity.getId() == mc.player.getId() ? HardcoreRevival.getClientRevivalData() : HardcoreRevival.getRevivalData(entity);
                    revivalData.setKnockedOut(message.knockedOut);
                    revivalData.setKnockoutTicksPassed(message.knockoutTicksPassed);
                    HardcoreRevivalClient.setBeingRescued(message.beingRescued);
                }
            }
        });
    }
}
