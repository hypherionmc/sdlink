package com.hypherionmc.sdlink.core.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypherionmc.craterlib.api.game.commands.CraterCommandSourceStack;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.util.EncryptionUtil;
import com.hypherionmc.sdlink.util.configeditor.SocketResponse;
import com.neovisionaries.ws.client.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.event.ClickEvent;
import org.apache.commons.io.FileUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ConfigEditorWSEvents implements WebSocketListener {

    private final Gson GSON = new GsonBuilder().serializeNulls().create();
    private final String identifier;
    private final CraterCommandSourceStack sourceStack;

    @Override
    public void onConnected(WebSocket webSocket, Map<String, List<String>> map) throws Exception {
        BotController.INSTANCE.getLogger().info("Editor Websocket connected");
    }

    @Override
    public void onTextFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        SocketResponse response = GSON.fromJson(webSocketFrame.getPayloadText(), SocketResponse.class);

        if (response.getSocketCode().equalsIgnoreCase("WS_WAITING")) {
            sourceStack.sendMessage(
                    Text.literal(String.format("Editor Connection Ready. Visit https://editor.firstdark.dev/%s to get started", identifier))
                            .clickEvent(ClickEvent.openUrl(String.format("https://editor.firstdark.dev/%s", identifier)))
            );
            BotController.INSTANCE.getLogger().info("Editor Connection Ready. Visit https://editor.firstdark.dev/{} to get started", identifier);
        }

        if (response.getSocketCode().equalsIgnoreCase("WS_GET_CONFIG")) {
            EncryptionUtil ec = new EncryptionUtil(identifier);
            String config = FileUtils.readFileToString(SDLinkConfig.INSTANCE.getConfigPath(), StandardCharsets.UTF_8);
            webSocket.sendText(GSON.toJson(SocketResponse.of("WS_SEND_CONFIG", ec.encrypt(config))));
        }

        if (response.getSocketCode().equalsIgnoreCase("WS_SAVE_CONFIG")) {
            BotController.INSTANCE.getLogger().info("Got Config update from editor");
            EncryptionUtil ec = new EncryptionUtil(identifier);
            String config = ec.decrypt(response.getMessage());
            FileUtils.writeStringToFile(SDLinkConfig.INSTANCE.getConfigPath(), config, StandardCharsets.UTF_8);
            SDLinkConfig.INSTANCE.configReloaded();
        }
    }

    @Override
    public void onConnectError(WebSocket webSocket, WebSocketException e) throws Exception {
        BotController.INSTANCE.getLogger().error("Failed to connect to editor web socket", e);
    }

    @Override
    public void onDisconnected(WebSocket webSocket, WebSocketFrame webSocketFrame, WebSocketFrame webSocketFrame1, boolean b) throws Exception {
        BotController.INSTANCE.getLogger().warn("Disconnected from Editor Websocket with code {}: {}", webSocketFrame.getCloseCode(), webSocketFrame.getCloseReason());
    }

    @Override
    public void onCloseFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        BotController.INSTANCE.getLogger().warn("Connection from Editor Terminated with code {}: {}", webSocketFrame.getCloseCode(), webSocketFrame.getCloseReason());
    }



    // WE DO NOT USE THESE, BUT THEY ARE NEED TO BE IMPLEMENTED!!!

    @Override
    public void onStateChanged(WebSocket webSocket, WebSocketState webSocketState) throws Exception {

    }

    @Override
    public void onFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

    }

    @Override
    public void onContinuationFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

    }

    @Override
    public void onBinaryFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

    }

    @Override
    public void onPingFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

    }

    @Override
    public void onPongFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

    }

    @Override
    public void onTextMessage(WebSocket webSocket, String s) throws Exception {

    }

    @Override
    public void onTextMessage(WebSocket webSocket, byte[] bytes) throws Exception {

    }

    @Override
    public void onBinaryMessage(WebSocket webSocket, byte[] bytes) throws Exception {

    }

    @Override
    public void onSendingFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

    }

    @Override
    public void onFrameSent(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

    }

    @Override
    public void onFrameUnsent(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

    }

    @Override
    public void onThreadCreated(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {

    }

    @Override
    public void onThreadStarted(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {

    }

    @Override
    public void onThreadStopping(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {

    }

    @Override
    public void onError(WebSocket webSocket, WebSocketException e) throws Exception {

    }

    @Override
    public void onFrameError(WebSocket webSocket, WebSocketException e, WebSocketFrame webSocketFrame) throws Exception {

    }

    @Override
    public void onMessageError(WebSocket webSocket, WebSocketException e, List<WebSocketFrame> list) throws Exception {

    }

    @Override
    public void onMessageDecompressionError(WebSocket webSocket, WebSocketException e, byte[] bytes) throws Exception {

    }

    @Override
    public void onTextMessageError(WebSocket webSocket, WebSocketException e, byte[] bytes) throws Exception {

    }

    @Override
    public void onSendError(WebSocket webSocket, WebSocketException e, WebSocketFrame webSocketFrame) throws Exception {

    }

    @Override
    public void onUnexpectedError(WebSocket webSocket, WebSocketException e) throws Exception {

    }

    @Override
    public void handleCallbackError(WebSocket webSocket, Throwable throwable) throws Exception {

    }

    @Override
    public void onSendingHandshake(WebSocket webSocket, String s, List<String[]> list) throws Exception {

    }
}
