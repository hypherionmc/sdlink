package com.hypherionmc.sdlink.core.relay;

import com.hypherionmc.craterlib.utils.ChatUtils;
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
        return of(ChatUtils.getAdventureSerializer().serialize(displayName), username, message == null ? null : ChatUtils.getAdventureSerializer().serialize(message), uuid, additional == null ? null : ChatUtils.getAdventureSerializer().serialize(additional), isFromServer);
    }

    public Component displayName() {
        return ChatUtils.getAdventureSerializer().deserialize(displayName);
    }

    public Component message() {
        return ChatUtils.getAdventureSerializer().deserialize(message);
    }

    public Component additional() {
        return ChatUtils.getAdventureSerializer().deserialize(additional);
    }

}
