package net.blay09.mods.hardcorerevival;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.fabric.provider.FabricBalmProviders;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.fabricmc.fabric.impl.lookup.ApiLookupImpl;
import net.fabricmc.fabric.impl.lookup.entity.EntityApiLookupImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class FabricHardcoreRevival implements ModInitializer {
    @Override
    public void onInitialize() {
        Balm.initialize(HardcoreRevival.MOD_ID, HardcoreRevival::initialize);

        var providers = ((FabricBalmProviders) Balm.getProviders());
        ResourceLocation identifier = new ResourceLocation(HardcoreRevival.MOD_ID, "player_data");
        providers.registerProvider(identifier, HardcoreRevivalData.class);
        var lookup = EntityApiLookup.get(identifier, HardcoreRevivalData.class, Void.class);
        lookup.registerForType((entity, context) -> ((FabricPlayer) entity).getHardcoreRevivalData(), EntityType.PLAYER);
    }
}
