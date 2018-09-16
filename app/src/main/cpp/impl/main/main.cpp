//
// Created by LukasW on 2018-09-04.
//

#include <jni.h>
#include <android/native_activity.h>
#include <android_native_app_glue.h>
#include "rendering/model/ModelLoader.h"
#include "rendering/OGLManager.h"

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "native-activity", __VA_ARGS__))
/*
ANativeActivity *activity;
ModelLoader *modelLoader;

void android_main(struct android_app* state) {
    // this has to be called for some unknown reason
    app_dummy();

    activity = state->activity;
    modelLoader = new ModelLoader(activity);

    modelLoader->loadModel("models/dog/BeagleDefault.fbx");

    OGLManager manager(state);

    while (1) {
        sleep(5);
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_juse_minigods_rendering_model_ModelLoader_loadModel(JNIEnv *env, jobject instance,
                                                             jstring fileName_) {
    const char *fileName = env->GetStringUTFChars(fileName_, 0);
    jstring str = modelLoader->loadModel(env, fileName);
    env->ReleaseStringUTFChars(fileName_, fileName);
    return str;
}
 */