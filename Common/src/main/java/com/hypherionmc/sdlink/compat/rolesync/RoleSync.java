package com.hypherionmc.sdlink.compat.rolesync;

import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.craterlib.nojang.world.entity.player.BridgedPlayer;
import com.hypherionmc.sdlink.api.accounts.DiscordUser;
import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.compat.rolesync.impl.FTBRankSync;
import com.hypherionmc.sdlink.compat.rolesync.impl.LuckPermsSync;
import com.hypherionmc.sdlink.core.config.SDLinkCompatConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public final class RoleSync {

    public static final RoleSync INSTANCE = new RoleSync();

    public void sync(BridgedPlayer p) {
        if (!SDLinkCompatConfig.INSTANCE.common.ftbranks && !SDLinkCompatConfig.INSTANCE.common.luckperms)
            return;

        MinecraftAccount account = MinecraftAccount.of(p.getGameProfile());
        DiscordUser user = account.getDiscordUser();
        if (user == null)
            return;

        Guild g = BotController.INSTANCE.getJDA().getGuilds().get(0);
        if (g == null)
            return;

        Member member = g.getMemberById(user.getUserId());
        if (member == null)
            return;

        List<Role> roles = member.getRoles();

        if (ModloaderEnvironment.INSTANCE.isModLoaded("ftbranks") && SDLinkCompatConfig.INSTANCE.common.ftbranks) {
            try {
                FTBRankSync.INSTANCE.sync(p, roles, g, member);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (ModloaderEnvironment.INSTANCE.isModLoaded("luckperms") && SDLinkCompatConfig.INSTANCE.common.luckperms) {
            try {
                LuckPermsSync.INSTANCE.sync(p, roles, g, member);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (ModloaderEnvironment.INSTANCE.isModLoaded("player_roles") && SDLinkCompatConfig.INSTANCE.common.playerroles) {
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void roleAddedToMember(Member member, Role role, Guild guild) {
        if (ModloaderEnvironment.INSTANCE.isModLoaded("ftbranks")) {
            FTBRankSync.INSTANCE.discordRoleAddedToMember(member, role, guild);
        }

        if (ModloaderEnvironment.INSTANCE.isModLoaded("luckperms")) {
            LuckPermsSync.INSTANCE.discordRoleAddedToMember(member, role, guild);
        }
    }

    public void roleRemovedFromMember(Member member, Role role, Guild guild, MinecraftAccount oldAccount) {
        if (ModloaderEnvironment.INSTANCE.isModLoaded("ftbranks")) {
            FTBRankSync.INSTANCE.discordRoleRemovedFromMember(member, role, guild, oldAccount);
        }

        if (ModloaderEnvironment.INSTANCE.isModLoaded("luckperms")) {
            LuckPermsSync.INSTANCE.discordRoleRemovedFromMember(member, role, guild, oldAccount);
        }
    }

}
