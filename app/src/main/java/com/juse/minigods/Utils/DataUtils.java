package com.juse.minigods.Utils;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by LukasW on 2018-03-11.
 * Just some help stuff
 */

public class DataUtils {
    private final static int VECTOR_SIZE = 3;

    public static FloatBuffer ToBuffer(Vector3f arr[]) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(arr.length * Float.BYTES * VECTOR_SIZE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        for (Vector3f vec : arr) {
            vec.get(buffer);
            buffer.position(buffer.position() + VECTOR_SIZE);
        }

        buffer.flip();
        return buffer;
    }

    public static FloatBuffer ToBuffer(Matrix4f mat) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(16 * Float.BYTES) // 4x4 matrix
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mat.get(buffer);

        return buffer;
    }

    public static IntBuffer newIntBuffer(int cap) {
        return ByteBuffer.
                allocateDirect(cap * Integer.BYTES).
                order(ByteOrder.nativeOrder()).
                asIntBuffer();
    }
}
