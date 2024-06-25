/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.hooks;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.core.services.SDLinkPlatform;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

        Set<Long> roles = event.getMember().getRoles().stream().map(ISnowflake::getIdLong).collect(Collectors.toSet());
        roles.add(event.getMember().getIdLong());
        roles.add(0L);

        sdlinkDatabase.reloadCollection("verifiedaccounts");
        List<SDLinkAccount> accounts = sdlinkDatabase.findAll(SDLinkAccount.class);
        Optional<SDLinkAccount> account = accounts.stream().filter(u -> u.getDiscordID() != null && u.getDiscordID().equals(event.getMember().getId())).findFirst();

        int permLevel = SDLinkConfig.INSTANCE.linkedCommands.permissions.stream().filter(r -> roles.contains(Long.parseLong(r.role))).map(r -> r.permissionLevel).max(Integer::compareTo).orElse(-1);
        List<String> commands = SDLinkConfig.INSTANCE.linkedCommands.permissions.stream().filter(c -> roles.contains(Long.parseLong(c.role))).flatMap(c -> c.commands.stream()).filter(s -> !s.isEmpty()).toList();

        String raw = event.getMessage().getContentRaw().substring(SDLinkConfig.INSTANCE.linkedCommands.prefix.length());

        if (permLevel == -1) {
            event.getMessage().reply("Sorry, you don't have permission to execute that command").mentionRepliedUser(false).queue(suc -> {
                event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                suc.delete().queueAfter(5, TimeUnit.SECONDS);
            });
            return;
        }

        if (commands.stream().anyMatch(raw::startsWith)) {
            Result res = SDLinkPlatform.minecraftHelper.executeMinecraftCommand(raw, Integer.MAX_VALUE, event, account.orElse(null));
            event.getMessage().reply(res.getMessage()).mentionRepliedUser(false).queue(s -> s.delete().queueAfter(5, TimeUnit.SECONDS));
            event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
        } else {
            Result res = SDLinkPlatform.minecraftHelper.executeMinecraftCommand(raw, permLevel, event, account.orElse(null));
            event.getMessage().reply(res.getMessage()).mentionRepliedUser(false).queue(s -> s.delete().queueAfter(5, TimeUnit.SECONDS));
            event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
        }
    }
}
