LOCAL_PATH := $(call my-dir)

#######################
# Prepare celix utils #
#######################
include $(CLEAR_VARS)
LOCAL_MODULE := celix_util
LOCAL_SRC_FILES := libcelix_utils.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include/celix
include $(PREBUILT_SHARED_LIBRARY)

###########################
# Prepare celix framework #
###########################
include $(CLEAR_VARS)
LOCAL_MODULE := celix_fw
LOCAL_SRC_FILES := libcelix_framework.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include/celix
include $(PREBUILT_SHARED_LIBRARY)

################
# Prepare curl #
################
include $(CLEAR_VARS)
LOCAL_MODULE := curl
LOCAL_SRC_FILES := libcurl.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

####################
# Prepare jni part #
####################
include $(CLEAR_VARS)
LOCAL_MODULE := jni_part
LOCAL_SRC_FILES := jni_part.c
LOCAL_STATIC_LIBRARIES := curl
LOCAL_SHARED_LIBRARIES := celix_fw celix_util
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)


#LOCAL_CFLAGS := -I$(LOCAL_PATH)/include
#LOCAL_LDLIBS := -llog -lz -lcurl -L$(LOCAL_PATH)/
############ OLD CONFIG ##################
# LOCAL_MODULE := jni_part
# LOCAL_CFLAGS := -v -DANDROID -mandroid -I/home/mjansen/workspace/fs/build/output/curl/include -I/home/mjansen/workspace/fs/build/output/celix/include/celix -I/home/mjansen/workspace/fs/build/toolchain-arm/sysroot/usr/include -I/home/mjansen/workspace/fs/build/resources/celix/build-android
# LOCAL_LDLIBS := \
# 	-llog -lcelix_framework -lcelix_utils -lz -lcurl -L/home/mjansen/workspace/fs/build/output/celix/lib -L/home/mjansen/workspace/fs/build/output/curl/lib -L/home/mjansen/workspace/fs/build/output/zlib/lib \
#
# LOCAL_SHARED_LIBRARIES := celix_framework celix_utils z curl
# LOCAL_SRC_FILES := jni_part.c
#
# include $(BUILD_SHARED_LIBRARY)
##########################################