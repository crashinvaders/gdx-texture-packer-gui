cmake_minimum_required(VERSION 3.1.0)
project(basisu-wrapper-test VERSION 0.1.0)

set (CMAKE_CXX_STANDARD 11)

add_definitions(-D__DESKTOP_TEST__)
add_definitions(-DBASISU_FORCE_DEVEL_MESSAGES=1)
add_definitions(-DBASISD_SUPPORT_UASTC=1)
add_definitions(-DBASISD_SUPPORT_BC7=1)
add_definitions(-DBASISD_SUPPORT_ATC=0)
add_definitions(-DBASISD_SUPPORT_ASTC_HIGHER_OPAQUE_QUALITY=0)
add_definitions(-DBASISD_SUPPORT_PVRTC2=0)
add_definitions(-DBASISD_SUPPORT_FXT1=0)
add_definitions(-DBASISD_SUPPORT_ETC2_EAC_RG11=0)
add_definitions(-DBASISU_SUPPORT_ENCODING=1)
add_definitions(-DBASISD_SUPPORT_KTX2=1)
add_definitions(-DBASISD_SUPPORT_KTX2_ZSTD=1)

# add_subdirectory("basis-universal")

include_directories(
    "." 
    "../jni" 
    "../jni/basis-universal/transcoder" 
    "../jni/basis-universal/encoder"
    "../jni/basis-universal/zstd"
)

file(GLOB SOURCES
    "./*.cpp"
    "../jni/basis-universal/transcoder/basisu_transcoder.cpp"
    "../jni/basis-universal/encoder/*.cpp"
    "../jni/basis-universal/zstd/zstd.c"
    "../jni/basisu_native_utils.cpp"
    "../jni/basisu_wrapper.cpp"
)

add_executable(basisu-wrapper-test ${SOURCES})