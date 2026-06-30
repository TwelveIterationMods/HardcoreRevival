package net.blay09.mods.hardcorerevival.tag;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypeTags {
    public static final TagKey<DamageType> BYPASSES_KNOCKOUT = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(HardcoreRevival.MOD_ID, "bypasses_knockout"));
}
