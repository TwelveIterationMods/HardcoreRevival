package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.server.level.ServerPlayer;

public class AcceptFateMessage {

    private boolean dummy;

    public static void handle(ServerPlayer player, AcceptFateMessage message) {
        if (!HardcoreRevivalConfig.getActive().allowAcceptingFate) {
            return;
        }

        HardcoreRevival.getManager().notRescuedInTime(player);
    }

}
