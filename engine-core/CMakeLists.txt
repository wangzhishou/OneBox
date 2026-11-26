cmake_minimum_required(VERSION 3.22.1)
project(onebox_engine_core LANGUAGES CXX)

add_library(onebox_engine_core STATIC
    src/core/Engine.cpp
    src/core/Game.cpp
    src/core/WorkerThread.cpp
    src/particles/ParticleSystem.cpp
    src/physics/PhysicsSystem.cpp
    src/physics/UniformGrid.cpp
    src/renderer/RenderGraph.cpp
)

target_include_directories(onebox_engine_core PUBLIC include)
target_compile_features(onebox_engine_core PUBLIC cxx_std_20)
target_compile_options(onebox_engine_core PRIVATE -Wall -Wextra -Wpedantic)
set_target_properties(onebox_engine_core PROPERTIES POSITION_INDEPENDENT_CODE ON)
