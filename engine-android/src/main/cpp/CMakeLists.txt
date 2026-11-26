cmake_minimum_required(VERSION 3.22.1)
project(onebox_engine_android LANGUAGES CXX)

get_filename_component(ONEBOX_PROJECT_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../../.." ABSOLUTE)
add_subdirectory("${ONEBOX_PROJECT_ROOT}/engine-core" "${CMAKE_BINARY_DIR}/engine-core")

file(GLOB ONEBOX_GAME_MANIFESTS CONFIGURE_DEPENDS "${ONEBOX_PROJECT_ROOT}/game-*/native/CMakeLists.txt")
foreach(ONEBOX_GAME_MANIFEST IN LISTS ONEBOX_GAME_MANIFESTS)
    get_filename_component(ONEBOX_GAME_NATIVE_DIR "${ONEBOX_GAME_MANIFEST}" DIRECTORY)
    get_filename_component(ONEBOX_GAME_DIR "${ONEBOX_GAME_NATIVE_DIR}" DIRECTORY)
    get_filename_component(ONEBOX_GAME_NAME "${ONEBOX_GAME_DIR}" NAME)
    add_subdirectory("${ONEBOX_GAME_NATIVE_DIR}" "${CMAKE_BINARY_DIR}/${ONEBOX_GAME_NAME}")
endforeach()
get_property(ONEBOX_GAME_PLUGIN_TARGETS GLOBAL PROPERTY ONEBOX_GAME_PLUGIN_TARGETS)

find_package(oboe REQUIRED CONFIG)
find_package(games-frame-pacing REQUIRED CONFIG)

add_library(onebox_engine SHARED
    EngineAndroid.cpp
    GlRenderer.cpp
    OboeAudio.cpp
)

target_include_directories(onebox_engine PRIVATE ${ONEBOX_PROJECT_ROOT})
target_compile_features(onebox_engine PRIVATE cxx_std_20)
target_link_libraries(onebox_engine PRIVATE
    onebox_engine_core
    ${ONEBOX_GAME_PLUGIN_TARGETS}
    oboe::oboe
    games-frame-pacing::swappy
    android
    log
    EGL
    GLESv3
)
target_link_options(onebox_engine PRIVATE -Wl,--build-id=none)
add_custom_command(TARGET onebox_engine POST_BUILD
    COMMAND ${CMAKE_STRIP} --remove-section .comment $<TARGET_FILE:onebox_engine>)
