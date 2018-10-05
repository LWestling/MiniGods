//
// Created by LukasW on 2018-08-11.
//

#include "rendering/model/ModelLoader.h"
#include <assimp/Importer.hpp>
#include <assimp/scene.h>
#include <assimp/postprocess.h>
#include <sstream>
#include <jni.h>

#define add3DVecComponents(vec3d, vec) \
    vec.push_back(vec3d.x); \
    vec.push_back(vec3d.y); \
    vec.push_back(vec3d.z);

#define add4DColComponents(col, vec) \
    vec.push_back(col.r); \
    vec.push_back(col.g); \
    vec.push_back(col.b); \
    vec.push_back(col.a); \

#define add2DVecComponents(vec3d, vec) \
    vec.push_back(vec3d.x); \
    vec.push_back(vec3d.y);

#define MODEL_DIR_PATH "com/juse/minigods/rendering/model/"
#define ANIM_CLASS MODEL_DIR_PATH "Animation"
#define NODE_CLASS MODEL_DIR_PATH "Model$Node"
#define SIG(x) "L" x ";"
#define VOID_SIG(x) "(" x ")V"
#define SIG_ARR(x) "[L" x ";"
#define KEY_VAL_SIG SIG_ARR(ANIM_CLASS "$KeyValue")

static Assimp::AndroidJNIIOSystem *androidJNIIOSystem = nullptr;
static Assimp::Importer *importer = nullptr;
static jclass matrixClass, quaternionClass, vectorClass, stringClass;

extern "C"
JNIEXPORT void JNICALL
Java_com_juse_minigods_rendering_model_ModelLoader_prepareModelLoading(JNIEnv *env,
                                                                       jobject instance,
                                                                       jstring internalDataFilePath_,
                                                                       jobject assetManager) {
    const char *internalDataFilePath = env->GetStringUTFChars(internalDataFilePath_, 0);

    AAssetManager *assetManagerJava = AAssetManager_fromJava(env, assetManager);
    std::string dataPath(internalDataFilePath);

    env->ReleaseStringUTFChars(internalDataFilePath_, internalDataFilePath);

    androidJNIIOSystem = new Assimp::AndroidJNIIOSystem(dataPath, assetManagerJava);
    importer = new Assimp::Importer();
    importer->SetIOHandler(androidJNIIOSystem);
}

void setFloatField(JNIEnv *env, jclass c, jobject obj, float val, const char *name);
void setJavaFloatArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, float values[]);
void setJavaIntArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, int *values);
void setJavaStringArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, const char **values);
void setJavaBoolean(JNIEnv *env, jclass c, jobject obj, jboolean val, const char *name);
void createJavaBone(JNIEnv *pEnv, aiBone *bone, jclass boneClass, jclass vertexWeightClass,
                       jobjectArray pArray, int i);
void extractAnimatedModel(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const aiScene *pScene);
void setIntField(JNIEnv *pEnv, jclass pJclass, jobject pJobject, unsigned int weights, const char string[11]);
void setStringField(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const char *value, const char *name);
void setMatrix(JNIEnv *pEnv, jclass pJclass, jobject pJobject, aiMatrix4x4 const &mat, const char *name);
void setVertexWeights(JNIEnv *pEnv, jclass pJclass, jobject pJobject, int numWeights, aiVertexWeight *pWeight, const char *name, jclass vertexWeightClass);
void extractAnimations(JNIEnv *pEnv, jclass pJclass, jobject pJobject, size_t pNumAnimations, aiAnimation **pAnimation);
jobject createAnimation(JNIEnv *pEnv, aiAnimation *pAnimation, jclass animClass, jclass jNodeClass);
jobject extractNode(JNIEnv *pEnv, jobject parent, aiNode *pNode, jclass pNodeClass);
jobject createNodeChannel(JNIEnv *pEnv, aiNodeAnim *pAnim, jmethodID pID, jclass pJclass,
                  jobject animationObject, jclass pJclass1, jmethodID pJmethodID);
jobject createQuaternion(JNIEnv *pEnv, aiQuaternion const &quat);
jobject createVec3(JNIEnv *pEnv, aiVector3D const &quat);
template <class T>
jobject createKeyVal(JNIEnv *pEnv, jclass pJclass, jmethodID pID, T key, jobject value,
                     jobject animationObject);

