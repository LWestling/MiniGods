package com.juse.minigods;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.juse.minigods.rendering.GameRenderer;

public class MainActivity extends Activity {
    private GLSurfaceView glSurfaceView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLConfigChooser(true);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(new GameRenderer(getAssets()));

        setContentView(glSurfaceView);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}
