package com.hypherionmc.sdlink.compat.rolesync.impl;

import com.hypherionmc.craterlib.api.events.compat.LuckPermsCompatEvents;
import com.hypherionmc.craterlib.compat.LuckPermsCompat;
import com.hypherionmc.craterlib.core.event.annot.CraterEventListener;
import com.hypherionmc.craterlib.nojang.authlib.BridgedGameProfile;
import com.hypherionmc.craterlib.nojang.world.entity.player.BridgedPlayer;
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class LuckPermsSync extends AbstractRoleSyncer {

    public static final LuckPermsSync INSTANCE = new LuckPermsSync();

    private LuckPermsSync() {
        super(() -> SDLinkCompatConfig.INSTANCE.common.luckperms && SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncToMinecraft);
    }

    @Override
    public void sync(BridgedPlayer p, List<Role> roles, Guild guild, Member member) {

        // Discord to Minecraft Sync
        if (SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncToMinecraft) {
            // Add Ranks To Users
            for (Role role : roles) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncs.stream().filter(s -> s.role.equalsIgnoreCase(role.getId())).findFirst();

                sync.ifPresent(s -> {
                    if (!LuckPermsCompat.INSTANCE.hasGroup(p.getUUID(), s.rank)) {
                        ignoreEvent = true;
                        LuckPermsCompat.INSTANCE.addGroupToUser(p.getUUID(), s.rank);
                        ignoreEvent = false;
                    }

                });
            }

            // Remove Ranks from Users
            Set<String> ranks = LuckPermsCompat.INSTANCE.getUserGroups(p.getUUID());
            for (String rank : ranks) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncs.stream().filter(s -> s.rank.equalsIgnoreCase(rank)).findFirst();

                sync.ifPresent(s -> {
                    if (roles.stream().noneMatch(r -> r.getId().equalsIgnoreCase(s.role)) && LuckPermsCompat.INSTANCE.hasGroup(p.getUUID(), s.rank)) {
                        ignoreEvent = true;
                        LuckPermsCompat.INSTANCE.removeGroupFromUser(p.getUUID(), s.rank);
                        ignoreEvent = false;
                    }
                });
            }
        }

        // Minecraft to Discord Sync
        if (SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncToDiscord) {
            Set<String> ranks = LuckPermsCompat.INSTANCE.getUserGroups(p.getUUID());

            // Add Roles to Users
            for (String rank : ranks) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncs.stream().filter(s -> s.rank.equalsIgnoreCase(rank)).findFirst();

                sync.ifPresent(s -> {
                    if (roles.stream().noneMatch(r -> r.getId().equalsIgnoreCase(s.role))) {
                        Optional<Role> r = RoleManager.getLuckPermsRoles().stream().filter(rr -> rr.getId().equalsIgnoreCase(s.role)).findFirst();
                        if (r.isEmpty())
                            return;

                        ignoreEvent = true;
                        guild.addRoleToMember(UserSnowflake.fromId(member.getId()), r.get()).queue(suc -> ignoreEvent = false);
                    }
                });
            }

            // Remove Role from Users
            for (Role role : roles) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncs.stream().filter(s -> s.role.equalsIgnoreCase(role.getId())).findFirst();

                if (sync.isPresent() && !LuckPermsCompat.INSTANCE.hasGroup(p.getUUID(), sync.get().rank)) {
                    Optional<Role> r = RoleManager.getLuckPermsRoles().stream().filter(rr -> rr.getId().equalsIgnoreCase(sync.get().role)).findFirst();
                    if (r.isEmpty())
                        return;

                    ignoreEvent = true;
                    guild.removeRoleFromMember(UserSnowflake.fromId(member.getId()), r.get()).queue(suc -> ignoreEvent = false);
                }
            }
        }
    }

    @CraterEventListener
    public void luckPermsGroupAddedToUser(LuckPermsCompatEvents.GroupAddedEvent event) {
        if (ignoreEvent)
            return;

        if (!SDLinkCompatConfig.INSTANCE.common.luckperms || !SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncToDiscord)
            return;

        // TODO REMOVE THIS TEMPORARY FIX ON NEXT CRATERLIB RELEASE
        String identifier = null;

        try {
            Field identifierField = event.getClass().getDeclaredField("identifier");
            identifierField.setAccessible(true);
            identifier = identifierField.get(event).toString();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (identifier == null)
            return;

        updateLuckpermsGroup(identifier, event.toProfile(), true);
    }

    @CraterEventListener
    public void luckPermsGroupRemovedFromUser(LuckPermsCompatEvents.GroupRemovedEvent event) {
        if (ignoreEvent)
            return;

        if (!SDLinkCompatConfig.INSTANCE.common.luckperms || !SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncToDiscord)
            return;

        // TODO REMOVE THIS TEMPORARY FIX ON NEXT CRATERLIB RELEASE
        String identifier = null;

        try {
            Field identifierField = event.getClass().getDeclaredField("identifier");
            identifierField.setAccessible(true);
            identifier = identifierField.get(event).toString();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (identifier == null)
            return;

        updateLuckpermsGroup(identifier, event.toProfile(), false);
    }

    private void updateLuckpermsGroup(String rank, BridgedGameProfile profile, boolean add) {
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

        Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncs.stream().filter(s -> s.rank.equalsIgnoreCase(rank)).findFirst();

        sync.ifPresent(s -> {
            Role role = RoleManager.getLuckPermsRoles().stream().filter(r -> r.getId().equalsIgnoreCase(s.role)).findFirst().orElse(null);
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

    @Override
    void discordRoleChanged(Member member, Guild guild, Role role, boolean add, MinecraftAccount oldAccount) {
        MinecraftAccount account = oldAccount != null ? oldAccount : MinecraftAccount.fromDiscordId(member.getId());
        if (account == null) return;

        RoleSyncCompat.Sync sync = SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncs.stream().filter(s -> s.role.equalsIgnoreCase(role.getId())).findFirst().orElse(null);
        if (sync == null) return;

        if (add) {
            if (!LuckPermsCompat.INSTANCE.hasGroup(account.getUuid(), sync.rank)) {
                ignoreEvent = true;
                LuckPermsCompat.INSTANCE.addGroupToUser(account.getUuid(), sync.rank);
                ignoreEvent = false;
            }
        } else {
            if (LuckPermsCompat.INSTANCE.hasGroup(account.getUuid(), sync.rank)) {
                ignoreEvent = true;
                LuckPermsCompat.INSTANCE.removeGroupFromUser(account.getUuid(), sync.rank);
                if (oldAccount != null) {
                    try {
                        guild.removeRoleFromMember(UserSnowflake.fromId(member.getId()), role).queue();
                    } catch (Exception ignored) {}
                }
                ignoreEvent = false;
            }
        }
    }

}
