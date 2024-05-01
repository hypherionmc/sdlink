/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import io.jsondb.JsonDBTemplate;

import java.util.Collections;

/**
 * @author HypherionSA
 * Helper class to initialize the JSON database
 */
public class DatabaseManager {

    public static final JsonDBTemplate sdlinkDatabase = new JsonDBTemplate("sdlinkstorage", "com.hypherionmc.sdlink.core.database");

    static {
        sdlinkDatabase.setupDB(Collections.singleton(SDLinkAccount.class));
    }

    public static void initialize() {
        if (!sdlinkDatabase.collectionExists(SDLinkAccount.class)) {
            sdlinkDatabase.createCollection(SDLinkAccount.class);
        }

        sdlinkDatabase.reloadCollection("verifiedaccounts");
    }
}
