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

extern "C"
JNIEXPORT void JNICALL
Java_com_juse_minigods_rendering_model_ModelLoader_prepareModelLoading(JNIEnv *env,
                                                                       jobject instance,
                                                                       jstring internalDataFilePath_,
                                                                       jobject assetManager) {
    const char *internalDataFilePath = env->GetStringUTFChars(internalDataFilePath_, 0);

    AAssetManager *assetManagerJava = AAssetManager_fromJava(env, assetManager);
    std::string dataPath(internalDataFilePath);

    androidJNIIOSystem = new Assimp::AndroidJNIIOSystem(dataPath, assetManagerJava);
    importer = new Assimp::Importer();
    importer->SetIOHandler(androidJNIIOSystem);

    env->ReleaseStringUTFChars(internalDataFilePath_, internalDataFilePath);
}

void setFloatField(JNIEnv *env, jclass c, jobject obj, float val, const char *name);
void setJavaFloatArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, float values[]);
void setJavaIntArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, int *values);
void setJavaStringArray(JNIEnv *env, jclass c, jobject obj, const char *name, int valueLen, const char **values);
void setJavaObjectArray(JNIEnv *env, jclass c, jobject obj, const char *objectClass, const char *name, unsigned long valueLen, jobject *values);
void setJavaBoolean(JNIEnv *env, jclass c, jobject obj, jboolean val, const char *name);
jobject createJavaBone(JNIEnv *pEnv, aiBone *bone);
void extractAnimatedModel(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const aiScene *pScene, const aiMesh *mesh);
void setIntField(JNIEnv *pEnv, jclass pJclass, jobject pJobject, unsigned int weights, const char string[11]);
void setStringField(JNIEnv *pEnv, jclass pJclass, jobject pJobject, const char *value, const char *name);
void setMatrix(JNIEnv *pEnv, jclass pJclass, jobject pJobject, aiMatrix4x4 const &mat, const char *name);
void setVertexWeights(JNIEnv *pEnv, jclass pJclass, jobject pJobject, int numWeights, aiVertexWeight *pWeight, const char *name);
void extractAnimations(JNIEnv *pEnv, jclass pJclass, jobject pJobject, size_t pNumAnimations, aiAnimation **pAnimation);
jobject createAnimation(JNIEnv *pEnv, aiAnimation *pAnimation, jclass animClass, jclass jNodeClass);
jobject extractNode(JNIEnv *pEnv, jobject parent, aiNode *pNode, jclass pNodeClass);
jobjectArray createJavaObjectArray(JNIEnv *pEnv, jclass pJclass, unsigned long len, jobject *pJobject);
jobject
createNodeChannel(JNIEnv *pEnv, aiNodeAnim *pAnim, jmethodID pID, jclass pJclass, jobject animationObject);
jobject createQuaternion(JNIEnv *pEnv, aiQuaternion const &quat);
jobject createVec3(JNIEnv *pEnv, aiVector3D const &quat);
void clearJArray(JNIEnv *pEnv, std::vector<jobject> vector);
template <class T>
jobject createKeyVal(JNIEnv *pEnv, jclass pJclass, jmethodID pID, T key, jobject value,
                     jobject animationObject);

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

    jobjectArray array = createJavaObjectArray(pEnv, pEnv->FindClass(MODEL_DIR_PATH "Bone"), bones.size(), bones.data());
    jmethodID methodId = pEnv->GetMethodID(pJclass, "setBones", "(" SIG_ARR(MODEL_DIR_PATH "Bone") ")V");
    pEnv->CallVoidMethod(pJobject, methodId, array);
    clearJArray(pEnv, bones);

    jobject rootNode = extractNode(pEnv, NULL, pScene->mRootNode, pEnv->FindClass(NODE_CLASS));
    methodId = pEnv->GetMethodID(pJclass, "setRootNode", "(" SIG(NODE_CLASS) ")V");
    pEnv->CallVoidMethod(pJobject, methodId, rootNode);
    pEnv->DeleteGlobalRef(rootNode);

    extractAnimations(pEnv, pJclass, pJobject, pScene->mNumAnimations, pScene->mAnimations);
}

