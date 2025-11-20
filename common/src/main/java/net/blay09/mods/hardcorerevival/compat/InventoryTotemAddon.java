package net.blay09.mods.hardcorerevival.compat;

import net.blay09.mods.hardcorerevival.api.PlayerAboutToKnockOutEvent;
import net.minecraft.core.component.DataComponents;

public class InventoryTotemAddon {
    public InventoryTotemAddon() {
        PlayerAboutToKnockOutEvent.EVENT.register(event -> {
            final var player = event.player();
            final var inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                final var itemStack = inventory.getItem(i);
                final var deathProtection = itemStack.get(DataComponents.DEATH_PROTECTION);
                if (deathProtection != null) {
                    event.setCanceled(true);
                }
            }
        });
    }
}
