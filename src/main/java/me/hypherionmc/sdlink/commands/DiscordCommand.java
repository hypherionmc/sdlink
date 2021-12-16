package me.hypherionmc.sdlink.commands;

import me.hypherionmc.sdlink.SimpleDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DiscordCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (SimpleDiscordLink.events != null && SimpleDiscordLink.events.getModConfig().chatConfig.inviteCommandEnabled) {
            sender.sendMessage(SimpleDiscordLink.events
                    .getModConfig().messageConfig
                    .inviteMessage.replace("%inviteurl%", SimpleDiscordLink.events.getModConfig().general.inviteLink));
            return true;
        }
        return false;
    }

}
