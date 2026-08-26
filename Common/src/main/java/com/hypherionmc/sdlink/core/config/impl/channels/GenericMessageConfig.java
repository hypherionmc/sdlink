package com.hypherionmc.sdlink.core.config.impl.channels;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GenericMessageConfig implements IChannelContainer {

    private transient final MessageType type;

    @Path("enabled")
    @SpecComment("Should these types of messages be enabled")
    public boolean enabled = true;

    @Path("channels")
    @SpecComment("The Channel IDs the message will be delivered to")
    public List<String> channels = new ArrayList<>() {{ add("default_event"); }};

    @Path("useFancy")
    @SpecComment("Use fancy messages (webhooks) to deliver messages to discord")
    public boolean useFancy = false;

    @Path("format")
    @SpecComment("The formatting that will be used for this message. Ignored when using Fancy or Embeds")
    public String format;

    @Path("useEmbed")
    @SpecComment("Should the message be sent using Embed style messages instead of plain text")
    public boolean useEmbed = false;

    @Path("embedLayout")
    @SpecComment("The embed layout to use for this message")
    public String embedLayout = "default";

    public GenericMessageConfig(MessageType type, String format) {
        this.type = type;
        this.format = format;
    }

    @Override
    @NotNull
    public List<GuildMessageChannel> channels() {
        return SDLCache.INSTANCE.getChannelDestinations(type);
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
    @NotNull
    public MessageType type() {
        return type;
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
