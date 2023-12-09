package net.blay09.mods.hardcorerevival;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.balm.neoforge.provider.NeoForgeBalmProviders;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataImpl;
import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.capabilities.*;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nullable;

@Mod(HardcoreRevival.MOD_ID)
public class NeoForgeHardcoreRevival {

    private final Capability<HardcoreRevivalData> hardcoreRevivalDataCapability = CapabilityManager.get(new CapabilityToken<>() {
    });

    public NeoForgeHardcoreRevival() {
        Balm.initialize(HardcoreRevival.MOD_ID, HardcoreRevival::initialize);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> BalmClient.initialize(HardcoreRevival.MOD_ID, HardcoreRevivalClient::initialize));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);
        NeoForge.EVENT_BUS.addGenericListener(Entity.class, this::attachEntityCapabilities);

        NeoForgeBalmProviders providers = (NeoForgeBalmProviders) Balm.getProviders();
        providers.register(HardcoreRevivalData.class, new CapabilityToken<>() {
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(HardcoreRevivalData.class);
    }

    private void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(HardcoreRevival.MOD_ID, "entity_data"), new ICapabilityProvider() {
                private LazyOptional<HardcoreRevivalData> revival;

                private LazyOptional<HardcoreRevivalData> getRevivalCapabilityInstance() {
                    if (revival == null) {
                        revival = LazyOptional.of(HardcoreRevivalDataImpl::new);
                    }

                    return revival;
                }

                @Override
                public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction facing) {
                    return hardcoreRevivalDataCapability.orEmpty(cap, getRevivalCapabilityInstance());
                }
            });
        }
    }
}
