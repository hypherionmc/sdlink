package com.hypherionmc.sdlink.networking;

import com.hypherionmc.craterlib.core.network.CraterPacket;
import com.hypherionmc.sdlink.client.ClientEvents;
import com.hypherionmc.sdlink.client.MentionsController;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;

/**
 * @author HypherionSA
 * Config Packet to send cache data from Server to Client to allow mentions
 */
public class MentionsSyncPacket implements CraterPacket<MentionsSyncPacket> {

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

    @Override
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
        friendlyByteBuf.writeNbt(tag);
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) {
        CompoundTag tag = friendlyByteBuf.readNbt();
        if (tag == null)
            return;

        CompoundTag rolesTag = tag.getCompound("roles");
        CompoundTag channelsTag = tag.getCompound("channels");
        CompoundTag usersTag = tag.getCompound("users");

        roles = new HashMap<>();
        rolesTag.getAllKeys().forEach(k -> roles.put(k, rolesTag.getString(k)));

        channelHashMap = new HashMap<>();
        channelsTag.getAllKeys().forEach(k -> channelHashMap.put(k, channelsTag.getString(k)));

        users = new HashMap<>();
        usersTag.getAllKeys().forEach(k -> users.put(k, usersTag.getString(k)));

        mentionsEnabled = tag.getBoolean("mentionsenabled");
    }

    @Override
    public PacketHandler<MentionsSyncPacket> createHandler() {
        return new PacketHandler<>() {
            @Override
            public void handle(MentionsSyncPacket mentionsSyncPacket, Player player, Object o) {
                if (!(roles == null || roles.isEmpty())) {
                    ResourceLocation rrl = new ResourceLocation("sdlink:roles");
                    MentionsController.registerMention(rrl, roles.keySet(), currentWord -> currentWord.startsWith("[@") || currentWord.startsWith("@"));
                }

                if (!(channelHashMap == null || channelHashMap.isEmpty())) {
                    ResourceLocation crl = new ResourceLocation("sdlink:channels");
                    MentionsController.registerMention(crl, channelHashMap.keySet(), currentWord -> currentWord.startsWith("[#") || currentWord.startsWith("#"));
                }

                if (!(users == null || users.isEmpty())) {
                    ResourceLocation url = new ResourceLocation("sdlink:users");
                    MentionsController.registerMention(url, users.keySet(), currentWord -> currentWord.startsWith("[@") || currentWord.startsWith("@"));
                }

                ClientEvents.mentionsEnabled = mentionsEnabled;
            }
        };
    }
}
