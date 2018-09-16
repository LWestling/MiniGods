//
// Created by LukasW on 2018-08-11.
//

#ifndef MINIGODS_MODELLOADER_H
#define MINIGODS_MODELLOADER_H

#include <assimp/port/AndroidJNI/AndroidJNIIOSystem.h>
#include <android_native_app_glue.h>
#include <jni.h>
#include <string>

// Java callable function
#define JCF(func) Java_com_juse_minigods_(func)
#define FFF(ret)
#define JCF2(returnType, func) JNIEXPORT returnType JNICALL Java_com_juse_minigods_(func)

class ModelLoader {
private:
public:
};


#endif //MINIGODS_MODELLOADER_H
