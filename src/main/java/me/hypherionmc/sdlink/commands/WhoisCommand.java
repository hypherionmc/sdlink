package me.hypherionmc.sdlink.commands;

import me.hypherionmc.sdlink.SimpleDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public class WhoisCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission(Permission.DEFAULT_PERMISSION.name())) {
            if (SimpleDiscordLink.events != null && SimpleDiscordLink.events.getBotEngine() != null) {
                String username = args[0];
                String value = SimpleDiscordLink.events.getBotEngine().getDiscordName(username);
                sender.sendMessage(value);
                return true;
            }
        }
        return false;
    }
}
