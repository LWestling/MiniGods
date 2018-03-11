package com.juse.minigods.rendering;

import android.opengl.GLES31;

import com.juse.minigods.Utils.DataUtils;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

/**
 * Created by LukasW on 2018-03-11.
 * Simple class to do the camera projection stuff
 */

public class CameraProjectionManager {
    private Matrix4f camera, projection;
    private FloatBuffer buffer;

    public CameraProjectionManager() {
        createMatrices();
    }

    public void bindGraphicsData(int location) {
        GLES31.glUniformMatrix4fv(location, 1, false, buffer);
    }

    private void createMatrices() {
        camera = new Matrix4f(); // TODO set aspect to actually width / height
        projection = new Matrix4f().setPerspective(90.f, 0.8f, 0.1f, 100.f);
    }

    public void updateCamera(Vector3f cameraPos, Vector3f dir) {
        camera = new Matrix4f().lookAt(cameraPos, dir, new Vector3f(0.f, 1.f, 0.f));
        buffer = DataUtils.ToBuffer(projection.mul(camera));
    }
}
