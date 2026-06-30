package net.blay09.mods.hardcorerevival.fabric.datagen;

import net.blay09.mods.hardcorerevival.tag.ModDamageTypeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeTagProvider extends FabricTagsProvider<DamageType> {
    public ModDamageTypeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        builder(ModDamageTypeTags.BYPASSES_KNOCKOUT)
                .add(DamageTypes.FELL_OUT_OF_WORLD)
                .addOptional(ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("contagion", "infection")));
    }
}
