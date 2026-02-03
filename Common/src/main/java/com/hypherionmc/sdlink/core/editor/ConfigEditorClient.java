package com.hypherionmc.sdlink.core.editor;

import com.hypherionmc.craterlib.api.game.commands.CraterCommandSourceStack;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.util.EncryptionUtil;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigEditorClient {

    public static final ConfigEditorClient INSTANCE = new ConfigEditorClient();

    private WebSocket webSocket;

    public void openConnection(CraterCommandSourceStack sourceStack) {
        String identifier = EncryptionUtil.getSaltString();

        try {
            closeServer();
            webSocket = new WebSocketFactory().createSocket("wss://editor.firstdark.dev/ws/config?identifier=" + identifier);
            webSocket.setPingInterval(10000);
            webSocket.addListener(new ConfigEditorWSEvents(identifier, sourceStack));
            webSocket.connect();
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to open connection to Config Editor", e);
        }
    }

    public void closeServer() {
        try {
            if (webSocket != null && webSocket.isOpen())
                webSocket.disconnect();
        } catch (Exception ignored) {}
    }
}
