package net.blay09.mods.hardcorerevival.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

import static net.blay09.mods.hardcorerevival.HardcoreRevival.id;

public class ModDamageTypeTags {
    public static final TagKey<DamageType> BYPASSES_KNOCKOUT = TagKey.create(Registries.DAMAGE_TYPE, id("bypasses_knockout"));
}
