package com.hypherionmc.sdlink;

import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;

public class SafeCalls {

    public static String getTailorSkin(ServerPlayer player) {
        if (player instanceof TailoredPlayer tp) {
            return tp.getSkinId();
        }

        return player.getStringUUID();
    }

}