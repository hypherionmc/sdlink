package com.hypherionmc.sdlink.core.discord.commands.slash.hide;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.core.managers.HiddenPlayersManager;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class UnhidePlayerCommand extends SDLinkSlashCommand {

    public UnhidePlayerCommand() {
        super(true);
        this.name = "unhideplayer";
        this.help = "Make a discord user visible";

        this.options = new ArrayList<>() {{
            add(new OptionData(OptionType.USER, "user", "The user to make visible").setRequired(true));
            add(new OptionData(OptionType.BOOLEAN, "minecraft", "Unhide the user in minecraft if they have a linked account").setRequired(false));
        }};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
        User user = event.getOption("user").getAsUser();
        boolean mc = event.hasOption("minecraft") && event.getOption("minecraft").getAsBoolean();

        if (mc) {
            if (!SDLinkConfig.INSTANCE.accessControl.enabled) {
                event.getHook().editOriginal("The minecraft option cannot be used when access control is disabled").queue();
                return;
            }

            List<SDLinkAccount> accounts = DatabaseManager.sdlinkDatabase.getCollection(SDLinkAccount.class).stream().filter(a -> a.getDiscordID() != null && a.getDiscordID().equalsIgnoreCase(user.getId())).toList();
            if (accounts.isEmpty()) {
                event.getHook().editOriginal("Cannot find linked minecraft account for user " + user.getAsMention()).queue();
            } else {
                for (SDLinkAccount account : accounts) {
                    HiddenPlayersManager.INSTANCE.unhidePlayer(account.getUuid());
                }
            }
        }

        Result res = HiddenPlayersManager.INSTANCE.unhidePlayer(user.getId());
        event.getHook().editOriginal(res.getMessage()).queue();
    }

}
