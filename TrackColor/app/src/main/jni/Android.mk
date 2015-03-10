

#include /home/ilhan/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk
#include /home/ilhan/Projects/android/TrackColor/libraries/native/jni/OpenCV.mk


#LOCAL_CFLAGS := -I/home/ilhan/OpenCV-android-sdk/sdk/native/jni/include -Wall -Werror
#LOCAL_CFLAGS  := -I/home/ilhan/Projects/android/TrackColor/libraries/native/jni/include -Wall -Werror


#LOCAL_C_INCLUDES := /home/ilhan/Projects/android/TrackColor/libraries/native/jni/include
#LOCAL_C_INCLUDE:= /home/ilhan/OpenCV-android-sdk/sdk/native/jni/include

#LOCAL_STATIC_LIBRARIES := /home/ilhan/Projects/android/TrackColor/libraries/native/libs/armeabi-v7a/libopencv_core.a
#LOCAL_STATIC_LIBRARIES := /home/ilhan/OpenCV-android-sdk/sdk/native/libs/armeabi-v7a/libopencv_core.a
#LOCAL_STATIC_LIBRARIES := ../jniLibs/armeabi-v7a/libopencv_core.a




LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OPENCV_CAMERA_MODULES:=off
#OPENCV_INSTALL_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=STATIC
#include ../../sdk/native/jni/OpenCV.mk
include /home/ilhan/Projects/android/TrackColor/libraries/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := jni_part.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl

LOCAL_MODULE     := nativegray

include $(BUILD_SHARED_LIBRARY)