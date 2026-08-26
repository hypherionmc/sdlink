package com.hypherionmc.sdlink.core.config.impl.channels;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagesConfig implements IChannelContainer {

    @Path("enabled")
    @SpecComment("Enable Chat Messages from being relayed to Minecraft")
    public boolean enabled = true;

    @Path("channels")
    @SpecComment("The Channel IDs the message will be delivered to")
    public List<String> channels = new ArrayList<>() {{ add("default_chat"); }};

    @Path("useFancy")
    @SpecComment("Use fancy messages (webhooks) to deliver channels messages to discord")
    public boolean useFancy = false;

    @Path("webhookNameFormat")
    @SpecComment("Change how the webhook name is displayed in discord. Available placeholders: %display_name%, %mc_name%")
    public String webhookNameFormat = "%display_name%";

    @Path("format")
    @SpecComment("The formatting that will be used for this message. Ignored when using Fancy or Embeds")
    public String format = "%player%: %message%";

    @Path("mcPrefix")
    @SpecComment("Prefix to add to Minecraft when a message is relayed from Discord. Supports MiniMessage formatting. Use %user% for the Discord Username")
    public String mcPrefix = "<yellow>[Discord]<reset> %user%: ";

    @Path("mcReplyFormatting")
    @SpecComment("How messages relayed from discord that are replies to other messages are formatted. Supports MiniMessage formatting")
    public String mcReplyFormatting = "    <b>┌────<reset> %color%@%replier_name%%end_color% <gray>%message_summary%<newline><reset>";

    @Path("useEmbed")
    @SpecComment("Should the message be sent using Embed style messages instead of plain text")
    public boolean useEmbed = false;

    @Path("embedLayout")
    @SpecComment("The embed layout to use for this message")
    public String embedLayout = "default";

    @Path("allowMentionsFromChat")
    @SpecComment("Allow players to mention Discord Users, Channels and Roles from Minecraft")
    public boolean allowMentionsFromChat = false;

    @Path("sendToMinecraft")
    @SpecComment("Should messages sent in discord be relayed to Minecraft")
    public boolean sendToMinecraft = true;

    @Path("sendFromMinecraft")
    @SpecComment("Should messages sent in Minecraft be relayed to Discord")
    public boolean sendFromMinecraft = true;

    @Path("ignoreBots")
    @SpecComment("Should messages from bots be ignored")
    public boolean ignoreBots = true;

    @Path("pluralKitCompat")
    @SpecComment("Compatibility with PluralKit (can introduce a very slight delay in messages being sent from Discord to Minecraft)")
    public boolean pluralKitCompat = false;

    @Path("pluralKitCompatMessageDelay")
    @SpecComment("Amount of time to delay messages by before sending them to Minecraft in milliseconds. Too low may make unproxied messages visible and too high will cause noticeable delay.")
    public int pluralKitCompatMessageDelay = 500;

    @Path("useServerForChat")
    @SpecComment("Use Server Author for channels messages, instead of the real author information")
    public boolean useServerForChat = false;

    @Path("showDiscordInfo")
    @SpecComment("Show the discord name, username and role of the user that sent a message in Minecraft when the message is hovered")
    public boolean showDiscordInfo = false;

    @Override
    @NotNull
    public List<GuildMessageChannel> channels() {
        return SDLCache.INSTANCE.getChannelDestinations(MessageType.CHAT);
    }

    @Override
    public boolean useEmbed() {
        return useEmbed;
    }

    @Override
    @NotNull
    public String embedLayout() {
        return embedLayout;
    }

    @Override
    public @NotNull MessageType type() {
        return MessageType.CHAT;
    }

    @Override
    public boolean useWebhook() {
        return useFancy;
    }

    @Override
    @NotNull
    public List<String> channelsRaw() {
        return channels;
    }
}
