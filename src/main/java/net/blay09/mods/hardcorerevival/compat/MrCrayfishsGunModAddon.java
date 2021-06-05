package net.blay09.mods.hardcorerevival.compat;

import com.mrcrayfish.guns.event.GunFireEvent;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

public class MrCrayfishsGunModAddon {
    private final ResourceLocation PISTOL = new ResourceLocation("cgm:pistol");

    public MrCrayfishsGunModAddon() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGunFirePre(GunFireEvent.Pre event) {
        if (HardcoreRevival.getRevivalData(event.getPlayer()).isKnockedOut()) {
            boolean isFiringPistol = Objects.equals(event.getPlayer().getHeldItemMainhand().getItem().getRegistryName(), PISTOL);
            if (isFiringPistol && HardcoreRevivalConfig.getActive().arePistolsAllowedWhileKnockout()) {
                return;
            }

            event.setCanceled(true);
        }
    }
}
