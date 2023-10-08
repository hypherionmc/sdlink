package me.hypherionmc.sdlink;

import me.hypherionmc.sdlinklib.config.ModConfig;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;

/**
 * @author HypherionSA
 * @date 29/10/2022
 */
public class SafeCalls {

    public static String getTailorSkin(ServerPlayer player) {
        try {
            if (player instanceof TailoredPlayer tp) {
                return tp.getSkinId();
            }
        } catch (Exception e) {
            if (ModConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOG.error("Failed to retrieve player skin from Fabric Tailor", e);
            }
        }

        return player.getStringUUID();
    }

}
