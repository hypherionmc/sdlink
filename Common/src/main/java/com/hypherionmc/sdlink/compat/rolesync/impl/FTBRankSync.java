package com.hypherionmc.sdlink.compat.rolesync.impl;

import com.hypherionmc.craterlib.api.compat.ftbranks.CraterFTBRank;
import com.hypherionmc.craterlib.api.compat.ftbranks.FTBRanks;
import com.hypherionmc.craterlib.api.events.compat.FTBRankEvents;
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

import java.util.List;
import java.util.Optional;

public final class FTBRankSync extends AbstractRoleSyncer {

    public static final FTBRankSync INSTANCE = new FTBRankSync();

    private FTBRankSync() {
        super(() -> SDLinkCompatConfig.INSTANCE.common.ftbranks && SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncToMinecraft);
    }

    @Override
    public void sync(CraterPlayer p, List<Role> roles, Guild guild, Member member) {
        // Discord to Minecraft Sync
        if (SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncToMinecraft) {
            // Add Ranks To Users
            for (Role role : roles) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncs.stream().filter(s -> s.role.equalsIgnoreCase(role.getId())).findFirst();

                sync.ifPresent(s -> {
                    if (!FTBRanks.getInstance().hasRank(p.getGameProfile(), s.rank)) {
                        ignoreEvent = true;
                        FTBRanks.getInstance().addRank(p.getGameProfile(), s.rank);
                        ignoreEvent = false;
                    }
                });
            }

            // Remove Ranks from Users
            List<? extends CraterFTBRank> ranks = FTBRanks.getInstance().getPlayerRanks(p.getGameProfile());
            for (CraterFTBRank rank : ranks) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncs.stream().filter(s -> s.rank.equalsIgnoreCase(rank.name()) || s.rank.equalsIgnoreCase(rank.id())).findFirst();

                sync.ifPresent(s -> {
                    if (roles.stream().noneMatch(r -> r.getId().equalsIgnoreCase(s.role)) && FTBRanks.getInstance().hasRank(p.getGameProfile(), s.rank)) {
                        ignoreEvent = true;
                        FTBRanks.getInstance().removeRank(p.getGameProfile(), s.rank);
                        ignoreEvent = false;
                    }
                });
            }
        }

        // Minecraft to Discord Sync
        if (SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncToDiscord) {
            List<? extends CraterFTBRank> ranks = FTBRanks.getInstance().getPlayerRanks(p.getGameProfile());

            // Add Roles to Users
            for (CraterFTBRank rank : ranks) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncs.stream().filter(s -> s.rank.equalsIgnoreCase(rank.name()) || s.rank.equalsIgnoreCase(rank.id())).findFirst();

                sync.ifPresent(s -> {
                    if (roles.stream().noneMatch(r -> r.getId().equalsIgnoreCase(s.role))) {
                        Optional<Role> r = RoleManager.getFtbRanksRoles().stream().filter(rr -> rr.getId().equalsIgnoreCase(s.role)).findFirst();
                        if (r.isEmpty())
                            return;

                        ignoreEvent = true;
                        guild.addRoleToMember(UserSnowflake.fromId(member.getId()), r.get()).queue(suc -> ignoreEvent = false);
                    }
                });
            }

            // Remove Role from Users
            for (Role role : roles) {
                Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncs.stream().filter(s -> s.role.equalsIgnoreCase(role.getId())).findFirst();

                if (sync.isPresent() && !FTBRanks.getInstance().hasRank(p.getGameProfile(), sync.get().rank)) {
                    Optional<Role> r = RoleManager.getFtbRanksRoles().stream().filter(rr -> rr.getId().equalsIgnoreCase(sync.get().role)).findFirst();
                    if (r.isEmpty())
                        return;

                    ignoreEvent = true;
                    guild.removeRoleFromMember(UserSnowflake.fromId(member.getId()), r.get()).queue(suc -> ignoreEvent = false);
                }
            }
        }
    }

    @CraterEventListener
    public void ftbRankAddedToUser(FTBRankEvents.RankAddedEvent event) {
        if (ignoreEvent)
            return;

        if (!SDLinkCompatConfig.INSTANCE.common.ftbranks || !SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncToDiscord)
            return;

        updateFtbRank(event.getGameProfile(), event.getRank(), true);
    }

    @CraterEventListener
    public void ftbRankRemovedFromUser(FTBRankEvents.RankRemovedEvent event) {
        if (ignoreEvent)
            return;

        if (!SDLinkCompatConfig.INSTANCE.common.ftbranks || !SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncToDiscord)
            return;

        updateFtbRank(event.getGameProfile(), event.getRank(), false);
    }

    private void updateFtbRank(CraterGameProfile profile, CraterFTBRank rank, boolean add) {
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

        Optional<RoleSyncCompat.Sync> sync = SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncs.stream().filter(s -> s.rank.equalsIgnoreCase(rank.name()) || s.rank.equalsIgnoreCase(rank.id())).findFirst();

        sync.ifPresent(s -> {
            Role role = RoleManager.getFtbRanksRoles().stream().filter(r -> r.getId().equalsIgnoreCase(s.role)).findFirst().orElse(null);
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

        RoleSyncCompat.Sync sync = SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncs.stream().filter(s -> s.role.equalsIgnoreCase(role.getId())).findFirst().orElse(null);
        if (sync == null) return;

        if (add) {
            if (!FTBRanks.getInstance().hasRank(account.toGameProfile(), sync.rank)) {
                ignoreEvent = true;
                FTBRanks.getInstance().addRank(account.toGameProfile(), sync.rank);
                ignoreEvent = false;
            }
        } else {
            if (FTBRanks.getInstance().hasRank(account.toGameProfile(), sync.rank)) {
                ignoreEvent = true;
                FTBRanks.getInstance().removeRank(account.toGameProfile(), sync.rank);
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
