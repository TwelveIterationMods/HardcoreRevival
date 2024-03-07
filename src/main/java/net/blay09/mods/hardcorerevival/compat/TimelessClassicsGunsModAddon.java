package net.blay09.mods.hardcorerevival.compat;

import com.tac.guns.event.GunFireEvent;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TimelessClassicsGunsModAddon {
    public TimelessClassicsGunsModAddon() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGunFirePre(GunFireEvent.Pre event) {
        if (HardcoreRevival.getRevivalData(event.getPlayer()).isKnockedOut()) {
            event.setCanceled(true);
        }
    }
}
