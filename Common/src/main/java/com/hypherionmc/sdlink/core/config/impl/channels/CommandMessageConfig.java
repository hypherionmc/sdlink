package com.hypherionmc.sdlink.core.config.impl.channels;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandMessageConfig implements IChannelContainer {

    @Path("enabled")
    @SpecComment("Enable Commands being relayed to Minecraft")
    public boolean enabled = true;

    @Path("channels")
    @SpecComment("The Channel IDs the message will be delivered to")
    public List<String> channels = new ArrayList<>() {{ add("default_console"); }};

    @Path("useFancy")
    @SpecComment("Use fancy messages (webhooks) to deliver channels messages to discord")
    public boolean useFancy = false;

    @Path("format")
    @SpecComment("The formatting that will be used for this message. Ignored when using Fancy or Embeds")
    public String format = "%player% **executed command**: *%command%*";

    @Path("useEmbed")
    @SpecComment("Should the message be sent using Embed style messages instead of plain text")
    public boolean useEmbed = false;

    @Path("embedLayout")
    @SpecComment("The embed layout to use for this message")
    public String embedLayout = "default";

    @Path("relayTellRaw")
    @SpecComment("Should messages sent with TellRaw be sent to discord as a channels? (Experimental)")
    public boolean relayTellRaw = true;

    @Path("relayFullCommands")
    @SpecComment("Should the entire command executed be relayed to discord, or only the name of the command")
    public boolean relayFullCommands = false;

    @Path("sendSayCommand")
    @SpecComment("Should Messages from the /say command be posted")
    public boolean sendSayCommand = true;

    @Path("ignoredCommands")
    @SpecComment("Commands that should not be broadcast to discord")
    public List<String> ignoredCommands = new ArrayList<>() {{
        add("particle");
        add("login");
        add("execute");
        add("sdconfigeditor");
    }};

    @Override
    @NotNull
    public List<GuildMessageChannel> channels() {
        return SDLCache.INSTANCE.getChannelDestinations(MessageType.COMMANDS);
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
        return MessageType.COMMANDS;
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