jobject extractNode(JNIEnv *pEnv, jobject parent, aiNode *pNode, jclass pNodeClass) {
    jobject node = pEnv->NewGlobalRef(pEnv->AllocObject(pNodeClass));

    std::vector<jobject> nodes;
    for (int nodeChildIndex = 0; nodeChildIndex < pNode->mNumChildren; nodeChildIndex++) {
        nodes.push_back(extractNode(pEnv, node, pNode->mChildren[nodeChildIndex], pNodeClass));
    }

    // set parent and name
    jfieldID field = pEnv->GetFieldID(pNodeClass, "parent", SIG(NODE_CLASS));
    pEnv->SetObjectField(node, field, parent);
    setStringField(pEnv, pNodeClass, node, pNode->mName.C_Str(), "name");

    // set transformation and mesh arr
    setMatrix(pEnv, pNodeClass, node, pNode->mTransformation, "transformation");
    if (pNode->mNumMeshes > 0)
        setJavaIntArray(pEnv, pNodeClass, node, "meshes", pNode->mNumMeshes, reinterpret_cast<int *>(pNode->mMeshes));

    setJavaObjectArray(pEnv, pNodeClass, node, NODE_CLASS, "children", nodes.size(), nodes.data());
    clearJArray(pEnv, nodes);

    return node;
}

void extractAnimations(JNIEnv *pEnv, jclass pJclass, jobject pJobject, size_t pNumAnimations,
                       aiAnimation **pAnimation) {
    std::vector<jobject> javaAnimations;
    jclass animClass = pEnv->FindClass(ANIM_CLASS);
    jclass nodeChannelClass = pEnv->FindClass(ANIM_CLASS "$NodeChannel");

    for (size_t i = 0; i < pNumAnimations; i++) {
        javaAnimations.push_back(createAnimation(pEnv, pAnimation[i], animClass, nodeChannelClass));
    }

    setJavaObjectArray(pEnv, pJclass, pJobject, ANIM_CLASS, "animations",
                       javaAnimations.size(), javaAnimations.data());

    clearJArray(pEnv, javaAnimations);
}

jobject createAnimation(JNIEnv *pEnv, aiAnimation *pAnimation, jclass animClass, jclass nodeChannelClass) {
    // create animation class
    jstring name = pEnv->NewStringUTF(pAnimation->mName.C_Str());
    jmethodID method = pEnv->GetMethodID(animClass, "<init>", "(" SIG("java/lang/String") "DD)V");
    jobject animationObject = pEnv->NewObject(animClass, method, name, pAnimation->mTicksPerSecond, pAnimation->mDuration);

    // create node channels
    jmethodID nodeMethod = pEnv->GetMethodID(nodeChannelClass, "<init>",
                                             "(" SIG(ANIM_CLASS) SIG("java/lang/String") "II"
                                                     KEY_VAL_SIG KEY_VAL_SIG KEY_VAL_SIG ")V");

    std::vector<jobject> nodeChannels;
    for (int nodeChannelIndex = 0; nodeChannelIndex < pAnimation->mNumChannels; nodeChannelIndex++) {
        nodeChannels.push_back(
                createNodeChannel(pEnv, pAnimation->mChannels[nodeChannelIndex], nodeMethod,
                                  nodeChannelClass, animationObject)
        );
    }

    // set node channels
    jobjectArray jNodeChannels = createJavaObjectArray(pEnv, nodeChannelClass, nodeChannels.size(), nodeChannels.data());
    jmethodID setNodeChannels = pEnv->GetMethodID(animClass, "setNodeChannels", VOID_SIG(SIG_ARR(ANIM_CLASS "$NodeChannel")));
    pEnv->CallVoidMethod(animationObject, setNodeChannels, jNodeChannels);
    pEnv->DeleteGlobalRef(jNodeChannels);

    // cleanup
    clearJArray(pEnv, nodeChannels);
    return pEnv->NewGlobalRef(animationObject);
}

