#pragma once

namespace basisuUtils {

    void logInfo(const char* tag, const char* message);

    void logError(const char* tag, const char* message);

    /**
     * Throws an exception in the high-level wrapping code.
     * @param env is "JNIEnv" for JNI implementations and nullptr for other platforms (Emscripten).
     */
    void throwException(void *env, const char *message);

}