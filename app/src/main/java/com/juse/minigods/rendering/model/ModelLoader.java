package com.juse.minigods.rendering.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.Nullable;

public class ModelLoader {
    static {
        System.loadLibrary("game_engine");
    }

    synchronized native void prepareModelLoading(String internalDataFilePath, AssetManager assetManager);
    synchronized native void loadModel(String name, Model model);
    synchronized native void cleanup();

    public synchronized @Nullable Model[] loadModels(String fileNames[], Context context, AssetManager assetManager) {
        String filePath = getInternalDataFilePath(context);

        if (filePath == null) {
            return null; // todo
        }

        prepareModelLoading(getInternalDataFilePath(context), assetManager);
        Model models[] = new Model[fileNames.length];
        for (int index = 0; index < fileNames.length; index++) { // todo, change so entire list is sent instead
            models[index] = new Model();
            loadModel(fileNames[index], models[index]);
        }
        cleanup();

        return models;
    }

    private String getInternalDataFilePath(Context context) {
        return context.getFilesDir() != null ? context.getFilesDir().getAbsolutePath() : null;
    }
}
