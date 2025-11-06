package com.boes.sage.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonStorageManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File dataFile;

    public JsonStorageManager(File dataFile) {
        this.dataFile = dataFile;
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
                save(new JsonObject());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public JsonObject load() {
        try {
            if (dataFile.length() == 0) {
                return new JsonObject();
            }
            return gson.fromJson(new FileReader(dataFile), JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new JsonObject();
        }
    }

    public void save(JsonObject json) {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(JsonObject json) {
        save(json);
    }

    public JsonElement get(String key) {
        return load().get(key);
    }

    public boolean has(String key) {
        return load().has(key);
    }
}