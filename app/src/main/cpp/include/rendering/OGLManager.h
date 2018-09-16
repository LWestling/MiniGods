//
// Created by LukasW on 2018-09-14.
//

#ifndef MINIGODS_OGLMANAGER_H
#define MINIGODS_OGLMANAGER_H

#include <GLES3/gl3.h>
#include <EGL/egl.h>

class OGLManager {
private:
    enum OGL_RETURN_CODE {
        NONE,
        MAKE_CURRENT_FAIL
    };

    OGL_RETURN_CODE initContext(struct android_app *engine);
public:
    OGLManager(struct android_app *engine);
    ~OGLManager();
};


#endif //MINIGODS_OGLMANAGER_H
