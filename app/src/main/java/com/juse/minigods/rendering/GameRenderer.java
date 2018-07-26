package com.juse.minigods.rendering;

import android.content.res.AssetManager;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.rendering.renderers.RendererInterface;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES32.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES32.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES32.GL_DONT_CARE;
import static android.opengl.GLES32.GL_FRAGMENT_SHADER;
import static android.opengl.GLES32.GL_VERTEX_SHADER;
import static android.opengl.GLES32.glDebugMessageControl;
import static com.juse.minigods.Utils.Constants.DEBUGGING;
import static com.juse.minigods.Utils.Constants.SHOW_OGL_DEBUG;

/**
 * Created by LukasW on 2018-03-08.
 * Implements Renderer
 */

public class GameRenderer implements GLSurfaceView.Renderer {
    private final static String VERTEX_SHADER = "vertex", FRAGMENT_SHADER = "fragment";
    private ShaderManager shaderManager;
    private AssetManager assetManager;
    private MaterialManager materialManager;
    private Material testMaterial;
    private Vector3f test = new Vector3f(0.f, 0.f, -3.f);
    private float test2 = 0;

    // Renderers
    private ArrayList<RendererInterface> rendererList;

    private int vertexShader, fragmentShader, renderPass;
    // test data
    private Vector3f triangle[] = {
            new Vector3f(-1.f, 0.f, -1.f),
            new Vector3f(-1.f, 0.f, 1.f),
            new Vector3f(1.f, 0.f, -1.f)
    };

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
                glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE,
                        0, DataUtils.newIntBuffer(0), true);

              //  GLES32.glDebugMessageCallback(new OglDebugCallback()); wow NYI Cx
            }
        }

        GLES31.glClearColor(0.95f, 0.05f, 0.05f, 0.95f);
        createShaders(assetManager);

        renderPass = materialManager.createRenderPass(vertexShader, fragmentShader, shaderManager);
        testMaterial = new Material(renderPass, DataUtils.ToBuffer(triangle), 0,
                3, DataUtils.ToBuffer(new Matrix4f().translate(test)),
                2);
        materialManager.addMaterial(testMaterial);

        rendererList.forEach(rendererInterface -> rendererInterface.setup(shaderManager, materialManager, assetManager));
    }

    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES31.glViewport(0, 0, width, height);
    }

    public void onDrawFrame(GL10 gl10) {
        GLES31.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // 4 testing
        test2 += 0.006f;
        testMaterial.updateUniform(DataUtils.ToBuffer(new Matrix4f().translate(test)), 0);

        materialManager.render(renderPass);
        rendererList.forEach(rendererInterface -> rendererInterface.render(shaderManager, materialManager));

        CrashManager.HandleOpenGlErrors();
    }

    private void createShaders(AssetManager assetManager) {
        try {
            vertexShader = shaderManager.createShader(
                    GL_VERTEX_SHADER,
                    assetManager.open(shaderManager.getShaderPath(VERTEX_SHADER))
            );

            fragmentShader = shaderManager.createShader(
                    GL_FRAGMENT_SHADER,
                    assetManager.open(shaderManager.getShaderPath(FRAGMENT_SHADER))
            );

            if (vertexShader != 0 && fragmentShader != 0) {
                System.out.println("Compile success!");
            } else {
                CrashManager.ReportCrash(CrashManager.CrashType.GRAPHICS,
                        "Error creating shaders", new Exception());
            }
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.IO, "Failure to open a shader", e);
        }
    }
}
