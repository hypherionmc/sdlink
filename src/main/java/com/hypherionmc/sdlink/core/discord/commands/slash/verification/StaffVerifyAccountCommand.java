/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.commands.slash.verification;

import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;

public class StaffVerifyAccountCommand extends SDLinkSlashCommand {

    public StaffVerifyAccountCommand() {
        super(true);
        this.name = "staffverify";
        this.help = "Allow staff members verify minecraft players, without verification";

        this.options = new ArrayList<>() {{
            add(new OptionData(OptionType.USER, "discorduser", "The discord user the minecraft account belongs to").setRequired(true));
            add(new OptionData(OptionType.STRING, "mcname", "The minecraft account to link to the user").setRequired(true));
        }};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();

        String mcname = event.getOption("mcname").getAsString();
        User user = event.getOption("discorduser").getAsUser();

        Member member = event.getGuild().getMemberById(user.getId());

        if (member == null) {
            event.getHook().sendMessage(user.getEffectiveName() + " is not a member of this discord server").setEphemeral(true).queue();
            return;
        }

        MinecraftAccount minecraftAccount = MinecraftAccount.of(mcname);

        Result result = minecraftAccount.verifyAccount(member, event.getGuild());
        event.getHook().sendMessage(result.getMessage()).setEphemeral(true).queue();
    }

}
