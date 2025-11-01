package net.blay09.mods.hardcorerevival.neoforge.compat;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.hardcorerevival.api.PlayerAboutToKnockOutEvent;
import net.blay09.mods.hardcorerevival.tag.ModItemTags;
import net.minecraft.world.item.Items;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosAddon {
    public CuriosAddon() {
        Balm.getEvents().onEvent(PlayerAboutToKnockOutEvent.class, event -> {
            CuriosApi.getCuriosInventory(event.getPlayer()).ifPresent(trinkets -> {
                if (trinkets.isEquipped(itemStack -> itemStack.is(ModItemTags.PASSTHROUGH_DEATH_WHEN_HELD))) {
                    event.setCanceled(true);
                }
            });
        });
    }
}
