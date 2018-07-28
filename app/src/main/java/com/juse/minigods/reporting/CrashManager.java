package com.juse.minigods.reporting;

import android.opengl.GLES31;
import android.opengl.GLUtils;

import static android.opengl.GLES31.*;

/**
 * Created by LukasW on 2018-03-08.
 */

public class CrashManager {
    public enum CrashType { NULL, IO, GRAPHICS, LOGIC, ANDROID }

    public static void ReportCrash(CrashType crashType, String message, Exception exception) {
        // NYI TODO FIREBASE CRASH REPORTING
        System.out.println(crashType.name() + ": " + message);
        exception.printStackTrace();
    }

    public static void HandleOpenGlErrors() {
        int error = GLES31.glGetError();
        while (error != GLES31.GL_NO_ERROR) {
            String msg;
            switch(error) {
                case GL_INVALID_OPERATION:      msg="INVALID_OPERATION";      break;
                case GL_INVALID_ENUM:           msg="INVALID_ENUM";           break;
                case GL_INVALID_VALUE:          msg="INVALID_VALUE";          break;
                case GL_OUT_OF_MEMORY:          msg="OUT_OF_MEMORY";          break;
                case GL_INVALID_FRAMEBUFFER_OPERATION:  msg="INVALID_FRAMEBUFFER_OPERATION";  break;
                default: msg = "WTF";
            }
            System.out.println(msg + " : " + GLUtils.getEGLErrorString(error));
            error = GLES31.glGetError();
        }
    }
}
