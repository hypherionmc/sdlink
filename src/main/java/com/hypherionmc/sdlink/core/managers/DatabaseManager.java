/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlink.core.database.HiddenPlayers;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import io.jsondb.JsonDBTemplate;
import io.jsondb.annotation.Document;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author HypherionSA
 * Helper class to initialize the JSON database
 */
public class DatabaseManager {

    public static final JsonDBTemplate sdlinkDatabase = new JsonDBTemplate("sdlinkstorage", "com.hypherionmc.sdlink.core.database");

    private static final Set<Class<?>> tables = new LinkedHashSet<>() {{
        add(SDLinkAccount.class);
        add(HiddenPlayers.class);
    }};

    static {
        sdlinkDatabase.setupDB(tables);
    }

    public static void initialize() {
        tables.forEach(t -> {
            if (!sdlinkDatabase.collectionExists(t)) {
                sdlinkDatabase.createCollection(t);
            }

            sdlinkDatabase.reloadCollection(t.getAnnotation(Document.class).collection());
        });
    }
}
