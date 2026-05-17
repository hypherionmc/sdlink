package com.hypherionmc.sdlink.core.jsondb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypherionmc.sdlink.core.jsondb.annotations.Document;
import com.hypherionmc.sdlink.core.jsondb.annotations.Id;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// TODO: Move this to it's own library
@Deprecated
@SuppressWarnings({"unchecked", "raw"})
public class JsonDatabase {

    private final File dbFolder;
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final Map<String, ReentrantReadWriteLock> locks = new HashMap<>();

    public JsonDatabase(String folderPath) {
        this.dbFolder = new File(folderPath);
        if (!dbFolder.exists()) dbFolder.mkdirs();
    }

    // Setup DB with initial collections
    public void setupDB(Set<Class<?>> tables) {
        tables.forEach(this::initializeCollection);
    }

    // Initialize collection file
    private void initializeCollection(Class<?> clazz) {
        String collectionName = getCollectionName(clazz);
        File collectionFile = new File(dbFolder, collectionName + ".json");
        locks.put(collectionName, new ReentrantReadWriteLock());
        if (!collectionFile.exists()) {
            try (Writer writer = new FileWriter(collectionFile)) {
                // Just keeping compat with the old DB engine
                writer.write("{\"schemaVersion\":\"1.0\"}\n");
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize collection: " + collectionName, e);
            }
        }
    }

    // Reload collection into memory (optional cache can be added)
    public <T> List<T> reloadCollection(String collectionName, Class<T> clazz) {
        if (!new File(dbFolder, collectionName + ".json").exists()) {
            initializeCollection(clazz);
        }

        lockRead(collectionName);
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(dbFolder, collectionName + ".json")))) {
            List<T> entries = new ArrayList<>();
            String line;
            boolean skipHeader = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;

                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                entries.add(gson.fromJson(line, clazz));
            }
            return entries;
        } catch (IOException e) {
            throw new RuntimeException("Failed to reload collection: " + collectionName, e);
        } finally {
            unlockRead(collectionName);
        }
    }

    // Add or update entry
    public void upsert(Object entry) {
        String collectionName = getCollectionName(entry.getClass());
        lockWrite(collectionName);
        try {
            List entries = reloadCollection(collectionName, entry.getClass());
            entries.removeIf(e -> getId(e).equals(getId(entry))); // Remove existing entry
            entries.add(entry);
            saveCollection(collectionName, entries);
        } finally {
            unlockWrite(collectionName);
        }
    }

    // Remove entry
    public void remove(Object entry) {
        String collectionName = getCollectionName(entry.getClass());
        lockWrite(collectionName);
        try {
            List entries = reloadCollection(collectionName, entry.getClass());
            entries.removeIf(e -> getId(e).equals(getId(entry)));
            saveCollection(collectionName, entries);
        } finally {
            unlockWrite(collectionName);
        }
    }

    // Find by ID
    public <T> T findById(Object id, Class<T> clazz) {
        String collectionName = getCollectionName(clazz);
        lockRead(collectionName);
        try {
            return reloadCollection(collectionName, clazz).stream()
                    .filter(entry -> id.equals(getId(entry)))
                    .findFirst()
                    .orElse(null);
        } finally {
            unlockRead(collectionName);
        }
    }

    // Get all entries
    public <T> List<T> getCollection(Class<T> clazz) {
        return reloadCollection(getCollectionName(clazz), clazz);
    }

    // Save collection back to file
    private void saveCollection(String collectionName, List<?> entries) {
        try (Writer writer = new FileWriter(new File(dbFolder, collectionName + ".json"))) {
            // Just keeping compat with the old DB engine
            writer.write("{\"schemaVersion\":\"1.0\"}\n");
            for (Object entry : entries) {
                writer.write(gson.toJson(entry) + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save collection: " + collectionName, e);
        }
    }

    private String getCollectionName(Class<?> clazz) {
        Document annotation = clazz.getAnnotation(Document.class);
        return annotation != null ? annotation.collection() : clazz.getSimpleName();
    }

    private Object getId(Object entry) {
        Class<?> clazz = entry.getClass();
        try {
            for (var field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true); // Access private fields
                    return field.get(entry);
                }
            }
            throw new RuntimeException("No field annotated with @Id found in: " + clazz.getSimpleName());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access @Id field in: " + clazz.getSimpleName(), e);
        }
    }

    // Thread-safety locks
    private void lockRead(String collectionName) {
        locks.get(collectionName).readLock().lock();
    }
    private void unlockRead(String collectionName) {
        locks.get(collectionName).readLock().unlock();
    }
    private void lockWrite(String collectionName) {
        locks.get(collectionName).writeLock().lock();
    }
    private void unlockWrite(String collectionName) {
        locks.get(collectionName).writeLock().unlock();
    }

}
