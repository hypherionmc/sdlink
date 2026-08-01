package com.hypherionmc.sdlink.core.discord.commands.slash.general;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.util.SDLinkUtils;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlinkrw.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlinkrw.modules.database.SDLinkAccount;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author SuperficialCake
 * Staff command to resync Discord with Minecraft
 */
public class SyncCommand extends SDLinkSlashCommand {

    public SyncCommand() {
        super(true);

        this.name = "sync";
        this.help = SDText.translate("command.sync.help").toString();
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event){
        event.deferReply(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();

        if (!SDLinkConfig.INSTANCE.accessControl.enabled) {
            event.getHook().sendMessage(SDText.translate("command.sync.access_control_disabled").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            return;
        }

        try {
            List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.findAll(SDLinkAccount.class).stream().filter(account -> !SDLinkUtils.isNullOrEmpty(account.getDiscordID())).toList();

            if (accounts.isEmpty()) {
                event.getHook().sendMessage(SDText.translate("command.verifiedaccounts.no_accounts").toString()).setEphemeral(true).queue();
                return;
            }

            for (SDLinkAccount itm : accounts) {
                Member discordMember = event.getGuild().getMemberById(itm.getDiscordID());

                if (discordMember.getNickname() == null || discordMember.getNickname() != itm.getInGameName()) {
                    MinecraftAccount minecraftAccount = MinecraftAccount.of(itm);
                    minecraftAccount.verifyAccount(discordMember);
                }

                List<String> memberRoles = discordMember.getRoles().stream()
                        .map(Role::getId)
                        .toList();

                if (!memberRoles.containsAll(SDLinkConfig.INSTANCE.accessControl.verifiedRole)) {
                    MinecraftAccount minecraftAccount = MinecraftAccount.of(itm);
                    minecraftAccount.verifyAccount(discordMember);
                }
            }

            event.getHook().sendMessage(SDText.translate("command.sync.synced").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();

        } catch (Exception e){
            event.getHook().sendMessage(SDText.translate("error.command_failed").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            SDLinkConstants.LOGGER.error("Failed to run sync command", e);
        }
    }
}
