package com.hypherionmc.sdlink.core.discord.commands.slash.general;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlinkrw.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlinkrw.modules.database.SDLinkAccount;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

/**
 * @Author SuperficialCake
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

        try {
            List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.findAll(SDLinkAccount.class);

            if (accounts.isEmpty()) {
                event.getHook().sendMessage(SDText.translate("command.verifiedaccounts.no_accounts").toString()).setEphemeral(true).queue();
                return;
            }

            for (SDLinkAccount itm : accounts) {
                Member discordMember = null;

                if (itm.getDiscordID() != null && !itm.getDiscordID().isBlank()){
                    discordMember = event.getGuild().getMemberById(itm.getDiscordID());
                }

                if (discordMember.getNickname() == null || discordMember.getNickname() != itm.getInGameName() && SDLinkConfig.INSTANCE.accessControl.enabled) {
                    MinecraftAccount minecraftAccount = MinecraftAccount.of(itm);
                    minecraftAccount.verifyAccount(discordMember);
                }

                if (discordMember.getRoles().contains(SDLinkConfig.INSTANCE.accessControl.verifiedRole.toString()) && SDLinkConfig.INSTANCE.accessControl.enabled) {
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
