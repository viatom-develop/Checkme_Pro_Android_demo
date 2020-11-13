LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES  += decode.cpp 
LOCAL_SRC_FILES  += adpcm.cpp
LOCAL_MODULE     := adpcm_docode
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)