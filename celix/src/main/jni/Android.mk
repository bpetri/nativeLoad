LOCAL_PATH := $(call my-dir)

#######################
# Prepare celix utils #
#######################
include $(CLEAR_VARS)
LOCAL_MODULE := celix_util
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libcelix_utils.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include/celix
include $(PREBUILT_SHARED_LIBRARY)


#####################
# Prepare celix dfi #
#####################
include $(CLEAR_VARS)
LOCAL_MODULE := celix_dfi
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libcelix_dfi.so
include $(PREBUILT_SHARED_LIBRARY)


###########################
# Prepare celix framework #
###########################
include $(CLEAR_VARS)
LOCAL_MODULE := celix_fw
LOCAL_SHARED_LIBRARIES := celix_dfi
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libcelix_framework.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include/celix
include $(PREBUILT_SHARED_LIBRARY)

################
# Prepare curl #
################
include $(CLEAR_VARS)
LOCAL_MODULE := curl
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libcurl.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include $(LOCAL_PATH)/$(TARGET_ARCH_ABI)/curl
include $(PREBUILT_STATIC_LIBRARY)

####################
# Prepare jni part #
####################
include $(CLEAR_VARS)
LOCAL_MODULE := jni_part
LOCAL_SRC_FILES := jni_part.c
LOCAL_STATIC_LIBRARIES := curl
LOCAL_SHARED_LIBRARIES := celix_fw celix_util
LOCAL_CFLAGS := -O0 -g -ggdb
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)