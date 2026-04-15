/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlink.core.database.HiddenPlayers;
import com.hypherionmc.sdlink.core.jsondb.JsonDatabase;
import com.hypherionmc.sdlink.core.jsondb.annotations.Document;
import com.hypherionmc.sdlinkrw.modules.database.SDLWebhooks;
import com.hypherionmc.sdlinkrw.modules.database.SDLinkAccount;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author HypherionSA
 * Helper class to initialize the JSON database
 */
public final class DatabaseManager {

    public static final DatabaseManager INSTANCE = new DatabaseManager();

    private final JsonDatabase sdlinkDatabase = new JsonDatabase("sdlinkstorage");

    private final Set<Class<?>> tables = new LinkedHashSet<>() {{
        add(SDLinkAccount.class);
        add(HiddenPlayers.class);
        add(SDLWebhooks.class);
    }};

    DatabaseManager() {
        sdlinkDatabase.setupDB(tables);
    }

    public void initialize() {
        tables.forEach(t -> sdlinkDatabase.reloadCollection(t.getAnnotation(Document.class).collection(), t));
    }

    public void updateEntry(Object t) {
        sdlinkDatabase.upsert(t);
        reload(t.getClass());
    }

    public void deleteEntry(Object t) {
        sdlinkDatabase.remove(t);
        reload(t.getClass());
    }

    public void deleteEntry(Object t, Class<?> clazz) {
        sdlinkDatabase.remove(t);
        reload(t.getClass());
    }

    private void reload(Class<?> clazz) {
        sdlinkDatabase.reloadCollection(clazz.getAnnotation(Document.class).collection(), clazz);
    }

    public <T> T findById(Object id, Class<T> entityClass) {
        reload(entityClass);
        return sdlinkDatabase.findById(id, entityClass);
    }

    public <T> List<T> getCollection(Class<T> entityClass) {
        reload(entityClass);
        return sdlinkDatabase.getCollection(entityClass);
    }

    public <T> List<T> findAll(Class<T> tClass) {
        reload(tClass);
        return sdlinkDatabase.getCollection(tClass);
    }
}