void extractMaterial(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const aiScene *pScene);
void extractVertices(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const aiScene *pScene);
void extractFaces(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const aiScene *pScene);

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
     * THINGS THAT DO NOT WORK AT ALL
     * * Multiple texture coord channels
     * * Multiple color channels
     * * Multiple meshes
     * * Embedded Textures
     */

    const aiScene *scene = importer->ReadFile(name, aiProcess_Triangulate | aiProcess_ConvertToLeftHanded);
    env->ReleaseStringUTFChars(fileName, name);

    matrixClass = env->FindClass("org/joml/Matrix4f");
    quaternionClass = env->FindClass("org/joml/Quaternionf");
    vectorClass = env->FindClass("org/joml/Vector3f");
    stringClass = env->FindClass("java/lang/String");

    extractMaterial(env, modelClass, model, scene);
    extractVertices(env, modelClass, model, scene);
    extractFaces(env, modelClass, model, scene);
    extractAnimatedModel(env, modelClass, model, scene);
}

void extractFaces(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const aiScene *pScene) {
    aiMesh *mesh = pScene->mMeshes[0]; // only supports 1 mesh atm

    if (mesh->HasFaces()) {
        std::vector<jint> intVec;
        for (int i = 0; i < mesh->mNumFaces; i++) {
            aiFace face = mesh->mFaces[i];
            for (int j = 0; j < face.mNumIndices; j++)
                intVec.push_back(face.mIndices[j]);
        }
        setJavaIntArray(pEnv, pJclass, pJobject, "indices", static_cast<int>(intVec.size()),
                        intVec.data());
    }
}

void extractVertices(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const aiScene *pScene) {
    aiVector3D *vec = pScene->mMeshes[0]->mVertices;
    aiMesh *mesh = pScene->mMeshes[0]; // only supports 1 mesh atm

    std::vector<jfloat> floatVec;
    if (mesh->HasNormals()) {
        aiVector3D *normals = mesh->mNormals;
        for (int i = 0; i < mesh->mNumVertices; i++) {
            add3DVecComponents(vec[i], floatVec);
            add3DVecComponents(normals[i], floatVec);

            // only support 1 uv channel and 2 dimensions (TODO ONE IF PER LOOP OMEGALUL)
            if (mesh->HasTextureCoords(0)) {
                add2DVecComponents(mesh->mTextureCoords[0][i], floatVec);
            } else if (mesh->HasVertexColors(0)) {
                add4DColComponents(mesh->mColors[0][i], floatVec);
            }
        }
        setJavaBoolean(pEnv, pJclass, pJobject, (jboolean) true, "useNormals");
    } else {
        for (int i = 0; i < mesh->mNumVertices; i++) {
            add3DVecComponents(vec[i], floatVec);
        }
        setJavaBoolean(pEnv, pJclass, pJobject, (jboolean) false, "useNormals");
    }

    setJavaFloatArray(pEnv, pJclass, pJobject, "vertices", static_cast<int>(floatVec.size()),
                      floatVec.data());
    floatVec.clear();
}

void extractMaterial(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const aiScene *pScene) {
    aiMesh *mesh = pScene->mMeshes[0]; // only supports 1 mesh atm

    if (mesh->HasNormals() && pScene->HasMaterials()) {
        aiMaterial *material = pScene->mMaterials[0]; // only supports 1 material atm

        int textureCount = material->GetTextureCount(aiTextureType_DIFFUSE);
        std::vector<const char *> textures;
        for (unsigned int i = 0; i < textureCount; i++) {
            aiString path;
            if (material->GetTexture(aiTextureType_DIFFUSE, i, &path) == aiReturn_SUCCESS) {
                textures.push_back(path.C_Str());
            }
        }

        setJavaStringArray(pEnv, pJclass, pJobject, "textures", textureCount, textures.data());
    }
}

