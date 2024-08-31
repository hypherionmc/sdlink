package com.hypherionmc.sdlink.core.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@RequiredArgsConstructor(staticName = "of")
public class SDLWebhookServerMember implements Member {

    private final User user;
    private final Guild guild;
    private final JDA jda;

    @NotNull
    @Override
    public User getUser() {
        return user;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return guild;
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissions() {
        return Permission.getPermissions(Permission.ALL_PERMISSIONS);
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissions(GuildChannel guildChannel) {
        return Permission.getPermissions(Permission.ALL_PERMISSIONS);
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissionsExplicit() {
        return Permission.getPermissions(Permission.ALL_PERMISSIONS);
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissionsExplicit(@NotNull GuildChannel guildChannel) {
        return Permission.getPermissions(Permission.ALL_PERMISSIONS);
    }

    @Override
    public boolean hasPermission(@NotNull Permission... permissions) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull Collection<Permission> collection) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull GuildChannel guildChannel, @NotNull Permission... permissions) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull GuildChannel guildChannel, @NotNull Collection<Permission> collection) {
        return true;
    }

    @Override
    public boolean canSync(@NotNull IPermissionContainer iPermissionContainer, @NotNull IPermissionContainer iPermissionContainer1) {
        return true;
    }

    @Override
    public boolean canSync(@NotNull IPermissionContainer iPermissionContainer) {
        return true;
    }

    @NotNull
    @Override
    public JDA getJDA() {
        return jda;
    }

    @NotNull
    @Override
    public OffsetDateTime getTimeJoined() {
        return user.getTimeCreated();
    }

    @Override
    public boolean hasTimeJoined() {
        return false;
    }

    @Override
    public OffsetDateTime getTimeBoosted() {
        return null;
    }

    @Override
    public boolean isBoosting() {
        return false;
    }

    @Override
    public OffsetDateTime getTimeOutEnd() {
        return null;
    }

    @Override
    public GuildVoiceState getVoiceState() {
        return null;
    }

    @NotNull
    @Override
    public List<Activity> getActivities() {
        return List.of();
    }

    @NotNull
    @Override
    public OnlineStatus getOnlineStatus() {
        return OnlineStatus.ONLINE;
    }

    @NotNull
    @Override
    public OnlineStatus getOnlineStatus(@NotNull ClientType clientType) {
        return OnlineStatus.ONLINE;
    }

    @NotNull
    @Override
    public EnumSet<ClientType> getActiveClients() {
        return EnumSet.of(ClientType.DESKTOP);
    }

    @Override
    public String getNickname() {
        return user.getName();
    }

    @NotNull
    @Override
    public String getEffectiveName() {
        return user.getEffectiveName();
    }

    @Override
    public String getAvatarId() {
        return user.getAvatarId();
    }

    @NotNull
    @Override
    public List<Role> getRoles() {
        return List.of();
    }

    @Override
    public Color getColor() {
        return Color.WHITE;
    }

    @Override
    public int getColorRaw() {
        return Color.white.getRGB();
    }

    @Override
    public int getFlagsRaw() {
        return user.getFlagsRaw();
    }

    @Override
    public boolean canInteract(@NotNull Member member) {
        return true;
    }

    @Override
    public boolean canInteract(@NotNull Role role) {
        return true;
    }

    @Override
    public boolean canInteract(@NotNull RichCustomEmoji richCustomEmoji) {
        return true;
    }

    @Override
    public boolean isOwner() {
        return false;
    }

    @Override
    public boolean isPending() {
        return false;
    }

    @Override
    public DefaultGuildChannelUnion getDefaultChannel() {
        return null;
    }

    @NotNull
    @Override
    public String getAsMention() {
        return user.getAsMention();
    }

    @NotNull
    @Override
    public String getDefaultAvatarId() {
        return user.getDefaultAvatarId();
    }

    @Override
    public long getIdLong() {
        return user.getIdLong();
    }
}
