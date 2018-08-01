package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;
import android.util.SparseArray;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.rendering.Font.Font;
import com.juse.minigods.rendering.Font.TextCache;
import com.juse.minigods.rendering.Material.Material;
import com.juse.minigods.rendering.Material.MaterialBuilder;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_VERTEX_SHADER;

public class FontRenderer implements RendererInterface{
    private final static String VS = "font/vertex", FS = "font/fragment";

    private Font font;
    private TextCache textCache;
    private int renderPass;

    private HashMap<Integer, Material> textMaterials;

    public FontRenderer(Font font) {
        this.font = font;
        textCache = new TextCache();
        textMaterials = new HashMap<>();
    }

    private FloatBuffer getTextVertexData(TextCache.Text text) {
        int vertexComponents = 5; // x,y,z u,v = 5
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(countCharacter(text.getContent()) * vertexComponents * 6 * Float.BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        float x = text.getX(), y = text.getY(), scale = text.getScale(); // todo get coordinate from the cache
        // TODO Refactor this for performance, such as pre-calculating all this shiet
        // TODO Spacing and such should be loaded in the text
        for (char character : text.getContent().toCharArray()) {
            if (character == '\n') {
                y -= font.getCellHeight() / font.getImageHeight() * text.getYSpacing();
                x = text.getX();
            } else {
                Vector4f uvPosSize = font.getUvCoordinate(character);

                float endX = x + uvPosSize.z() * scale;
                float endY = y + uvPosSize.w() * scale;
                float endU = uvPosSize.x() + uvPosSize.z();
                float endV = uvPosSize.y() + uvPosSize.w();

                addVertexToBuffer(floatBuffer, x, y, 0.f, uvPosSize.x(), endV);
                addVertexToBuffer(floatBuffer, endX, y, 0.f, endU, endV);
                addVertexToBuffer(floatBuffer, x, endY, 0.f, uvPosSize.x(), uvPosSize.y());

                addVertexToBuffer(floatBuffer, endX, y, 0.f, endU, endV);
                addVertexToBuffer(floatBuffer, endX, endY, 0.f, endU, uvPosSize.y());
                addVertexToBuffer(floatBuffer, x, endY, 0.f, uvPosSize.x(), uvPosSize.y());

                x += uvPosSize.z() * scale * text.getXSpacing();
            }
        }

        floatBuffer.flip();
        return floatBuffer;
    }

    // TODO replace with better solution
    private int countCharacter(String content) {
        int counter = 0;
        for (char c : content.toCharArray())
            if (c != '\n')
                counter++;
        return counter;
    }

    private void addVertexToBuffer(FloatBuffer buffer, float x, float y, float z, float u, float v) {
        buffer.put(x);
        buffer.put(y);
        buffer.put(z);

        buffer.put(u);
        buffer.put(v);
    }

    @Override
    public void setup(ShaderManager shaderManager, MaterialManager materialManager, AssetManager assets) {
        font.createTexture();

        int vs, fs;
        try {
            vs = loadShader(shaderManager, assets, GL_VERTEX_SHADER, VS);
            fs = loadShader(shaderManager, assets, GL_FRAGMENT_SHADER, FS);
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.GRAPHICS, "Error loading: " +
                    "vs / fs in font renderer", e);
            return;
        }

        renderPass = materialManager.createRenderPass(vs, fs, shaderManager);
    }

    private void createTextMaterial(int key, MaterialManager materialManager, FloatBuffer vertexData) {
        MaterialBuilder materialBuilder = new MaterialBuilder();
        materialBuilder.setUniforms(new int[] {2}, DataUtils.ToBuffer(new Matrix4f()));
        materialBuilder.setVertices(vertexData, vertexData.capacity() / 5, GL_STATIC_DRAW,
                new int[] {3, 2}, new int[] {0, 1}, new int[] {Float.BYTES * 5, Float.BYTES * 5}, new int[] {0, Float.BYTES * 3});
        materialBuilder.setImageTexture(font.getImageTexture());

        Material textM = new Material(renderPass, materialBuilder);
        textMaterials.put(key, textM);
        materialManager.addMaterial(textM);
    }

    @Override
    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass, textMaterials.values().toArray(new Material[] {}));
    }

    @Override
    public void update(MaterialManager materialManager) {
        ArrayList<TextToUpdate> textsToUpdate = new ArrayList<>();

        Lock lock = textCache.getLock();

        lock.lock();
        try {
            // todo, some of these can probably be moved to the text cache class
            SparseArray<TextCache.Text> stringsToRender = textCache.getStringsToRender();
            for (int key : textCache.getStringsToUpdate()) {
                TextCache.Text text = stringsToRender.get(key, null);
                textsToUpdate.add(new TextToUpdate(text, key));
            }

            textCache.getStringsToUpdate().clear();
        } finally {
            lock.unlock();
        }

        // don't want to do all these calculations inside key as that with lock up the game, so those calculations can continue
        for (TextToUpdate textToUpdate : textsToUpdate) {
            TextCache.Text text = textToUpdate.text;
            int key = textToUpdate.key;

            if (text == null) {
                textMaterials.remove(key);
            } else {
                if (text.isNewlyAdded()) {
                    createTextMaterial(key, materialManager, getTextVertexData(text));
                } else {
                    textMaterials.remove(key);
                    createTextMaterial(key, materialManager, getTextVertexData(text));
                }
            }
        }
    }

    private class TextToUpdate {
        TextCache.Text text;
        int key;

        TextToUpdate(TextCache.Text text, int key) {
            this.text = text;
            this.key = key;
        }
    }

    public TextCache getTextCache() {
        return textCache;
    }
}