void extractAnimatedModel(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const aiScene *pScene) {
    const aiMesh *mesh = pScene->mMeshes[0];
    // if the scene doesn't have animations, we don't need bones and vise-versa
    if (!pScene->HasAnimations() || !mesh->HasBones()) {
        setJavaBoolean(pEnv, pJclass, pJobject, (jboolean) false, "useAnimations");
        return;
    }

    setJavaBoolean(pEnv, pJclass, pJobject, (jboolean) true, "useAnimations");

    jclass boneClass = pEnv->FindClass(MODEL_DIR_PATH "Bone");
    jclass vertexWeightClass = pEnv->FindClass(MODEL_DIR_PATH "VertexWeight");
    jobjectArray boneArray = static_cast<jobjectArray>(pEnv->NewGlobalRef(
            pEnv->NewObjectArray(static_cast<jsize>(mesh->mNumBones), boneClass, 0)
    ));

    for (int boneIndex = 0; boneIndex < mesh->mNumBones; boneIndex++) {
        aiBone *bone = mesh->mBones[boneIndex];
        createJavaBone(pEnv, bone, boneClass, vertexWeightClass, boneArray, boneIndex);
    }

    pEnv->DeleteLocalRef(boneClass);
    pEnv->DeleteLocalRef(vertexWeightClass);

    jmethodID methodId = pEnv->GetMethodID(pJclass, "setBones", "(" SIG_ARR(MODEL_DIR_PATH "Bone") ")V");
    pEnv->CallVoidMethod(pJobject, methodId, boneArray);

    jobject rootNode = extractNode(pEnv, NULL, pScene->mRootNode, pEnv->FindClass(NODE_CLASS));
    methodId = pEnv->GetMethodID(pJclass, "setRootNode", "(" SIG(NODE_CLASS) ")V");
    pEnv->CallVoidMethod(pJobject, methodId, rootNode);
    pEnv->DeleteGlobalRef(rootNode);

    extractAnimations(pEnv, pJclass, pJobject, pScene->mNumAnimations, pScene->mAnimations);
}

jobject extractNode(JNIEnv *pEnv, jobject parent, aiNode *pNode, jclass pNodeClass) {
    jobject node = pEnv->NewGlobalRef(pEnv->AllocObject(pNodeClass));

    jobjectArray nodeArray = pEnv->NewObjectArray(pNode->mNumChildren, pNodeClass, 0);
    for (int nodeChildIndex = 0; nodeChildIndex < pNode->mNumChildren; nodeChildIndex++) {
        jobject nodeObj = extractNode(pEnv, node, pNode->mChildren[nodeChildIndex], pNodeClass);
        pEnv->SetObjectArrayElement(nodeArray, nodeChildIndex, nodeObj);
        pEnv->DeleteGlobalRef(nodeObj);
    }

    // set children
    jfieldID children = pEnv->GetFieldID(pNodeClass, "children", SIG_ARR(NODE_CLASS));
    pEnv->SetObjectField(node, children, nodeArray);
    pEnv->DeleteLocalRef(nodeArray);

    // set parent and name
    jfieldID field = pEnv->GetFieldID(pNodeClass, "parent", SIG(NODE_CLASS));
    pEnv->SetObjectField(node, field, parent);
    setStringField(pEnv, pNodeClass, node, pNode->mName.C_Str(), "name");

    // set transformation and mesh arr
    setMatrix(pEnv, pNodeClass, node, pNode->mTransformation, "transformation");
    if (pNode->mNumMeshes > 0)
        setJavaIntArray(pEnv, pNodeClass, node, "meshes", pNode->mNumMeshes, reinterpret_cast<int *>(pNode->mMeshes));

    return node;
}

void extractAnimations(JNIEnv *pEnv, jclass pJclass, jobject pJobject, size_t pNumAnimations,
                       aiAnimation **pAnimation) {
    jclass animClass = pEnv->FindClass(ANIM_CLASS);
    jclass nodeChannelClass = pEnv->FindClass(ANIM_CLASS "$NodeChannel");

    jobjectArray animationArray = pEnv->NewObjectArray(static_cast<jsize>(pNumAnimations), animClass, 0);
    for (size_t i = 0; i < pNumAnimations; i++) {
        jobject animation = createAnimation(pEnv, pAnimation[i], animClass, nodeChannelClass);
        pEnv->SetObjectArrayElement(animationArray, static_cast<jsize>(i), animation);
        pEnv->DeleteGlobalRef(animation);
    }

    jfieldID animationsField = pEnv->GetFieldID(pJclass, "animations", SIG_ARR(ANIM_CLASS));
    pEnv->SetObjectField(pJobject, animationsField, animationArray);
    pEnv->DeleteLocalRef(animationArray);
}

