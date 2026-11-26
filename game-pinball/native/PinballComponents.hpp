#pragma once

#include <array>
#include <cstdint>

#include "onebox/ecs/Registry.hpp"

namespace onebox::pinball {

inline constexpr float kDefaultBallRadius = 11.0F;
inline constexpr float kEnlargedBallRadius = 18.0F;
inline constexpr float kEnlargeDurationSeconds = 12.0F;
inline constexpr std::size_t kMaxBalls = 12;

enum class Phase : std::uint32_t {
    Ready = 0,
    Playing = 1,
    Paused = 2,
    GameOver = 3,
    LevelComplete = 4,
};

enum class BrickEffect : std::uint32_t {
    Normal = 0,
    Split = 1,
    Enlarge = 2,
};

struct PinballPalette {
    std::array<float, 4> background{0.035F, 0.12F, 0.14F, 1.0F};
    std::array<float, 4> normalBrick{0.05F, 0.65F, 0.72F, 0.94F};
    std::array<float, 4> splitBrick{1.0F, 0.35F, 0.22F, 0.96F};
    std::array<float, 4> enlargeBrick{1.0F, 0.85F, 0.15F, 0.96F};
    std::array<float, 4> border{0.55F, 0.63F, 0.63F, 0.9F};
    std::array<float, 4> ball{0.95F, 0.98F, 1.0F, 1.0F};
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
    PinballPalette palette{};
};

struct Ball {
    bool attached = true;
    float enlargeSeconds = 0.0F;
};

struct Brick {
    int hitPoints = 1;
    std::uint32_t score = 100;
    BrickEffect effect = BrickEffect::Normal;
};

struct Bumper {
    std::uint32_t score = 25;
};

struct PaddleControlTag {};
struct PaddleTag {};
struct BorderTag {};
struct BallMotionTag {};
struct BrickDamageTag {};
struct BrickEffectTag {};
struct ComboScoreTag {};
struct RoundTag {};
struct PentatonicAudioTag {};
struct HapticsTag {};
struct CameraTag {};

}  // namespace onebox::pinball
