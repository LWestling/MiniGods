package com.juse.minigods.rendering.model;

/**
 * Generic Model converted aiScene (cpp) (assimp)
 */
public class Model {
    public float[] vertices;
    public int[] indices;
    public String[] textures;
    public Bone[] bones;
    public Animation[] animations;

    public boolean useNormals, useAnimations;
}