jobject createAnimation(JNIEnv *pEnv, aiAnimation *pAnimation, jclass animClass, jclass nodeChannelClass) {
    // create animation class
    jstring name = pEnv->NewStringUTF(pAnimation->mName.C_Str());
    jmethodID method = pEnv->GetMethodID(animClass, "<init>", "(" SIG("java/lang/String") "DD)V");
    jobject animationObject = pEnv->NewObject(animClass, method, name, pAnimation->mTicksPerSecond, pAnimation->mDuration);
    pEnv->DeleteLocalRef(name);

    // create node channels
    jmethodID nodeMethod = pEnv->GetMethodID(nodeChannelClass, "<init>",
                                             "(" SIG(ANIM_CLASS) SIG("java/lang/String") "II"
                                                     KEY_VAL_SIG KEY_VAL_SIG KEY_VAL_SIG ")V");

    jclass keyClass = pEnv->FindClass(ANIM_CLASS "$KeyValue");
    jmethodID keyInit = pEnv->GetMethodID(keyClass, "<init>", "(" SIG(ANIM_CLASS) SIG("java/lang/Object") "D)V");
    jobjectArray nodeChannelArray = pEnv->NewObjectArray(pAnimation->mNumChannels, nodeChannelClass, 0);

    for (int nodeChannelIndex = 0; nodeChannelIndex < pAnimation->mNumChannels; nodeChannelIndex++) {
        jobject nodeChannel = createNodeChannel(pEnv, pAnimation->mChannels[nodeChannelIndex],
                                                nodeMethod, nodeChannelClass, animationObject,
                                                keyClass, keyInit);
        pEnv->SetObjectArrayElement(nodeChannelArray, nodeChannelIndex, nodeChannel);
        pEnv->DeleteGlobalRef(nodeChannel);
    }

    pEnv->DeleteLocalRef(keyClass);

    // set node channels
    jmethodID setNodeChannels = pEnv->GetMethodID(animClass, "setNodeChannels", VOID_SIG(SIG_ARR(ANIM_CLASS "$NodeChannel")));
    pEnv->CallVoidMethod(animationObject, setNodeChannels, nodeChannelArray);
    pEnv->DeleteLocalRef(nodeChannelArray);

    return pEnv->NewGlobalRef(animationObject);
}

jobject createNodeChannel(JNIEnv *pEnv, aiNodeAnim *pAnim, jmethodID pID, jclass pJclass,
                          jobject animationObject, jclass keyClass, jmethodID keyInit) {
    jobjectArray translations, rotations, scalings;

    translations = pEnv->NewObjectArray(pAnim->mNumPositionKeys, keyClass, 0);
    for (int i = 0; i < pAnim->mNumPositionKeys; i++) {
        jobject jVec = createVec3(pEnv, pAnim->mPositionKeys[i].mValue);
        jobject keyVal = createKeyVal<aiVectorKey>(pEnv, keyClass, keyInit, pAnim->mPositionKeys[i], jVec, animationObject);

        pEnv->DeleteGlobalRef(jVec);
        pEnv->SetObjectArrayElement(translations, i, keyVal);
        pEnv->DeleteGlobalRef(keyVal);
    }

    rotations = pEnv->NewObjectArray(pAnim->mNumRotationKeys, keyClass, 0);
    for (int i = 0; i < pAnim->mNumRotationKeys; i++) {
        jobject jQuat = createQuaternion(pEnv, pAnim->mRotationKeys[i].mValue);
        jobject keyVal = createKeyVal<aiQuatKey>(pEnv, keyClass, keyInit, pAnim->mRotationKeys[i], jQuat, animationObject);

        pEnv->DeleteGlobalRef(jQuat);
        pEnv->SetObjectArrayElement(rotations, i, keyVal);
        pEnv->DeleteGlobalRef(keyVal);
    }

    scalings = pEnv->NewObjectArray(pAnim->mNumScalingKeys, keyClass, 0);
    for (int i = 0; i < pAnim->mNumScalingKeys; i++) {
        jobject jVec = createVec3(pEnv, pAnim->mScalingKeys[i].mValue);
        jobject keyVal = createKeyVal<aiVectorKey>(pEnv, keyClass, keyInit,
                                          pAnim->mScalingKeys[i], jVec, animationObject);
        pEnv->DeleteGlobalRef(jVec);
        pEnv->SetObjectArrayElement(scalings, i, keyVal);
        pEnv->DeleteGlobalRef(keyVal);
    }

    jstring name = pEnv->NewStringUTF(pAnim->mNodeName.C_Str());
    jobject nodeChannel = pEnv->NewGlobalRef(pEnv->NewObject(pJclass, pID, animationObject, name,
                                          pAnim->mPreState, pAnim->mPostState, translations, scalings, rotations));
    pEnv->DeleteLocalRef(translations);
    pEnv->DeleteLocalRef(rotations);
    pEnv->DeleteLocalRef(scalings);

    return nodeChannel;
}

