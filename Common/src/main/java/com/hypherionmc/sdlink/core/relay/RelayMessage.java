package com.hypherionmc.sdlink.core.relay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Getter
public final class RelayMessage {

    private MessageType type;
    private String serverName;
    private DataMessage data;
    private String message;

    public static RelayMessage of(MessageType type, String serverName, DataMessage data) {
        return new RelayMessage(type, serverName, data, null);
    }

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        ADVANCEMENT,
        DEATH,
        DISCORD
    }
}
