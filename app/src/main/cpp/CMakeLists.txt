cmake_minimum_required(VERSION 3.1)

add_library(assimp SHARED IMPORTED)
set_property(TARGET assimp PROPERTY IMPORTED_LOCATION "${PROJECT_SOURCE_DIR}/../jni/${ANDROID_ABI}/libassimp.so")

add_library(native_app_glue ${ANDROID_NDK}/sources/android/native_app_glue/android_native_app_glue.c)
add_library(game_engine SHARED ${CMAKE_CURRENT_LIST_DIR}/impl/main/main.cpp)

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/port/AndroidJNI/)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/impl/rendering/)

target_include_directories(game_engine PRIVATE ${ANDROID_NDK}/sources/android/native_app_glue ./include/ ../libInclude)
target_link_libraries(game_engine native_app_glue android log assimp android_jniiosystem GLESv3 EGL)
