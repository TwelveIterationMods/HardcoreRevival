package net.blay09.mods.hardcorerevival.compat;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.hardcorerevival.api.PlayerAboutToKnockOutEvent;
import net.blay09.mods.hardcorerevival.tag.ModItemTags;
import net.minecraft.world.item.Items;

public class InventoryTotemAddon {
    public InventoryTotemAddon() {
        Balm.getEvents().onEvent(PlayerAboutToKnockOutEvent.class, event -> {
            final var player = event.getPlayer();
            final var inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                final var itemStack = inventory.getItem(i);
                if (itemStack.is(ModItemTags.PASSTHROUGH_DEATH_WHEN_HELD)) {
                    event.setCanceled(true);
                }
            }
        });
    }
}
