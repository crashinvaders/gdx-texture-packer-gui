#include "basisu_native_utils.h"

// LOGGING ==============================
#include <iostream>

void basisuUtils::logInfo(const char *tag, const char *message) {
    std::cout << "[" << tag << "] INFO: " << message << std::endl;
}

void basisuUtils::logError(const char *tag, const char *message) {
    std::cout << "[" << tag << "] ERROR: " << message << std::endl;
}

// EXCEPTIONS ==============================
#if defined __DESKTOP_TEST__ // Only required for desktop native tests (jni-test dir project).
    #include <iostream>
    #include <stdlib.h>

    void basisuUtils::throwException(void*, const char *message) {
        std::cout << "[EXCEPTION ERROR]: " << message << std::endl;
        exit(1);
    }

#else // General JNI case
    #include <iostream>
    #include <jni.h>

    void basisuUtils::throwException(void *envRaw, const char *message) {
        JNIEnv *env = (JNIEnv*)envRaw;
        env->ThrowNew(env->FindClass("com/crashinvaders/basisu/BasisuWrapperException"), message);
    }
    
#endif