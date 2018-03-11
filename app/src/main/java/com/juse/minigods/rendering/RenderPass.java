package com.juse.minigods.rendering;

import static android.opengl.GLES31.glLinkProgram;
import static android.opengl.GLES31.glCreateProgram;

/**
 * Created by LukasW on 2018-03-08.
 * A render pass
 */

public class RenderPass {
    private int vertexShader, fragmentShader, program;

    public RenderPass(int vertexShader, int fragmentShader, ShaderManager manager) {
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;

        program = glCreateProgram();
        manager.bindShaders(program, vertexShader, fragmentShader);
        glLinkProgram(program);
    }

    public int getVertexShader() {
        return vertexShader;
    }

    public int getFragmentShader() {
        return fragmentShader;
    }

    public int getProgram() {
        return program;
    }
}
