package net.blay09.mods.hardcorerevival.tag;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
    public static final TagKey<Block> ALLOW_USE_WHILE_KNOCKED_OUT = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "allow_use_while_knocked_out"));
    public static final TagKey<Block> ALLOW_BREAK_WHILE_KNOCKED_OUT = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "allow_break_while_knocked_out"));
}