jobject createNodeChannel(JNIEnv *pEnv, aiNodeAnim *pAnim, jmethodID pID, jclass pJclass, jobject animationObject) {
    jobjectArray translations, rotations, scalings;
    std::vector<jobject> translationObjects, rotationObjects, scalingObjects;

    jclass keyClass = pEnv->FindClass(ANIM_CLASS "$KeyValue");
    jmethodID keyInit = pEnv->GetMethodID(keyClass, "<init>", "(" SIG(ANIM_CLASS) SIG("java/lang/Object") "D)V");

    for (int i = 0; i < pAnim->mNumPositionKeys; i++) {
        jobject jVec = createVec3(pEnv, pAnim->mPositionKeys[i].mValue);
        translationObjects.push_back(
                createKeyVal<aiVectorKey>(pEnv, keyClass, keyInit,
                                          pAnim->mPositionKeys[i], jVec, animationObject));
        pEnv->DeleteGlobalRef(jVec);
    }
    for (int i = 0; i < pAnim->mNumRotationKeys; i++) {
        jobject jQuat = createQuaternion(pEnv, pAnim->mRotationKeys[i].mValue);
        rotationObjects.push_back(
                createKeyVal<aiQuatKey>(pEnv, keyClass, keyInit,
                                        pAnim->mRotationKeys[i], jQuat, animationObject));
        pEnv->DeleteGlobalRef(jQuat);
    }
    for (int i = 0; i < pAnim->mNumScalingKeys; i++) {
        jobject jVec = createVec3(pEnv, pAnim->mScalingKeys[i].mValue);
        scalingObjects.push_back(
                createKeyVal<aiVectorKey>(pEnv, keyClass, keyInit,
                                          pAnim->mScalingKeys[i], jVec, animationObject));
        pEnv->DeleteGlobalRef(jVec);
    }

    translations = createJavaObjectArray(pEnv, keyClass, translationObjects.size(), translationObjects.data());
    clearJArray(pEnv, translationObjects);

    rotations = createJavaObjectArray(pEnv, keyClass, rotationObjects.size(), rotationObjects.data());
    clearJArray(pEnv, rotationObjects);

    scalings = createJavaObjectArray(pEnv, keyClass, scalingObjects.size(), scalingObjects.data());
    clearJArray(pEnv, scalingObjects);

    jstring name = pEnv->NewStringUTF(pAnim->mNodeName.C_Str());
    jobject nodeChannel = pEnv->NewGlobalRef(pEnv->NewObject(pJclass, pID, animationObject, name,
                                          pAnim->mPreState, pAnim->mPostState, translations, scalings, rotations));
    pEnv->DeleteGlobalRef(translations);
    pEnv->DeleteGlobalRef(scalings);
    pEnv->DeleteGlobalRef(rotations);

    return nodeChannel;
}

template <class T>
jobject createKeyVal(JNIEnv *pEnv, jclass pJclass, jmethodID pID, T key, jobject value, jobject animationObject) {
    return pEnv->NewGlobalRef(pEnv->NewObject(pJclass, pID, animationObject, value, key.mTime));
}

void clearJArray(JNIEnv *pEnv, std::vector<jobject> vector) {
    for (jobject obj : vector) {
        pEnv->DeleteGlobalRef(obj);
    }
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
                       name, static_cast<unsigned long>(numWeights), weights.data());
    clearJArray(pEnv, weights);
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

jobject createQuaternion(JNIEnv *pEnv, aiQuaternion const &quat) {
    jclass quaternionClass = pEnv->FindClass("org/joml/Quaternionf");
    jmethodID constructor = pEnv->GetMethodID(quaternionClass, "<init>", "(FFFF)V");
    jobject matrix = pEnv->NewObject(
            quaternionClass, constructor,
            quat.x, quat.y, quat.z, quat.w
    );

    return pEnv->NewGlobalRef(matrix);
}

jobject createVec3(JNIEnv *pEnv, aiVector3D const &vec) {
    jclass quaternionClass = pEnv->FindClass("org/joml/Vector3f");
    jmethodID constructor = pEnv->GetMethodID(quaternionClass, "<init>", "(FFF)V");
    jobject matrix = pEnv->NewObject(
            quaternionClass, constructor,
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
    jclass stringClass = static_cast<jclass>(env->FindClass("java/lang/String"));
    jobjectArray array = env->NewObjectArray(valueLen, stringClass, 0);

    for (int i = 0; i < valueLen; i++) {
        env->SetObjectArrayElement(array, i, env->NewStringUTF(values[i])); // is new string gc:ed?
    }

    env->SetObjectField(obj, fieldId, array);
}

void setJavaObjectArray(JNIEnv *env, jclass c, jobject obj, const char *classPath, const char *name,
                        unsigned long valueLen, jobject *values) {
    // Usch
    std::ostringstream str;
    str << "[L" << classPath << ";";

    jfieldID fieldId = env->GetFieldID(c, name, str.str().c_str());
    jclass stringClass = env->FindClass(classPath);

    jobjectArray array = createJavaObjectArray(env, stringClass, valueLen, values);
    env->SetObjectField(obj, fieldId, array);
    env->DeleteGlobalRef(array);
}

jobjectArray createJavaObjectArray(JNIEnv *pEnv, jclass pJclass, unsigned long len, jobject *arr) {
    jobjectArray objArray = static_cast<jobjectArray>(pEnv->NewGlobalRef(pEnv->NewObjectArray(static_cast<jsize>(len), pJclass, 0)));

    for (int i = 0; i < len; i++) {
        pEnv->SetObjectArrayElement(objArray, i, arr[i]);
    };

    return objArray;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_juse_minigods_rendering_model_ModelLoader_cleanup(JNIEnv *env, jobject instance) {
    if (importer != nullptr) {
        delete importer;
        importer = nullptr;
    }
}