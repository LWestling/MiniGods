package com.juse.minigods.rendering;

import android.content.res.AssetManager;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.os.Build;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.rendering.renderers.RendererInterface;
import com.juse.minigods.reporting.CrashManager;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES32.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES32.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES32.GL_DONT_CARE;
import static android.opengl.GLES32.glDebugMessageControl;
import static com.juse.minigods.Utils.Constants.DEBUGGING;
import static com.juse.minigods.Utils.Constants.SHOW_OGL_DEBUG;

/**
 * Created by LukasW on 2018-03-08.
 * Implements Renderer
 */

public class GameRenderer implements GLSurfaceView.Renderer {
    private ShaderManager shaderManager;
    private AssetManager assetManager;
    private MaterialManager materialManager;

    private ArrayList<RendererInterface> rendererList;

    public GameRenderer(AssetManager assetManager) {
        super();
        shaderManager = new ShaderManager();
        materialManager = new MaterialManager();
        this.assetManager = assetManager;
    }

    public void setupRendererList(ArrayList<RendererInterface> interfaces) {
        rendererList = interfaces;
    }

    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        if (DEBUGGING) {
            if (SHOW_OGL_DEBUG) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE,
                            0, DataUtils.newIntBuffer(0), true);
                }
            }
        }

        GLES31.glClearColor(0.f, 148.f / 255.f, 1.f, 1.f);

        for (RendererInterface rendererInterface : rendererList) {
            rendererInterface.setup(shaderManager, materialManager, assetManager);
        }
    }

    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES31.glViewport(0, 0, width, height);
    }

    public void onDrawFrame(GL10 gl10) {
        GLES31.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // obvious bad multithreading
        for (RendererInterface anInterface : rendererList) {
            anInterface.update(materialManager);
        }
        for (RendererInterface rendererInterface : rendererList) {
            rendererInterface.render(shaderManager, materialManager);
        }

        CrashManager.HandleOpenGlErrors();
    }
}
