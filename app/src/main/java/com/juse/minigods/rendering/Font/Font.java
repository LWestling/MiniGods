package com.juse.minigods.rendering.Font;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

import com.juse.minigods.rendering.Material.ImageTexture;
import com.juse.minigods.reporting.CrashManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Font {
    private static final String BITMAP_PATH = "textures/%s.png", FONT_DATA_PATH = "fontData/%s.csv";
    private static final String CHARACTER = "Char", BASE = "Base", WIDTH = "Width", OFFSET = "Offset",
                                X_POSITION = "X", Y_POSITION = "Y", CHAR_OFFSET = "Start";

    private Bitmap bitmap;
    private ImageTexture imageTexture;
    private SparseArray<Character> characters = new SparseArray<>();
    private InputStream csvStream;
    private int offset;

    public Font(String bitmapName, String fontCsvName, AssetManager assetManager) {
        try {
            loadFontFiles(assetManager, bitmapName, fontCsvName);
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.IO, "Error loading font", e);
            return;
        }

        try {
            loadFontCsvData(csvStream);
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.IO, "Error loading font csv data " + fontCsvName, e);
        }

        try {
            createTexture();
        } catch (Exception e) {
            CrashManager.ReportCrash(CrashManager.CrashType.GRAPHICS, "Error creating font texture", e);
        }
    }

    private void createTexture() {
        imageTexture = new ImageTexture(bitmap);
    }

    public ImageTexture getImageTexture() {
        return imageTexture;
    }

    private void loadFontCsvData(InputStream inputStream) throws IOException {
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {

            String csvLine;
            offset = 0;
            while ((csvLine = bufferedReader.readLine()) != null) {
                loadFontCsvData(csvLine);
            }
        }
    }

    private void loadFontCsvData(String csvLine) {
        String split[] = csvLine.split( " ");

        String lineType = split[0];

        if (lineType.equals(CHAR_OFFSET)) {
            offset = getValue(split[1]);
        } else if (lineType.equals(CHARACTER)) {
            Character character = characters.get(toInt(split[1]) + offset, new Character());

            switch (split[2]) {
                case BASE:
                    character.setBaseWidth(getValue(split[3]));
                    break;
                case X_POSITION:
                    character.setXOffset(getValue(split[3]));
                    break;
                case Y_POSITION:
                    character.setYOffset(getValue(split[3]));
                    break;
                case WIDTH:
                    character.setWidthOffset(getValue(split[3]));
                    break;
            }
        }
    }

    // Last line is always something,VALUE there is probably more but right now don't know
    private int getValue(String s) {
        return toInt(s.split(",")[1]);
    }

    private int toInt(String s) {
        return Integer.parseInt(s);
    }

    private void loadFontFiles(AssetManager assetManager, String bitmapName, String fontCsvName) throws IOException {
        bitmap = BitmapFactory.decodeStream(assetManager.open(String.format(BITMAP_PATH, bitmapName)));
        csvStream = assetManager.open(String.format(FONT_DATA_PATH, fontCsvName));
    }

    private class Character {
        private int xOffset, yOffset, widthOffset, baseWidth;

        public int getXOffset() {
            return xOffset;
        }

        public void setXOffset(int xOffset) {
            this.xOffset = xOffset;
        }

        public int getYOffset() {
            return yOffset;
        }

        public void setYOffset(int yOffset) {
            this.yOffset = yOffset;
        }

        public int getWidthOffset() {
            return widthOffset;
        }

        public void setWidthOffset(int widthOffset) {
            this.widthOffset = widthOffset;
        }

        public int getBaseWidth() {
            return baseWidth;
        }

        public void setBaseWidth(int baseWidth) {
            this.baseWidth = baseWidth;
        }
    }
}
