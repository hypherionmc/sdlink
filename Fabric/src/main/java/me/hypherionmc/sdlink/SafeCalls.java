package me.hypherionmc.sdlink;

import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;

/**
 * @author HypherionSA
 * @date 29/10/2022
 */
public class SafeCalls {

    public static String getTailorSkin(ServerPlayer player) {
        if (player instanceof TailoredPlayer tp) {
            return tp.getSkinId();
        }

        return player.getStringUUID();
    }

}
