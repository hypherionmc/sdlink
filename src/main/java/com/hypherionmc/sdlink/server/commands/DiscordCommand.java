package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.util.SDLinkChatUtils;

public class DiscordCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("discord")
                .requiresPermission(0)
                .executes(ctx -> {
                    if (SDLinkConfig.INSTANCE.botConfig.invite.inviteLink != null && !SDLinkConfig.INSTANCE.botConfig.invite.inviteLink.isEmpty()) {

                        String invite = SDLinkConfig.INSTANCE.botConfig.invite.inviteMessage
                                .replace("%inviteurl%", SDLinkConfig.INSTANCE.botConfig.invite.inviteLink);

                        ctx.sendSuccess(() -> SDLinkChatUtils.parseChatLinks(invite), false);
                    }
                });

        event.registerCommand(cmd);
    }

}