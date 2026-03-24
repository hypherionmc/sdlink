package com.hypherionmc.sdlink.networking;

import com.hypherionmc.craterlib.api.client.mentions.MentionsController;
import com.hypherionmc.craterlib.api.game.nbt.CraterDataTag;
import com.hypherionmc.craterlib.api.game.network.CraterFriendlyByteBuf;
import com.hypherionmc.craterlib.api.game.resources.CraterIdentifier;
import com.hypherionmc.craterlib.core.networking.data.PacketContext;
import com.hypherionmc.craterlib.core.networking.data.PacketSide;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.client.ClientEvents;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;

import java.util.HashMap;

/**
 * @author HypherionSA
 * Config Packet to send cache data from Server to Client to allow mentions
 */
public final class MentionsSyncPacket {

    public static final CraterIdentifier CHANNEL = CraterIdentifier.fromGame(SDLinkConstants.MOD_ID, "syncpacket");

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

    public static MentionsSyncPacket decode(CraterFriendlyByteBuf buf) {
        MentionsSyncPacket p = new MentionsSyncPacket();

        CraterDataTag tag = buf.readNbt();
        if (tag == null)
            return p;

        CraterDataTag rolesTag = tag.getCompound("roles");
        CraterDataTag channelsTag = tag.getCompound("channels");
        CraterDataTag usersTag = tag.getCompound("users");

        p.roles = new HashMap<>();
        rolesTag.getAllKeys().forEach(k -> p.roles.put(k, rolesTag.getString(k)));

        p.channelHashMap = new HashMap<>();
        channelsTag.getAllKeys().forEach(k -> p.channelHashMap.put(k, channelsTag.getString(k)));

        p.users = new HashMap<>();
        usersTag.getAllKeys().forEach(k -> p.users.put(k, usersTag.getString(k)));

        p.mentionsEnabled = tag.getBoolean("mentionsenabled");

        return p;
    }

    public void write(CraterFriendlyByteBuf friendlyByteBuf) {
        CraterDataTag tag = CraterDataTag.empty();
        CraterDataTag rolesTag = CraterDataTag.empty();
        CraterDataTag channelsTag = CraterDataTag.empty();
        CraterDataTag usersTag = CraterDataTag.empty();
        roles.forEach(rolesTag::putString);
        channelHashMap.forEach(channelsTag::putString);
        users.forEach(usersTag::putString);

        tag.put("roles", rolesTag);
        tag.put("channels", channelsTag);
        tag.put("users", usersTag);
        tag.putBoolean("mentionsenabled", SDLinkConfig.INSTANCE.chatConfig.allowMentionsFromChat);
        friendlyByteBuf.writeNbt(tag);
    }

    public static void handle(PacketContext<MentionsSyncPacket> ctx) {
        if (PacketSide.CLIENT.equals(ctx.side())) {
            MentionsSyncPacket p = ctx.message();

            if (!(p.roles == null || p.roles.isEmpty())) {
                CraterIdentifier rrl = CraterIdentifier.fromGame("sdlink:roles");
                MentionsController.registerMention(rrl, p.roles.keySet(), currentWord -> currentWord.startsWith("[@") || currentWord.startsWith("@"));
            }

            if (!(p.channelHashMap == null || p.channelHashMap.isEmpty())) {
                CraterIdentifier crl = CraterIdentifier.fromGame("sdlink:channels");
                MentionsController.registerMention(crl, p.channelHashMap.keySet(), currentWord -> currentWord.startsWith("[#") || currentWord.startsWith("#"));
            }

            if (!(p.users == null || p.users.isEmpty())) {
                CraterIdentifier url = CraterIdentifier.fromGame("sdlink:users");
                MentionsController.registerMention(url, p.users.keySet(), currentWord -> currentWord.startsWith("[@") || currentWord.startsWith("@"));
            }

            ClientEvents.mentionsEnabled = p.mentionsEnabled;
        }

    }
}
