/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.hooks;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MinecraftCommands;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.core.services.SDLinkPlatform;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hypherionmc.sdlink.core.managers.DatabaseManager.sdlinkDatabase;

public class MinecraftCommandHook {

    public static void discordMessageEvent(MessageReceivedEvent event) {
        if (!SDLinkConfig.INSTANCE.linkedCommands.enabled || SDLinkConfig.INSTANCE.linkedCommands.permissions.isEmpty())
            return;

        if (!event.getMessage().getContentRaw().startsWith(SDLinkConfig.INSTANCE.linkedCommands.prefix))
            return;

        if (event.getMessage().getContentRaw().equalsIgnoreCase(SDLinkConfig.INSTANCE.linkedCommands.prefix))
            return;

        if (!SDLinkConfig.INSTANCE.linkedCommands.allowedChannels.isEmpty() && SDLinkConfig.INSTANCE.linkedCommands.allowedChannels.stream().noneMatch(c -> c.equals(event.getChannel().getId()))) {
            event.getMessage().reply("Sorry, minecraft commands are not allowed in this channel").mentionRepliedUser(false).queue(s -> s.delete().queueAfter(5, TimeUnit.SECONDS));
            event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }

        LinkedHashSet<Long> roles = new LinkedHashSet<>();
        roles.add(event.getMember().getIdLong());
        roles.addAll(event.getMember().getRoles().stream().sorted((r1, r2) -> Long.compare(r2.getPositionRaw(), r1.getPositionRaw())).map(ISnowflake::getIdLong).collect(Collectors.toSet()));

        sdlinkDatabase.reloadCollection("verifiedaccounts");
        List<SDLinkAccount> accounts = sdlinkDatabase.findAll(SDLinkAccount.class);
        Optional<SDLinkAccount> account = accounts.stream().filter(u -> u.getDiscordID() != null && u.getDiscordID().equals(event.getMember().getId())).findFirst();

        MinecraftCommands.Command allowedCommand = null;
        for (long roleId : roles) {
            var firstMatch = SDLinkConfig.INSTANCE.linkedCommands.permissions.stream().filter(r -> Long.parseLong(r.role) == roleId).findFirst();
            if (firstMatch.isPresent()) {
                allowedCommand = firstMatch.get();
                break;
            }
        }

        if (allowedCommand == null) {
            allowedCommand = SDLinkConfig.INSTANCE.linkedCommands.permissions.stream().filter(r -> r.role.equals("0")).findFirst().orElse(null);
        }

        if (allowedCommand == null) {
            event.getMessage().reply("Sorry, you do not have permission to execute that command").mentionRepliedUser(false).queue(suc -> suc.delete().queueAfter(5, TimeUnit.SECONDS));
            event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }

        String rawCommand = event.getMessage().getContentRaw().substring(SDLinkConfig.INSTANCE.linkedCommands.prefix.length());

        if (allowedCommand.commands.isEmpty()) {
            executeCommand(rawCommand, allowedCommand.permissionLevel, event, account.orElse(null));
            return;
        }

        if (allowedCommand.commands.stream().anyMatch(rawCommand::startsWith)) {
            executeCommand(rawCommand, Integer.MAX_VALUE, event, account.orElse(null));
            return;
        }

        event.getMessage().reply("Sorry, but you are not allowed to execute that command").mentionRepliedUser(false).queue(suc -> {
            suc.delete().queueAfter(5, TimeUnit.SECONDS);
            event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
        });
    }

    private static void executeCommand(String command, int permLevel, MessageReceivedEvent event, SDLinkAccount account) {
        Result res = SDLinkPlatform.minecraftHelper.executeMinecraftCommand(command, permLevel, event, account);
        event.getMessage().reply(res.getMessage())
                .mentionRepliedUser(false)
                .queue(s -> s.delete().queueAfter(5, TimeUnit.SECONDS));
        event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
    }
}
