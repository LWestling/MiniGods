//
// Created by LukasW on 2018-08-11.
//

#include "rendering/model/ModelLoader.h"
#include <assimp/Importer.hpp>
#include <assimp/scene.h>
#include <assimp/postprocess.h>

#define add3DVecComponents(vec3d, vec) \
    vec.push_back(vec3d.x); \
    vec.push_back(vec3d.y); \
    vec.push_back(vec3d.z);

#define add2DVecComponents(vec3d, vec) \
    vec.push_back(vec3d.x); \
    vec.push_back(vec3d.y);

static Assimp::AndroidJNIIOSystem *androidJNIIOSystem = nullptr;
static Assimp::Importer *importer = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_juse_minigods_rendering_model_ModelLoader_prepareModelLoading(JNIEnv *env,
                                                                       jobject instance,
                                                                       jstring internalDataFilePath_,
                                                                       jobject assetManager) {
    const char *internalDataFilePath = env->GetStringUTFChars(internalDataFilePath_, 0);

    // TODO
    AAssetManager *assetManagerJava = AAssetManager_fromJava(env, assetManager);
    std::string dataPath(internalDataFilePath);

    androidJNIIOSystem = new Assimp::AndroidJNIIOSystem(dataPath, assetManagerJava);
    importer = new Assimp::Importer();
    importer->SetIOHandler(androidJNIIOSystem);

    env->ReleaseStringUTFChars(internalDataFilePath_, internalDataFilePath);
}

jfieldID setFloatField(JNIEnv *env, jclass c, jobject obj, float val, const char *name);
void setJavaFloatArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, float values[]);
void setJavaIntArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, int *values);
void setJavaStringArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, const char **values);
void setJavaBoolean(JNIEnv *env, jclass c, jobject obj, jboolean val, const char *name);

extern "C"
JNIEXPORT void JNICALL
Java_com_juse_minigods_rendering_model_ModelLoader_loadModel(JNIEnv *env, jobject instance,
                                                             jstring fileName, jobject model) {
    const char *name = env->GetStringUTFChars(fileName, 0);
    jclass modelClass = static_cast<jclass>(env->GetObjectClass(model));

    /*
     * THIS IS A HEAVY WORK IN PROGRESS
     * AND PROBABLY WILL BE FOR A LONG TIME
     *
     * JUST WANT TO GET SOMETHING THAT WORKS
     *
     */

    const aiScene *scene = importer->ReadFile(name, aiProcess_Triangulate | aiProcess_ConvertToLeftHanded);

    aiMesh *mesh = scene->mMeshes[0]; // only supports 1 mesh atm
    aiVector3D *vec = scene->mMeshes[0]->mVertices;

    if (mesh->HasNormals() && scene->HasMaterials()) {
        aiMaterial *material = scene->mMaterials[0]; // only supports 1 material atm

        int textureCount = material->GetTextureCount(aiTextureType_DIFFUSE);
        std::vector<const char*> textures;
        for (unsigned int i = 0; i < textureCount; i++) {
            aiString path;
            if(material->GetTexture(aiTextureType_DIFFUSE, i, &path) == aiReturn_SUCCESS) {
                textures.push_back(path.C_Str());
            }
        }

        setJavaStringArray(env, modelClass, model, "textures", textureCount, textures.data());
    }

    std::vector<jfloat> floatVec;
    if (mesh->HasNormals()) {
        aiVector3D *normals = scene->mMeshes[0]->mNormals;
        for (int i = 0; i < mesh->mNumVertices; i++) {
            add3DVecComponents(vec[i], floatVec);
            add3DVecComponents(normals[i], floatVec);

            // only support 1 uv channel and 2 dimensions (TODO ONE IF PER LOOP OMEGALUL)
            if (mesh->HasTextureCoords(0)) {
                add2DVecComponents(mesh->mTextureCoords[0][i], floatVec);
            }
        }
        setJavaBoolean(env, modelClass, model, (jboolean) true, "useNormals");
    } else {
        for (int i = 0; i < mesh->mNumVertices; i++) {
            add3DVecComponents(vec[i], floatVec);
        }
        setJavaBoolean(env, modelClass, model, (jboolean) false, "useNormals");
    }

    setJavaFloatArray(env, modelClass, model, "vertices", static_cast<int>(floatVec.size()), floatVec.data());
    floatVec.clear();

    if (mesh->HasFaces()) {
        std::vector<jint> intVec;
        for (int i = 0; i < mesh->mNumFaces; i++) {
            aiFace face = mesh->mFaces[i];
            for (int j = 0; j < face.mNumIndices; j++)
                intVec.push_back(face.mIndices[j]);
        }
        setJavaIntArray(env, modelClass, model, "indices", static_cast<int>(intVec.size()), intVec.data());
    }

    env->ReleaseStringUTFChars(fileName, name);
    importer->FreeScene();
}

jfieldID setFloatField(JNIEnv *env, jclass c, jobject obj, float val, const char *name) {
    jfieldID fieldId = env->GetFieldID(c, name, "F");
    env->SetFloatField(obj, fieldId, val);
    return NULL;
}

void setJavaBoolean(JNIEnv *env, jclass c, jobject obj, jboolean val, const char *name) {
    jfieldID fieldId = env->GetFieldID(c, name, "Z");
    env->SetBooleanField(obj, fieldId, val);
}

void setJavaFloatArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, jfloat *values) {
    jfieldID fieldId = env->GetFieldID(c, name, "[F");
    jfloatArray array = env->NewFloatArray(valueLen);

    env->SetFloatArrayRegion(array, 0, valueLen, values);
    env->SetObjectField(obj, fieldId, array);
}

void setJavaIntArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, jint *values) {
    jfieldID fieldId = env->GetFieldID(c, name, "[I");
    jintArray array = env->NewIntArray(valueLen);

    env->SetIntArrayRegion(array, 0, valueLen, values);
    env->SetObjectField(obj, fieldId, array);
}

void setJavaStringArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, const char **values) {
    jfieldID fieldId = env->GetFieldID(c, name, "[Ljava/lang/String;");
    jclass stringClass = static_cast<jclass>(env->FindClass("java/lang/String"));
    jobjectArray array = env->NewObjectArray(valueLen, stringClass, 0);

    for (int i = 0; i < valueLen; i++) {
        env->SetObjectArrayElement(array, i, env->NewStringUTF(values[i])); // is new string gc:ed?
    }

    env->SetObjectField(obj, fieldId, array);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_juse_minigods_rendering_model_ModelLoader_cleanup(JNIEnv *env, jobject instance) {
    if (importer != nullptr) {
        delete importer;
        importer = nullptr;
    }
}