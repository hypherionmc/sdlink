package com.hypherionmc.sdlink.core.discord.hooks;

import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.TriggerCommandsConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DiscordRoleHooks {

    public static final DiscordRoleHooks INSTANCE = new DiscordRoleHooks();

    public void onRoleAdded(@NotNull GuildMemberRoleAddEvent event) {
        Optional<SDLinkAccount> account = DatabaseManager.INSTANCE.getCollection(SDLinkAccount.class)
                .stream()
                .filter(a -> a.getDiscordID() != null && a.getDiscordID().equals(event.getMember().getId()))
                .findFirst();

        runCommandChecks(event.getRoles(), SDLinkConfig.INSTANCE.triggerCommands.roleAdded, "roleAdded", account.map(List::of).orElseGet(List::of));
    }

    public void onRoleRemoved(@NotNull GuildMemberRoleRemoveEvent event) {
        List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.getCollection(SDLinkAccount.class)
                .stream()
                .filter(a -> a.getDiscordID() != null && a.getDiscordID().equals(event.getMember().getId()))
                .toList();

        onRoleRemoved(event.getRoles(), accounts);
    }

    public void onRoleRemoved(@NotNull List<Role> roles, @NotNull List<SDLinkAccount> account) {
        runCommandChecks(roles, SDLinkConfig.INSTANCE.triggerCommands.roleRemoved, "roleRemoved", account);
    }

    private void runCommandChecks(List<Role> roles, List<TriggerCommandsConfig.TriggerHolder> triggers, String section, List<SDLinkAccount> accounts) {
        if (!(SDLinkConfig.INSTANCE.accessControl.enabled || SDLinkConfig.INSTANCE.accessControl.optionalVerification) || !SDLinkConfig.INSTANCE.triggerCommands.enabled)
            return;

        try {
            if (accounts.isEmpty())
                return;

            accounts.forEach(acc -> {
                MinecraftAccount mcAccount = MinecraftAccount.of(acc);

                for (Role role : roles) {
                    Optional<TriggerCommandsConfig.TriggerHolder> triggerHolder = triggers.stream().filter(r -> r.discordRole.equalsIgnoreCase(role.getName()) || r.discordRole.equalsIgnoreCase(role.getId())).findFirst();
                    if (triggerHolder.isEmpty())
                        continue;

                    triggerHolder.get().minecraftCommand.forEach(cmd -> executeCommand(
                            cmd.replace("%player%", mcAccount.getUsername())
                                    .replace("%role%", role.getName())
                                    .replace("%ingamename%", acc.getInGameName())
                                    .replace("%rolenospaces%", role.getName().replace(" ", "")))
                    );
                }
            });
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to run {} trigger", section, e);
        }
    }

    private static void executeCommand(String command) {
        CompletableFuture<Result> result = new CompletableFuture<>();
        SDLinkMCPlatform.INSTANCE.executeCommand(command, 4, "", result);

        result.thenAccept(res -> {
           if (res.isError()) {
               BotController.INSTANCE.getLogger().error("Failed to trigger command {}: {}", command, res.getMessage());
           }
        });
    }
}
