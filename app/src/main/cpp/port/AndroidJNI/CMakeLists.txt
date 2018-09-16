cmake_minimum_required(VERSION 2.8)

include_directories(${CMAKE_SOURCE_DIR}/../libInclude/)
include_directories(${CMAKE_SOURCE_DIR}/../libInclude/port/AndroidJNI/)
add_library(android_jniiosystem STATIC AndroidJNIIOSystem.cpp)
TARGET_LINK_LIBRARIES(android_jniiosystem android log)
