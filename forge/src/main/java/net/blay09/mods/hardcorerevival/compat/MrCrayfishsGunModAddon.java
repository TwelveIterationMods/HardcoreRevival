package net.blay09.mods.hardcorerevival.compat;

import com.mrcrayfish.guns.event.GunFireEvent;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.util.Objects;

public class MrCrayfishsGunModAddon {
    private final Identifier PISTOL = Identifier.fromNamespaceAndPath("cgm", "pistol");

    public MrCrayfishsGunModAddon() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public boolean onGunFirePre(GunFireEvent.Pre event) {
        if (PlayerHardcoreRevivalManager.isKnockedOut(event.getEntity())) {
            Identifier mainHandItemKey = BuiltInRegistries.ITEM.getKey(event.getEntity().getMainHandItem().getItem());
            boolean isFiringPistol = Objects.equals(mainHandItemKey, PISTOL);
            return !isFiringPistol || !HardcoreRevivalConfig.getActive().allowPistols;
        }
        return false;
    }
}
