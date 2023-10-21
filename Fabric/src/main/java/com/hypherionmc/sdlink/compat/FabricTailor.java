package com.hypherionmc.sdlink.compat;

import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;

public class FabricTailor {

    public static String getTailorSkin(ServerPlayer player) {
        try {
           if (player instanceof TailoredPlayer tp) {
               return tp.getSkinId();
           }
        } catch (Exception e) {
           e.printStackTrace();
        }

        return player.getStringUUID();
    }

}