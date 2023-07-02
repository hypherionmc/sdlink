package com.hypherionmc.sdlink.networking;

import com.hypherionmc.craterlib.core.network.CraterPacket;
import com.hypherionmc.sdlink.client.ClientEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
        users.forEach(channelsTag::putString);

        tag.put("roles", rolesTag);
        tag.put("channels", channelsTag);
        tag.put("users", usersTag);
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
    }

    @Override
    public PacketHandler<MentionsSyncPacket> createHandler() {
        return new PacketHandler<>() {
            @Override
            public void handle(MentionsSyncPacket mentionsSyncPacket, Player player, Object o) {
                if (!(roles == null || roles.isEmpty())) {
                    ClientEvents.roles = roles;
                }

                if (!(channelHashMap == null || channelHashMap.isEmpty())) {
                    ClientEvents.channels = channelHashMap;
                }

                if (!(users == null || users.isEmpty())) {
                    ClientEvents.users = users;
                }
            }
        };
    }
}
