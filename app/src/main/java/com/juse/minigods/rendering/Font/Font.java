package com.juse.minigods.rendering.Font;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

import com.juse.minigods.rendering.Material.ImageTexture;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Vector2i;
import org.joml.Vector4f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Load the font data and create the texture to use when rendering the font.
 */
public class Font {
    private static final String BITMAP_PATH = "textures/%s.png", FONT_DATA_PATH = "fontData/%s.csv";
    private static final String CHARACTER = "Char", BASE = "Base", WIDTH = "Width", HEIGHT = "Height",
                                OFFSET = "Offset", X_POSITION = "X", Y_POSITION = "Y",
                                CHAR_OFFSET = "Start", CELL = "Cell", IMAGE = "Image";

    private Bitmap bitmap;
    private ImageTexture imageTexture;
    private Vector2i imageDimension, cellDimension;
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
    }

    public void createTexture() {
        imageTexture = new ImageTexture(bitmap);
    }

    public ImageTexture getImageTexture() {
        return imageTexture;
    }

    private void loadFontCsvData(InputStream inputStream) throws IOException {
        cellDimension = new Vector2i();
        imageDimension = new Vector2i();

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
        } else if (lineType.equals(CELL)) {
            // todo refactor
            String split2[] = split[1].split(",");
            switch (split2[0]) {
                case WIDTH:
                    cellDimension.x = toInt(split2[1]);
                    break;
                case HEIGHT:
                    cellDimension.y = toInt(split2[1]);
                    break;
            }
        } else if (lineType.equals(IMAGE)) {
            // todo refactor
            String split2[] = split[1].split(",");
            switch (split2[0]) {
                case WIDTH:
                    imageDimension.x = toInt(split2[1]);
                    break;
                case HEIGHT:
                    imageDimension.y = toInt(split2[1]);
                    break;
            }
        } else if (lineType.equals(CHARACTER)) {
            Character character;
            if((character = characters.get(toInt(split[1]), null)) == null) {
                character = new Character();
                characters.put(toInt(split[1]), character);
            }

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

    // TODO, DO THIS IN THE CHARACTER CLASS INSTEAD OF LOADING THEM EVERYTIME AS THEY DO NOT CHANGE
    public Vector4f getUvCoordinate(char ch) {
        int index = ch - offset;
        Character character = characters.get(ch % 256); // characters in the csv file has their index but loops around so then character 256 has an index of 0.

        // these might not be correct for all fonts TODO
        int width = character.getBaseWidth();
        int height = cellDimension.y(); // todo change

        int columnsPerRow = imageDimension.x() / cellDimension.x();
        int pixelX = (index * cellDimension.x()) % imageDimension.x();
        int pixelY = (index / columnsPerRow) * height;

        return new Vector4f((float) pixelX / imageDimension.x(), (float) pixelY / imageDimension.y(),
                (float) width / imageDimension.x(), (float) height / imageDimension.y());
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

    public float getCellWidth() {
        return cellDimension.x();
    }

    public float getCellHeight() {
        return cellDimension.y();
    }

    public float getImageWidth() {
        return imageDimension.x();
    }

    public float getImageHeight() {
        return imageDimension.y();
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
