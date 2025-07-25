package net.blay09.mods.hardcorerevival.client;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class HardcoreRevivalClientManager extends HardcoreRevivalManager {

    @Override
    public HardcoreRevivalData getRevivalData(Player player) {
        if (Minecraft.getInstance().player == player) {
            return HardcoreRevival.getClientRevivalData();
        }

        return super.getRevivalData(player);
    }

}
