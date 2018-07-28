package com.juse.minigods.rendering.Material;

import android.graphics.Bitmap;
import android.opengl.GLES31;
import android.opengl.GLUtils;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES31.GL_TEXTURE_2D;
import static android.opengl.GLES31.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES31.GL_TEXTURE_MIN_FILTER;

/**
 * Texture for an image from the disk, should not be used for shadow maps etc.
 */
public class ImageTexture {
    private int textureId[];

    public ImageTexture(Bitmap bitmap) {
        createImage(bitmap);
    }

    private void createImage(Bitmap bitmap) {
        textureId = new int[1];

        GLES31.glGenTextures(textureId.length, textureId, 0);

        glBindTexture(GL_TEXTURE_2D, textureId[0]);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES31.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GLES31.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public int getTextureId() {
        return textureId[0];
    }
}
