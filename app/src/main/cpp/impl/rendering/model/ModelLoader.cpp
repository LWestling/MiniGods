//
// Created by LukasW on 2018-08-11.
//

#include "rendering/model/ModelLoader.h"
#include <assimp/Importer.hpp>
#include <assimp/scene.h>
#include <assimp/postprocess.h>
#include <sstream>

#define add3DVecComponents(vec3d, vec) \
    vec.push_back(vec3d.x); \
    vec.push_back(vec3d.y); \
    vec.push_back(vec3d.z);

#define add2DVecComponents(vec3d, vec) \
    vec.push_back(vec3d.x); \
    vec.push_back(vec3d.y);

#define MODEL_DIR_PATH "com/juse/minigods/rendering/model/"
#define ANIM_CLASS MODEL_DIR_PATH "Animation"

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

void setFloatField(JNIEnv *env, jclass c, jobject obj, float val, const char *name);

void setJavaFloatArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen,
                       float values[]);

void
setJavaIntArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, int *values);

void setJavaStringArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen,
                        const char **values);

void
setJavaObjectArray(JNIEnv *env, jclass c, jobject obj, const char *objectClass, const char *name,
                   int valueLen, jobject *values);

void setJavaBoolean(JNIEnv *env, jclass c, jobject obj, jboolean val, const char *name);

jobject createJavaBone(JNIEnv *pEnv, aiBone *bone);

void extractAnimatedModel(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const aiScene *pScene,
                          const aiMesh *mesh);

void setIntField(JNIEnv *pEnv, jclass pJclass, jobject pJobject, unsigned int weights,
                 const char string[11]);

void
setStringField(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const char *value, const char *name);

void
setMatrix(JNIEnv *pEnv, jclass pJclass, jobject pJobject, aiMatrix4x4 const &mat, const char *name);
void
setQuaternion(JNIEnv *pEnv, jclass pJclass, jobject pJobject, aiQuaternion const &quat, const char *name);

void setVertexWeights(JNIEnv *pEnv, jclass pJclass, jobject pJobject, int numWeights,
                      aiVertexWeight *pWeight, const char *name);

void extractAnimations(JNIEnv *pEnv, jclass pJclass, jobject pJobject, unsigned int pNumAnimations,
                       aiAnimation **pAnimation);

