package com.hypherionmc.sdlink.networking;

import com.hypherionmc.craterlib.core.abstraction.server.AbstractFriendlyByteBuff;
import com.hypherionmc.craterlib.core.networking.data.PacketContext;
import com.hypherionmc.craterlib.core.networking.data.PacketSide;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.client.ClientEvents;
import com.hypherionmc.sdlink.client.MentionsController;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

/**
 * @author HypherionSA
 * Config Packet to send cache data from Server to Client to allow mentions
 */
public class MentionsSyncPacket {

    public static final ResourceLocation CHANNEL = new ResourceLocation(SDLinkConstants.MOD_ID, "syncpacket");

    private HashMap<String, String> roles;
    private HashMap<String, String> channelHashMap;
    private HashMap<String, String> users;
    private boolean mentionsEnabled = false;

    public MentionsSyncPacket() {}

    public MentionsSyncPacket(HashMap<String, String> roles, HashMap<String, String> channels, HashMap<String, String> users) {
        this.roles = roles;
        this.channelHashMap = channels;
        this.users = users;
    }

    public static MentionsSyncPacket decode(FriendlyByteBuf buf) {
        MentionsSyncPacket p = new MentionsSyncPacket();

        CompoundTag tag = buf.readNbt();
        if (tag == null)
            return p;

        CompoundTag rolesTag = tag.getCompound("roles");
        CompoundTag channelsTag = tag.getCompound("channels");
        CompoundTag usersTag = tag.getCompound("users");

        p.roles = new HashMap<>();
        rolesTag.getAllKeys().forEach(k -> p.roles.put(k, rolesTag.getString(k)));

        p.channelHashMap = new HashMap<>();
        channelsTag.getAllKeys().forEach(k -> p.channelHashMap.put(k, channelsTag.getString(k)));

        p.users = new HashMap<>();
        usersTag.getAllKeys().forEach(k -> p.users.put(k, usersTag.getString(k)));

        p.mentionsEnabled = tag.getBoolean("mentionsenabled");

        return p;
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        CompoundTag tag = new CompoundTag();
        CompoundTag rolesTag = new CompoundTag();
        CompoundTag channelsTag = new CompoundTag();
        CompoundTag usersTag = new CompoundTag();
        roles.forEach(rolesTag::putString);
        channelHashMap.forEach(channelsTag::putString);
        users.forEach(usersTag::putString);

        tag.put("roles", rolesTag);
        tag.put("channels", channelsTag);
        tag.put("users", usersTag);
        tag.putBoolean("mentionsenabled", SDLinkConfig.INSTANCE.chatConfig.allowMentionsFromChat);
        AbstractFriendlyByteBuff.write(friendlyByteBuf, tag);
    }

    public static void handle(PacketContext<MentionsSyncPacket> ctx) {
        if (PacketSide.CLIENT.equals(ctx.side())) {
            MentionsSyncPacket p = ctx.message();

            if (!(p.roles == null || p.roles.isEmpty())) {
                ResourceLocation rrl = new ResourceLocation("sdlink:roles");
                MentionsController.registerMention(rrl, p.roles.keySet(), currentWord -> currentWord.startsWith("[@") || currentWord.startsWith("@"));
            }

            if (!(p.channelHashMap == null || p.channelHashMap.isEmpty())) {
                ResourceLocation crl = new ResourceLocation("sdlink:channels");
                MentionsController.registerMention(crl, p.channelHashMap.keySet(), currentWord -> currentWord.startsWith("[#") || currentWord.startsWith("#"));
            }

            if (!(p.users == null || p.users.isEmpty())) {
                ResourceLocation url = new ResourceLocation("sdlink:users");
                MentionsController.registerMention(url, p.users.keySet(), currentWord -> currentWord.startsWith("[@") || currentWord.startsWith("@"));
            }

            ClientEvents.mentionsEnabled = p.mentionsEnabled;
        }

    }
}
