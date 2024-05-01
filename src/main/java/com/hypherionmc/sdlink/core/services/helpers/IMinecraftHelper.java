/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.services.helpers;

import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.messaging.Result;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author HypherionSA
 * Service to bridge communication between the Library and Minecraft
 */
public interface IMinecraftHelper {

    void discordMessageReceived(Member member, String message);

    Result checkWhitelisting();

    Pair<Integer, Integer> getPlayerCounts();

    List<MinecraftAccount> getOnlinePlayers();

    long getServerUptime();

    String getServerVersion();

    Result executeMinecraftCommand(String command, int permLevel, MessageReceivedEvent event, @Nullable SDLinkAccount account);

    boolean isOnlineMode();

    void banPlayer(MinecraftAccount acc);
}
