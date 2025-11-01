package net.blay09.mods.hardcorerevival.fabric.compat;

import dev.emi.trinkets.api.TrinketsApi;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.hardcorerevival.api.PlayerAboutToKnockOutEvent;
import net.blay09.mods.hardcorerevival.tag.ModItemTags;
import net.minecraft.world.item.Items;

public class TrinketsAddon {
    public TrinketsAddon() {
        Balm.getEvents().onEvent(PlayerAboutToKnockOutEvent.class, event -> {
            TrinketsApi.getTrinketComponent(event.getPlayer()).ifPresent(trinkets -> {
                if (trinkets.isEquipped(itemStack -> itemStack.is(ModItemTags.PASSTHROUGH_DEATH_WHEN_HELD))) {
                    event.setCanceled(true);
                }
            });
        });
    }
}
