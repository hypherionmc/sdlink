package com.hypherionmc.sdlink.core.relay;

import com.hypherionmc.craterlib.api.game.text.Text;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

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

    public static DataMessage of(Text displayName, String username, @Nullable Text message, UUID uuid, boolean isFromServer) {
        return of (displayName, username, message, uuid, null, isFromServer);
    }

    public static DataMessage of(Text displayName, String username, @Nullable Text message, UUID uuid, @Nullable Text additional, boolean isFromServer) {
        return of(displayName.toJsonString(), username, message == null ? null : message.toJsonString(), uuid, additional == null ? null : additional.toJsonString(), isFromServer);
    }

    public Text displayName() {
        return Text.fromJson(displayName);
    }

    public Text message() {
        return Text.fromJson(message);
    }

    public Text additional() {
        return Text.fromJson(additional);
    }

}
