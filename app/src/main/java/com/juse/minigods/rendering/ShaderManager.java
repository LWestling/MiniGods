package com.juse.minigods.rendering;

import android.opengl.GLES31;

import com.juse.minigods.reporting.CrashManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.opengl.GLES31.GL_COMPILE_STATUS;
import static android.opengl.GLES31.GL_FALSE;
import static android.opengl.GLES31.glCompileShader;
import static android.opengl.GLES31.glCreateShader;
import static android.opengl.GLES31.glDeleteShader;
import static android.opengl.GLES31.glGetShaderInfoLog;
import static android.opengl.GLES31.glGetShaderiv;
import static android.opengl.GLES31.glShaderSource;

/**
 * Created by LukasW on 2018-03-08.
 * Manage shaders
 */

public class ShaderManager {
    private final static String SHADER_TAG = "shaderOutput";
    public final static String SHADER_PATH_FORMAT = "shaders/%s.glsl";

    void bindShaders(int program, int vertex, int fragment) {
        GLES31.glAttachShader(program, vertex);
        GLES31.glAttachShader(program, fragment);
    }

    void unbindShaders(int program, int vertex, int fragment) {
        GLES31.glDetachShader(program, vertex);
        GLES31.glDetachShader(program, fragment);
    }

    void deleteShader(int shader) {
        GLES31.glDeleteShader(shader);
    }

    public int createShader(int shaderType, InputStream stream) throws IOException {
        int shader = glCreateShader(shaderType);

        if (shader != 0) {
            glShaderSource(shader, readStream(stream));
            glCompileShader(shader);

            final int[] ret = new int[1];
            glGetShaderiv(shader, GL_COMPILE_STATUS, ret, 0);

            // returns false if failed to compile
            if (ret[0] == GL_FALSE)
            {
                String error = glGetShaderInfoLog(shader);
                CrashManager.ReportCrash(CrashManager.CrashType.GRAPHICS,
                        "Could not compile shader type " + shaderType,
                        new RuntimeException(error));

                glDeleteShader(shader);
                shader = 0;
            }
        }

        stream.close();
        return shader;
    }

    private String readStream(InputStream stream) {
        // this shit is why java can be awful
        StringBuilder str = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                str.append(line).append('\n');
            }
        } catch (Exception e) {
            return "NULL";
        }

        return str.toString();
    }

    String getShaderPath(String name){
        return "shaders/" + name + ".glsl";
    }
}
