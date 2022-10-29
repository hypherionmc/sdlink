package me.hypherionmc.sdlink;

import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;

/**
 * @author HypherionSA
 * @date 29/10/2022
 */
public class SafeCalls {

    public static void tailorPlayerJoin(ServerPlayer player, String command) {
        if (player instanceof TailoredPlayer) {
            TailoredPlayer tp = (TailoredPlayer) player;
            ServerEvents.getInstance().commandEvent(
                    command,
                    player.getDisplayName().getString(),
                    tp.getSkinValue()
            );
        }
    }

    public static void tailerPlayerMessage(ServerPlayer player, String message) {
        if (player instanceof TailoredPlayer) {
            TailoredPlayer tp = (TailoredPlayer) player;
            ServerEvents.getInstance().onServerChatEvent(
                    message,
                    player.getDisplayName().getString(),
                    tp.getSkinValue()
            );
        }
    }

}
