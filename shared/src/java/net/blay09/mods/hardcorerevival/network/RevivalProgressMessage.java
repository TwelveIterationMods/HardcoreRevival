package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class RevivalProgressMessage {
    private final int entityId;
    private final float progress;

    public RevivalProgressMessage(int entityId, float progress) {
        this.entityId = entityId;
        this.progress = progress;
    }

    public static void encode(RevivalProgressMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
        buf.writeFloat(message.progress);
    }

    public static RevivalProgressMessage decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        float progress = buf.readFloat();
        return new RevivalProgressMessage(entityId, progress);
    }

    public static void handle(Player player, RevivalProgressMessage message) {
        HardcoreRevivalClient.setRevivalProgress(message.entityId, message.progress);
    }
}
