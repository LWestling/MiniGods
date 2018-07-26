package com.juse.minigods.reporting;

import android.opengl.GLES32;

/**
 * Created by LukasW on 2018-03-25.
 * Please work for all that is holy, working with this
 * sucks ass, jesus fucking christ.
 */

public class OglDebugCallback implements GLES32.DebugProc {
    public void onMessage(int i, int i1, int i2, int i3, String s) {
        System.out.println(s);
    }
}
