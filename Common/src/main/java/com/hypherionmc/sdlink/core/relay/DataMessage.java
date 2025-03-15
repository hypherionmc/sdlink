package com.hypherionmc.sdlink.core.relay;

import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import shadow.kyori.adventure.text.Component;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class DataMessage {

    private String displayName;
    @Getter private String username;
    private String message;
    @Getter private UUID uuid;
    private String additional;
    @Getter private boolean isFromServer;

    public static DataMessage of(Component displayName, String username, @Nullable Component message, UUID uuid, boolean isFromServer) {
        return of (displayName, username, message, uuid, null, isFromServer);
    }

    public static DataMessage of(Component displayName, String username, @Nullable Component message, UUID uuid, @Nullable Component additional, boolean isFromServer) {
        return of(SDLinkChatUtils.serializer.serialize(displayName), username, message == null ? null : SDLinkChatUtils.serializer.serialize(message), uuid, additional == null ? null : SDLinkChatUtils.serializer.serialize(additional), isFromServer);
    }

    public Component displayName() {
        return SDLinkChatUtils.serializer.deserialize(displayName);
    }

    public Component message() {
        return SDLinkChatUtils.serializer.deserialize(message);
    }

    public Component additional() {
        return SDLinkChatUtils.serializer.deserialize(additional);
    }

}
