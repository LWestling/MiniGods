package com.juse.minigods.Utils;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by LukasW on 2018-03-11.
 * Just some help stuff
 */

public class DataUtils {
    public static FloatBuffer ToBuffer(Vector3f arr[]) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(arr.length * 4 * 3) // 4 bytes per float, 3 floats per vector
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        for (Vector3f vec : arr) {
            vec.get(buffer);
            buffer.position(buffer.position() + 3);
        }

        buffer.flip();
        return buffer;
    }

    public static FloatBuffer ToBuffer(Matrix4f mat) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(4 * 4 * 4) // 4x4 matrix with 4 bytes per float
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mat.get(buffer);

        return buffer;
    }
}
