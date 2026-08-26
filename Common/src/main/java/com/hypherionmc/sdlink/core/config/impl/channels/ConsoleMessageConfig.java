package com.hypherionmc.sdlink.core.config.impl.channels;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConsoleMessageConfig implements IChannelContainer {

    @Path("enabled")
    @SpecComment("Should these types of messages be enabled")
    public boolean enabled = false;

    @Path("channels")
    @SpecComment("The Channel IDs the message will be delivered to")
    public List<String> channels = new ArrayList<>() {{ add("default_console"); }};

    @Path("filterIPs")
    @SpecComment("Filter out IP addresses from console messages")
    public boolean filterIPs = true;

    @Override
    @NotNull
    public List<GuildMessageChannel> channels() {
        return SDLCache.INSTANCE.getChannelDestinations(MessageType.CONSOLE);
    }

    @Override
    public boolean useEmbed() {
        return false;
    }

    @Override
    @NotNull
    public String embedLayout() {
        return "";
    }

    @Override
    public @NotNull MessageType type() {
        return MessageType.CONSOLE;
    }

    @Override
    public boolean useWebhook() {
        return false;
    }

    @Override
    @NotNull
    public List<String> channelsRaw() {
        return channels;
    }
}