jobject createAnimation(JNIEnv *pEnv, aiAnimation *pAnimation);

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

    const aiScene *scene = importer->ReadFile(name, aiProcess_Triangulate |
                                                    aiProcess_ConvertToLeftHanded);

    aiMesh *mesh = scene->mMeshes[0]; // only supports 1 mesh atm
    aiVector3D *vec = scene->mMeshes[0]->mVertices;

    if (mesh->HasNormals() && scene->HasMaterials()) {
        aiMaterial *material = scene->mMaterials[0]; // only supports 1 material atm

        int textureCount = material->GetTextureCount(aiTextureType_DIFFUSE);
        std::vector<const char *> textures;
        for (unsigned int i = 0; i < textureCount; i++) {
            aiString path;
            if (material->GetTexture(aiTextureType_DIFFUSE, i, &path) == aiReturn_SUCCESS) {
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

    setJavaFloatArray(env, modelClass, model, "vertices", static_cast<int>(floatVec.size()),
                      floatVec.data());
    floatVec.clear();

    if (mesh->HasFaces()) {
        std::vector<jint> intVec;
        for (int i = 0; i < mesh->mNumFaces; i++) {
            aiFace face = mesh->mFaces[i];
            for (int j = 0; j < face.mNumIndices; j++)
                intVec.push_back(face.mIndices[j]);
        }
        setJavaIntArray(env, modelClass, model, "indices", static_cast<int>(intVec.size()),
                        intVec.data());
    }

    extractAnimatedModel(env, modelClass, model, scene, mesh);

    env->ReleaseStringUTFChars(fileName, name);
    importer->FreeScene();
}

void extractAnimatedModel(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const aiScene *pScene,
                          const aiMesh *mesh) {
    // if the scene doesn't have animations, we don't need bones and vise-versa
    if (!pScene->HasAnimations() || !mesh->HasBones()) {
        setJavaBoolean(pEnv, pJclass, pJobject, (jboolean) false, "useAnimations");
        return;
    }

    setJavaBoolean(pEnv, pJclass, pJobject, (jboolean) true, "useAnimations");

    std::vector<jobject> bones;
    for (int boneIndex = 0; boneIndex < mesh->mNumBones; boneIndex++) {
        aiBone *bone = mesh->mBones[boneIndex];
        bones.push_back(createJavaBone(pEnv, bone));
    }
    setJavaObjectArray(pEnv, pJclass, pJobject, MODEL_DIR_PATH "Bone",
                       "bones", bones.size(), bones.data());
    for (jobject obj : bones)
        pEnv->DeleteGlobalRef(obj);

    extractAnimations(pEnv, pJclass, pJobject, pScene->mNumAnimations, pScene->mAnimations);
}

void extractAnimations(JNIEnv *pEnv, jclass pJclass, jobject pJobject, unsigned int pNumAnimations,
                       aiAnimation **pAnimation) {
    std::vector<jobject> javaAnimations;
    for (unsigned int i = 0; i < pNumAnimations; i++) {
        javaAnimations.push_back(createAnimation(pEnv, pAnimation[i]));
    }

    setJavaObjectArray(pEnv, pJclass, pJobject, ANIM_CLASS, "animations", javaAnimations.size(),
                       javaAnimations.data());

    for (jobject obj : javaAnimations)
        pEnv->DeleteGlobalRef(obj);
}

jobject createAnimation(JNIEnv *pEnv, aiAnimation *pAnimation) {
    jclass javaAnimationClass = pEnv->FindClass(ANIM_CLASS);
    jobject javaAnimation = pEnv->NewGlobalRef(pEnv->AllocObject(javaAnimationClass));

    /*
    jclass vänsterClass = pEnv->FindClass(ANIM_CLASS "$blablaVänsterFejdan");
    jobject vänster = pEnv->AllocObject(vänsterClass);
    setJavaBoolean(pEnv, vänsterClass, vänster, (jboolean) true, "kd");

    jfieldID vf = pEnv->GetFieldID(javaAnimationClass, "hi2", "L" ANIM_CLASS "$blablaVänsterFejdan;");
    pEnv->SetObjectField(javaAnimation, vf, vänster);
    */

    return javaAnimation;
}


jobject createJavaBone(JNIEnv *pEnv, aiBone *bone) {
    jclass boneClass = pEnv->FindClass(MODEL_DIR_PATH "Bone");
    jobject boneObject = pEnv->NewGlobalRef(pEnv->AllocObject(boneClass));

    setStringField(pEnv, boneClass, boneObject, bone->mName.C_Str(), "name");

    setMatrix(pEnv, boneClass, boneObject, bone->mOffsetMatrix, "transformation");
    setVertexWeights(pEnv, boneClass, boneObject, bone->mNumWeights, bone->mWeights,
                     "vertexWeights");

    return boneObject;
}

void setVertexWeights(JNIEnv *pEnv, jclass pJclass, jobject pJobject, int numWeights,
                      aiVertexWeight *pWeight, const char *name) {
    std::vector<jobject> weights;
    jclass weightClass = pEnv->FindClass(MODEL_DIR_PATH "VertexWeight");
    for (int weightIndex = 0; weightIndex < numWeights; weightIndex++) {
        jobject weightObject = pEnv->NewGlobalRef(pEnv->AllocObject(weightClass));
        setIntField(pEnv, weightClass, weightObject, pWeight[weightIndex].mVertexId, "vertexId");
        setFloatField(pEnv, weightClass, weightObject, pWeight[weightIndex].mWeight, "weight");
        weights.push_back(weightObject);
    }

    setJavaObjectArray(pEnv, pJclass, pJobject, MODEL_DIR_PATH "VertexWeight",
                       name, numWeights, weights.data());

    for (jobject obj : weights)
        pEnv->DeleteGlobalRef(obj);
}

void setMatrix(JNIEnv *pEnv, jclass pJclass, jobject pJobject, aiMatrix4x4 const &mat,
               const char *name) {
    jclass matrixClass = pEnv->FindClass("org/joml/Matrix4f");
    jmethodID constructor = pEnv->GetMethodID(matrixClass, "<init>", "(FFFFFFFFFFFFFFFF)V");
    jobject matrix = pEnv->NewObject(
            matrixClass, constructor,
            mat.a1, mat.a2, mat.a3, mat.a4,
            mat.b1, mat.b2, mat.b3, mat.b4,
            mat.c1, mat.c2, mat.c3, mat.c4,
            mat.d1, mat.d2, mat.d3, mat.d4
    );

    jfieldID fieldId = pEnv->GetFieldID(pJclass, name, "Lorg/joml/Matrix4f;");
    pEnv->SetObjectField(pJobject, fieldId, matrix);
}

void setQuaternion(JNIEnv *pEnv, jclass pJclass, jobject pJobject, aiQuaternion const &quat,
               const char *name) {
    jclass quaternionClass = pEnv->FindClass("org/joml/Quaternionf");
    jmethodID constructor = pEnv->GetMethodID(quaternionClass, "<init>", "(FFFF)V");
    jobject matrix = pEnv->NewObject(
            quaternionClass, constructor,
            quat.x, quat.y, quat.z, quat.w
    );

    jfieldID fieldId = pEnv->GetFieldID(pJclass, name, "Lorg/joml/Matrix4f;");
    pEnv->SetObjectField(pJobject, fieldId, matrix);
}

void setStringField(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const char *value,
                    const char *name) {
    jfieldID fieldId = pEnv->GetFieldID(pJclass, name, "Ljava/lang/String;");
    pEnv->SetObjectField(pJobject, fieldId, pEnv->NewStringUTF(value));
}

void setIntField(JNIEnv *pEnv, jclass pJclass, jobject pJobject, unsigned int weights,
                 const char *name) {
    jfieldID fieldId = pEnv->GetFieldID(pJclass, name, "I");
    pEnv->SetIntField(pJobject, fieldId, weights);
}

void setFloatField(JNIEnv *env, jclass c, jobject obj, float val, const char *name) {
    jfieldID fieldId = env->GetFieldID(c, name, "F");
    env->SetFloatField(obj, fieldId, val);
}

void setJavaBoolean(JNIEnv *env, jclass c, jobject obj, jboolean val, const char *name) {
    jfieldID fieldId = env->GetFieldID(c, name, "Z");
    env->SetBooleanField(obj, fieldId, val);
}

void setJavaFloatArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen,
                       jfloat *values) {
    jfieldID fieldId = env->GetFieldID(c, name, "[F");
    jfloatArray array = env->NewFloatArray(valueLen);

    env->SetFloatArrayRegion(array, 0, valueLen, values);
    env->SetObjectField(obj, fieldId, array);
}

void
setJavaIntArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, jint *values) {
    jfieldID fieldId = env->GetFieldID(c, name, "[I");
    jintArray array = env->NewIntArray(valueLen);

    env->SetIntArrayRegion(array, 0, valueLen, values);
    env->SetObjectField(obj, fieldId, array);
}

void setJavaStringArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen,
                        const char **values) {
    jfieldID fieldId = env->GetFieldID(c, name, "[Ljava/lang/String;");
    jclass stringClass = static_cast<jclass>(env->FindClass("java/lang/String"));
    jobjectArray array = env->NewObjectArray(valueLen, stringClass, 0);

    for (int i = 0; i < valueLen; i++) {
        env->SetObjectArrayElement(array, i, env->NewStringUTF(values[i])); // is new string gc:ed?
    }

    env->SetObjectField(obj, fieldId, array);
}

void setJavaObjectArray(JNIEnv *env, jclass c, jobject obj, const char *classPath, const char *name,
                        int valueLen, jobject *values) {
    // Usch
    std::ostringstream str;
    str << "[L" << classPath << ";";

    jfieldID fieldId = env->GetFieldID(c, name, str.str().c_str());
    jclass stringClass = static_cast<jclass>(env->FindClass(classPath));
    jobjectArray array = env->NewObjectArray(valueLen, stringClass, 0);

    for (int i = 0; i < valueLen; i++) {
        env->SetObjectArrayElement(array, i, values[i]);
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