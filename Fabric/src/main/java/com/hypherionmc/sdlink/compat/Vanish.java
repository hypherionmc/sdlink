package com.hypherionmc.sdlink.compat;

import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.server.ServerEvents;
import com.hypherionmc.sdlink.util.ModUtils;
import me.drex.vanish.api.VanishAPI;
import me.drex.vanish.api.VanishEvents;
import net.minecraft.server.level.ServerPlayer;

public class Vanish {

    public static void register() {
        VanishEvents.VANISH_EVENT.register((serverPlayer, b) -> {
            if (b) {
                if (ServerEvents.getInstance().canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.playerLeave) {
                    String name = ModUtils.resolve(serverPlayer.getDisplayName());
                    DiscordMessage message = new DiscordMessageBuilder(MessageType.JOIN_LEAVE)
                            .message(SDLinkConfig.INSTANCE.messageFormatting.playerLeft.replace("%player%", name))
                            .author(DiscordAuthor.SERVER)
                            .build();

                    message.sendMessage();
                }
            } else {
                if (ServerEvents.getInstance().canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.playerJoin) {
                    String name = ModUtils.resolve(serverPlayer.getDisplayName());
                    DiscordMessage message = new DiscordMessageBuilder(MessageType.JOIN_LEAVE)
                            .message(SDLinkConfig.INSTANCE.messageFormatting.playerJoined.replace("%player%", name))
                            .author(DiscordAuthor.SERVER)
                            .build();

                    message.sendMessage();
                }
            }
        });
    }

    public static boolean isPlayerVanished(ServerPlayer player) {
        return VanishAPI.isVanished(player);
    }
}
