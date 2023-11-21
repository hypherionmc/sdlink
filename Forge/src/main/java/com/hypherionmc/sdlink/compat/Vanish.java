package com.hypherionmc.sdlink.compat;

import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.server.ServerEvents;
import com.hypherionmc.sdlink.util.ModUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import redstonedubstep.mods.vanishmod.api.PlayerVanishEvent;

public class Vanish {

    public Vanish() {

    }

    @SubscribeEvent
    public void vanishevent(PlayerVanishEvent event) {
        if (event.isVanished()) {
            if (ServerEvents.getInstance().canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.playerLeave) {
                String name = ModUtils.resolve(event.getEntity().getDisplayName());
                DiscordMessage message = new DiscordMessageBuilder(MessageType.LEAVE)
                        .message(SDLinkConfig.INSTANCE.messageFormatting.playerLeft.replace("%player%", name))
                        .author(DiscordAuthor.SERVER)
                        .build();

                message.sendMessage();
            }
        } else {
            if (ServerEvents.getInstance().canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.playerJoin) {
                String name = ModUtils.resolve(event.getEntity().getDisplayName());
                DiscordMessage message = new DiscordMessageBuilder(MessageType.JOIN)
                        .message(SDLinkConfig.INSTANCE.messageFormatting.playerJoined.replace("%player%", name))
                        .author(DiscordAuthor.SERVER)
                        .build();

                message.sendMessage();
            }
        }
    }

}
