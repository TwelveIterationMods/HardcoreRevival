package net.blay09.mods.hardcorerevival.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;

public class ReviveCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("revive")
                .requires((source) -> source.hasPermissionLevel(2))
                .executes((source) -> reviveEntities(source.getSource(), ImmutableList.of(source.getSource().assertIsEntity()), false))
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes((source) -> reviveEntities(source.getSource(), EntityArgument.getEntities(source, "targets"), false))
                        .then(Commands.argument("skipEffects", BoolArgumentType.bool())
                                .executes((source) -> reviveEntities(source.getSource(), EntityArgument.getEntities(source, "targets"), BoolArgumentType.getBool(source, "skipEffects"))))));
    }

    private static int reviveEntities(CommandSource source, Collection<? extends Entity> targets, boolean skipEffects) {
        for (Entity entity : targets) {
            if (entity instanceof PlayerEntity) {
                HardcoreRevival.getManager().wakeup(((PlayerEntity) entity), !skipEffects);
            }
        }

        if (targets.size() == 1) {
            source.sendFeedback(new TranslationTextComponent("commands.revive.success.single", targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendFeedback(new TranslationTextComponent("commands.revive.success.multiple", targets.size()), true);
        }

        return targets.size();
    }
}
