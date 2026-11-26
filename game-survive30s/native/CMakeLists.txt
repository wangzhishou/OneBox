add_library(onebox_game_survive30s OBJECT
    SurviveGame.cpp
    SurviveSystems.cpp
)

target_include_directories(onebox_game_survive30s PUBLIC ${ONEBOX_PROJECT_ROOT})
target_link_libraries(onebox_game_survive30s PUBLIC onebox_engine_core)
target_compile_features(onebox_game_survive30s PUBLIC cxx_std_20)
set_property(GLOBAL APPEND PROPERTY ONEBOX_GAME_PLUGIN_TARGETS onebox_game_survive30s)
