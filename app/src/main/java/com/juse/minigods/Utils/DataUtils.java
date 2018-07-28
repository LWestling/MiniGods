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

    public static FloatBuffer ToBuffer(float arr[]) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(arr.length * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        buffer.put(arr);

        buffer.flip();
        return buffer;
    }

    public static IntBuffer ToBuffer(int arr[]) {
        IntBuffer buffer = ByteBuffer.allocateDirect(arr.length * Integer.BYTES)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        buffer.put(arr);
        buffer.flip();

        return buffer;
    }

    // Fucking java
    public static IntBuffer ToBuffer(Integer arr[]) {
        int intArr[] = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            intArr[i] = arr[i];
        }

        return ToBuffer(intArr);
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
