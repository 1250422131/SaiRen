# the minimum version of CMake.
cmake_minimum_required(VERSION 3.4.1)
project(kuikly_entry)

set(KUIKLY_ENTRY_ROOT_PATH ${CMAKE_CURRENT_SOURCE_DIR})

if(DEFINED PACKAGE_FIND_FILE)
    include(${PACKAGE_FIND_FILE})
endif()

include_directories(${KUIKLY_ENTRY_ROOT_PATH})

add_library(kuikly_render SHARED IMPORTED)
set_target_properties(kuikly_render
    PROPERTIES
    IMPORTED_LOCATION ${KUIKLY_ENTRY_ROOT_PATH}/../../../oh_modules/@kuikly-open/render/libs/${OHOS_ARCH}/libkuikly.so)

add_library(kuikly_shared SHARED IMPORTED)
set_target_properties(kuikly_shared
    PROPERTIES
    IMPORTED_LOCATION ${KUIKLY_ENTRY_ROOT_PATH}/../../../libs/${OHOS_ARCH}/libshared.so)

add_library(kuikly_entry SHARED napi_init.cpp)
target_link_libraries(kuikly_entry
    PUBLIC
    libace_napi.z.so
    hilog_ndk.z.so
    kuikly_render
    kuikly_shared
)