template <class T>
jobject createKeyVal(JNIEnv *pEnv, jclass pJclass, jmethodID pID, T key, jobject value, jobject animationObject) {
    return pEnv->NewGlobalRef(pEnv->NewObject(pJclass, pID, animationObject, value, key.mTime));
}

void createJavaBone(JNIEnv *pEnv, aiBone *bone, jclass boneClass, jclass vertexWeightClass,
                       jobjectArray pArray, int index) {
    jobject boneObject = pEnv->AllocObject(boneClass);

    setStringField(pEnv, boneClass, boneObject, bone->mName.C_Str(), "name");

    setMatrix(pEnv, boneClass, boneObject, bone->mOffsetMatrix, "transformation");
    setVertexWeights(pEnv, boneClass, boneObject, bone->mNumWeights, bone->mWeights,
                     "vertexWeights", vertexWeightClass);

    pEnv->SetObjectArrayElement(pArray, index, boneObject);
}

void setVertexWeights(JNIEnv *pEnv, jclass pJclass, jobject pJobject, int numWeights,
                      aiVertexWeight *pWeight, const char *name, jclass vertexWeightClass) {
    jobjectArray weightArray = pEnv->NewObjectArray(numWeights, vertexWeightClass, 0);

    for (int weightIndex = 0; weightIndex < numWeights; weightIndex++) {
        jobject weightObject = pEnv->AllocObject(vertexWeightClass);

        setIntField(pEnv, vertexWeightClass, weightObject, pWeight[weightIndex].mVertexId, "vertexId");
        setFloatField(pEnv, vertexWeightClass, weightObject, pWeight[weightIndex].mWeight, "weight");
        pEnv->SetObjectArrayElement(weightArray, weightIndex, weightObject);

        pEnv->DeleteLocalRef(weightObject);
    }

    jfieldID vertexWeightField = pEnv->GetFieldID(pJclass, name, SIG_ARR(MODEL_DIR_PATH "VertexWeight"));
    pEnv->SetObjectField(pJobject, vertexWeightField, weightArray);
    pEnv->DeleteLocalRef(weightArray);
}

void setMatrix(JNIEnv *pEnv, jclass pJclass, jobject pJobject, aiMatrix4x4 const &mat,
               const char *name) {
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

jobject createQuaternion(JNIEnv *pEnv, aiQuaternion const &quat) {
    jmethodID constructor = pEnv->GetMethodID(quaternionClass, "<init>", "(FFFF)V");
    jobject matrix = pEnv->NewObject(
            quaternionClass, constructor,
            quat.x, quat.y, quat.z, quat.w
    );

    return pEnv->NewGlobalRef(matrix);
}

jobject createVec3(JNIEnv *pEnv, aiVector3D const &vec) {
    jmethodID constructor = pEnv->GetMethodID(vectorClass, "<init>", "(FFF)V");
    jobject matrix = pEnv->NewObject(
            vectorClass, constructor,
            vec.x, vec.y, vec.z
    );

    return pEnv->NewGlobalRef(matrix);
}

void setStringField(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const char *value,
                    const char *name) {
    jfieldID fieldId = pEnv->GetFieldID(pJclass, name, "Ljava/lang/String;");
    jstring str = pEnv->NewStringUTF(value);
    pEnv->SetObjectField(pJobject, fieldId, str);
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
    jobjectArray array = env->NewObjectArray(valueLen, stringClass, 0);

    for (int i = 0; i < valueLen; i++) {
        jstring str = env->NewStringUTF(values[i]);
        env->SetObjectArrayElement(array, i, str); // is new string gc:ed?
    }

    env->SetObjectField(obj, fieldId, array);
    env->DeleteLocalRef(array);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_juse_minigods_rendering_model_ModelLoader_cleanup(JNIEnv *env, jobject instance) {
    if (importer != nullptr) {
        delete importer;
        importer = nullptr;
    }
}