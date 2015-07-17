LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := jni_part
LOCAL_CFLAGS := -v -DANDROID -mandroid -L/home/bjoern/Development/celix/git/celix.build.android.2/install/lib -I/home/bjoern/Development/celix/git/celix.build.android.2/install/include/celix -I/home/bjoern/Development/android/standalone.android18/sysroot/usr/include
LOCAL_LDLIBS := \
	-llog -lcelix_framework -lcelix_utils -lz -lcurl -L/home/bjoern/Development/celix/git/celix.build.android.2/install/lib -L/home/bjoern/Development/android/cross/android18/curl/lib -L/home/bjoern/Development/android/cross/android18/zlib/lib \
#	-llog -lcelix_framework -lcelix_utils -lz -lcurl -L/home/bjoern/Development/celix/git/celix.build.android.2/install/lib -L/home/bjoern/Development/android/cross/android18/curl/lib -L/home/bjoern/Development/android/cross/android18/zlib/lib \

LOCAL_SHARED_LIBRARIES := celix_framework celix_utils z curl
LOCAL_SRC_FILES := \
	/home/bjoern/AndroidStudioProjects/nativeLoad/app/src/main/jni/jni_part.c \

LOCAL_C_INCLUDES += /home/bjoern/AndroidStudioProjects/nativeLoad/app/src/main/jni
LOCAL_C_INCLUDES += /home/bjoern/AndroidStudioProjects/nativeLoad/app/src/debug/jni

include $(BUILD_SHARED_LIBRARY)
