package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;
import com.hypherionmc.sdlinkrw.modules.database.HiddenPlayers;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Getter
public final class HiddenPlayersManager {

    public static final HiddenPlayersManager INSTANCE = new HiddenPlayersManager();
    private final HashMap<String, HiddenPlayers> hiddenPlayers = new LinkedHashMap<>();

    private HiddenPlayersManager() {}

    public void loadHiddenPlayers() {
        hiddenPlayers.clear();
        DatabaseManager.INSTANCE.getCollection(HiddenPlayers.class).forEach(p -> hiddenPlayers.put(p.getIdentifier(), p));
    }

    public Result hidePlayer(String identifier, String displayName, String type) {
        try {
            HiddenPlayers player = HiddenPlayers.of(identifier, displayName, type);
            DatabaseManager.INSTANCE.updateEntry(player);
            hiddenPlayers.put(identifier, player);
            return Result.success(SDText.translate("hiding.now_hidden", displayName));
        } catch (Exception e) {
            SDLinkConstants.LOGGER.error("Failed to hide player {}", displayName, e);
            return Result.error(SDText.translate("hiding.failed", e.getMessage()));
        }
    }

    public Result unhidePlayer(String identifier) {
        try {
            HiddenPlayers player = DatabaseManager.INSTANCE.findById(identifier, HiddenPlayers.class);

            if (player == null) {
                return Result.error(SDText.translate("hiding.not_hidden"));
            }

            hiddenPlayers.remove(identifier);
            DatabaseManager.INSTANCE.deleteEntry(player);
            return Result.success(SDText.translate("hiding.unhidden", player.getDisplayName()));
        } catch (Exception e) {
            SDLinkConstants.LOGGER.error("Failed to unhide player {}", identifier, e);
            return Result.error(SDText.translate("hiding.unhide_failed", e.getMessage()));
        }
    }

    public boolean isPlayerHidden(String identifier) {
        return hiddenPlayers.containsKey(identifier);
    }

}
