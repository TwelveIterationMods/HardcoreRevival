package net.blay09.mods.hardcorerevival.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import net.blay09.mods.balm.api.command.BalmCommands;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class KnockoutCommand {

    private static final ResourceLocation PERMISSION_KNOCKOUT = new ResourceLocation("hardcorerevival", "command.hardcorerevival.knockout");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        BalmCommands.registerPermission(PERMISSION_KNOCKOUT, Commands.LEVEL_GAMEMASTERS);

        dispatcher.register(Commands.literal("knockout")
                .requires(BalmCommands.requirePermission(PERMISSION_KNOCKOUT))
                .executes((source) -> knockoutPlayers(source.getSource(), ImmutableList.of(source.getSource().getPlayerOrException())))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes((source) -> knockoutPlayers(source.getSource(), EntityArgument.getPlayers(source, "targets")))));
    }

    private static int knockoutPlayers(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (final var player : targets) {
            HardcoreRevival.getManager().knockout(player, player.damageSources().generic());
            player.setHealth(1f);
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.knockout.success.single", targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.knockout.success.multiple", targets.size()), true);
        }

        return targets.size();
    }
}
