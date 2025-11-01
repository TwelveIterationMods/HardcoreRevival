package net.blay09.mods.hardcorerevival.tag;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> PASSTHROUGH_DEATH_WHEN_HELD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "passthrough_death_when_held"));
    public static final TagKey<Item> ALLOW_ATTACK_WHILE_KNOCKED_OUT = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "allow_attack_while_knocked_out"));
    public static final TagKey<Item> ALLOW_USE_WHILE_KNOCKED_OUT = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "allow_use_while_knocked_out"));
    public static final TagKey<Item> ALLOW_TOSS_WHILE_KNOCKED_OUT = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "allow_toss_while_knocked_out"));
}
