#pragma once

#include <cstdint>

#include "onebox/ecs/Registry.hpp"

namespace onebox::pinball {

enum class Phase : std::uint32_t {
    Ready = 0,
    Playing = 1,
    Paused = 2,
    GameOver = 3,
    LevelComplete = 4,
};

struct PinballState {
    Entity paddle = kInvalidEntity;
    Entity ball = kInvalidEntity;
    Phase phase = Phase::Ready;
    Phase phaseBeforePause = Phase::Ready;
    std::uint32_t score = 0;
    std::uint32_t combo = 0;
    std::uint32_t lives = 3;
    std::uint32_t level = 1;
    std::uint32_t remainingBricks = 0;
    float paddleTargetX = 360.0F;
    float comboSeconds = 0.0F;
    float audioCooldown = 0.0F;
    std::uint32_t hapticSequence = 0;
    float hapticStrength = 0.0F;
};

struct Ball {
    bool attached = true;
};

struct Brick {
    int hitPoints = 1;
    std::uint32_t score = 100;
};

struct Bumper {
    std::uint32_t score = 25;
};

struct PaddleControlTag {};
struct BallMotionTag {};
struct BrickDamageTag {};
struct ComboScoreTag {};
struct RoundTag {};
struct PentatonicAudioTag {};
struct HapticsTag {};
struct CameraTag {};

}  // namespace onebox::pinball
