package com.hypherionmc.sdlink.compat.rolesync.impl;

import com.hypherionmc.craterlib.api.compat.playerroles.BridgedPlayerRoles;
import com.hypherionmc.craterlib.api.compat.playerroles.PlayerRolesCompat;
import com.hypherionmc.craterlib.api.events.compat.PlayerRolesEvents;
import com.hypherionmc.craterlib.api.game.authlib.CraterGameProfile;
import com.hypherionmc.craterlib.api.game.world.entity.player.CraterPlayer;
import com.hypherionmc.craterlib.core.event.annot.CraterEventListener;
import com.hypherionmc.sdlink.api.accounts.DiscordUser;
import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkCompatConfig;
import com.hypherionmc.sdlink.core.config.impl.compat.RoleSyncCompat;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.managers.RoleManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public final class PlayerRolesSync extends AbstractRoleSyncer {

    public static final PlayerRolesSync INSTANCE = new PlayerRolesSync();

    private PlayerRolesSync() {
        super(() -> SDLinkCompatConfig.INSTANCE.common.playerroles && SDLinkCompatConfig.INSTANCE.playerroles.syncToMinecraft);
    }

    @Override
    public void sync(CraterPlayer p, List<Role> roles, Guild guild, Member member) {
        // Discord to Minecraft Sync
        if (SDLinkCompatConfig.INSTANCE.playerroles.syncToMinecraft) {
            // Add Ranks To Users
            for (Role role : roles) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.playerroles.syncs.stream().filter(s -> s.role.equalsIgnoreCase(role.getId())).findFirst();

                sync.ifPresent(s -> {
                    if (!PlayerRolesCompat.INSTANCE.hasRole(p.getGameProfile(), s.rank)) {
                        ignoreEvent = true;
                        PlayerRolesCompat.INSTANCE.addRole(p.getGameProfile(), s.rank);
                        ignoreEvent = false;
                    }
                });
            }

            // Remove Ranks from Users
            List<String> ranks = PlayerRolesCompat.INSTANCE.getRoles(p.getGameProfile());
            for (String rank : ranks) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.playerroles.syncs.stream().filter(s -> s.rank.equalsIgnoreCase(rank)).findFirst();

                sync.ifPresent(s -> {
                    if (roles.stream().noneMatch(r -> r.getId().equalsIgnoreCase(s.role)) && PlayerRolesCompat.INSTANCE.hasRole(p.getGameProfile(), s.rank)) {
                        ignoreEvent = true;
                        PlayerRolesCompat.INSTANCE.removeRole(p.getGameProfile(), s.rank);
                        ignoreEvent = false;
                    }
                });
            }
        }

        // Minecraft to Discord Sync
        if (SDLinkCompatConfig.INSTANCE.playerroles.syncToDiscord) {
            List<String> ranks = PlayerRolesCompat.INSTANCE.getRoles(p.getGameProfile());

            // Add Roles to Users
            for (String rank : ranks) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.playerroles.syncs.stream().filter(s -> s.rank.equalsIgnoreCase(rank)).findFirst();

                sync.ifPresent(s -> {
                    if (roles.stream().noneMatch(r -> r.getId().equalsIgnoreCase(s.role))) {
                        Optional<Role> r = RoleManager.getPlayerRoleRoles().stream().filter(rr -> rr.getId().equalsIgnoreCase(s.role)).findFirst();
                        if (r.isEmpty())
                            return;

                        ignoreEvent = true;
                        guild.addRoleToMember(UserSnowflake.fromId(member.getId()), r.get()).queue(suc -> ignoreEvent = false);
                    }
                });
            }

            // Remove Role from Users
            for (Role role : roles) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.playerroles.syncs.stream().filter(s -> s.role.equalsIgnoreCase(role.getId())).findFirst();

                if (sync.isPresent() && !PlayerRolesCompat.INSTANCE.hasRole(p.getGameProfile(), sync.get().rank)) {
                    Optional<Role> r = RoleManager.getPlayerRoleRoles().stream().filter(rr -> rr.getId().equalsIgnoreCase(sync.get().role)).findFirst();
                    if (r.isEmpty())
                        return;

                    ignoreEvent = true;
                    guild.removeRoleFromMember(UserSnowflake.fromId(member.getId()), r.get()).queue(suc -> ignoreEvent = false);
                }
            }
        }
    }

    @Override
    void discordRoleChanged(Member member, Guild guild, Role role, boolean add, @NonNull List<MinecraftAccount> oldAccount) {
        if (oldAccount.isEmpty()) {
            oldAccount.add(MinecraftAccount.fromDiscordId(member.getId()));
        }

        RoleSyncCompat.Sync sync = SDLinkCompatConfig.INSTANCE.playerroles.syncs.stream().filter(s -> s.role.equalsIgnoreCase(role.getId())).findFirst().orElse(null);
        if (sync == null) return;

        for (MinecraftAccount account : oldAccount) {
            if (add) {
                if (!PlayerRolesCompat.INSTANCE.hasRole(account.toGameProfile(), sync.rank)) {
                    ignoreEvent = true;
                    PlayerRolesCompat.INSTANCE.addRole(account.toGameProfile(), sync.rank);
                    ignoreEvent = false;
                }
            } else {
                if (PlayerRolesCompat.INSTANCE.hasRole(account.toGameProfile(), sync.rank)) {
                    ignoreEvent = true;
                    PlayerRolesCompat.INSTANCE.removeRole(account.toGameProfile(), sync.rank);
                    if (!oldAccount.isEmpty()) {
                        try {
                            guild.removeRoleFromMember(UserSnowflake.fromId(member.getId()), role).queue();
                        } catch (Exception ignored) {}
                    }
                    ignoreEvent = false;
                }
            }
        }
    }

    @CraterEventListener
    public void playerRoleAddedToUser(PlayerRolesEvents.RoleAddedEvent event) {
        if (ignoreEvent)
            return;

        if (!SDLinkCompatConfig.INSTANCE.common.playerroles || !SDLinkCompatConfig.INSTANCE.playerroles.syncToDiscord)
            return;

        updatePlayerRole(event.getProfile(), event.getRole(), true);
    }

    @CraterEventListener
    public void playerRoleRemovedFromUser(PlayerRolesEvents.RoleRemovedEvent event) {
        if (ignoreEvent)
            return;

        if (!SDLinkCompatConfig.INSTANCE.common.playerroles || !SDLinkCompatConfig.INSTANCE.playerroles.syncToDiscord)
            return;

        updatePlayerRole(event.getProfile(), event.getRole(), false);
    }

    private void updatePlayerRole(CraterGameProfile profile, BridgedPlayerRoles rank, boolean add) {
        MinecraftAccount account = MinecraftAccount.of(profile);
        DiscordUser user = account.getDiscordUser();
        if (user == null)
            return;

        Guild g = BotController.INSTANCE.getJDA().getGuilds().get(0);
        if (g == null)
            return;

        Member member = g.getMemberById(user.getUserId());
        if (member == null)
            return;

        Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.playerroles.syncs.stream().filter(s -> s.rank.equalsIgnoreCase(rank.getId())).findFirst();

        sync.ifPresent(s -> {
            Role role = RoleManager.getPlayerRoleRoles().stream().filter(r -> r.getId().equalsIgnoreCase(s.role)).findFirst().orElse(null);
            if (role == null)
                return;

            if (add) {
                ignoreEvent = true;
                g.addRoleToMember(UserSnowflake.fromId(member.getId()), role).queue(suc -> ignoreEvent = false);
            } else {
                ignoreEvent = true;
                g.removeRoleFromMember(UserSnowflake.fromId(member.getId()), role).queue(suc -> ignoreEvent = false);
            }
        });
    }
}
