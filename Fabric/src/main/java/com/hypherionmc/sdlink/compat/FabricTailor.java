package com.hypherionmc.sdlink.compat;

import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;

public class FabricTailor {

    public static String getTailorSkin(ServerPlayer player) {
        if (player instanceof TailoredPlayer tp) {
            return tp.getSkinId();
        }

        return player.getStringUUID();
    }

}