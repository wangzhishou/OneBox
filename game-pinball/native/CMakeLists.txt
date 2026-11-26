add_library(onebox_game_pinball OBJECT
    PinballGame.cpp
    PinballSystems.cpp
)

target_include_directories(onebox_game_pinball PUBLIC ${ONEBOX_PROJECT_ROOT})
target_link_libraries(onebox_game_pinball PUBLIC onebox_engine_core)
target_compile_features(onebox_game_pinball PUBLIC cxx_std_20)
set_property(GLOBAL APPEND PROPERTY ONEBOX_GAME_PLUGIN_TARGETS onebox_game_pinball)
