#pragma once

#include <array>
#include <cstdint>

#include "onebox/ecs/Registry.hpp"
#include "onebox/math/Vec2.hpp"

namespace onebox::survive30s {

enum class GamePhase : std::uint32_t { Idle = 0, Playing = 1, Paused = 2, GameOver = 3, Win = 4 };
enum class DifficultyPhase : std::uint32_t { Warmup = 0, Rush = 1, Storm = 2 };
enum class ObstacleType : std::uint32_t { TopFall = 0, BottomRise = 1, LeftFly = 2, RightFly = 3 };

enum SoundCode : std::uint32_t {
    SoundNone = 0,
    SoundGameOver = 1,
    SoundShieldBlock = 2,
    SoundNearMiss = 3,
    SoundShieldGain = 4,
    SoundPhaseChange = 5,
    SoundWin = 6,
};

struct SurvivePalette {
    std::array<float, 4> primary{0.05F, 0.65F, 0.72F, 0.96F};
    std::array<float, 4> secondary{1.0F, 0.35F, 0.22F, 0.96F};
    std::array<float, 4> tertiary{1.0F, 0.85F, 0.15F, 0.96F};
    std::array<float, 4> player{0.73F, 0.10F, 0.10F, 1.0F};
};

struct SurviveState {
    Entity player = kInvalidEntity;
    Entity shieldRing = kInvalidEntity;
    GamePhase phase = GamePhase::Idle;
    DifficultyPhase difficulty = DifficultyPhase::Warmup;
    Vec2 playerTarget{360.0F, 1088.0F};
    float elapsedSeconds = 0.0F;
    float spawnSeconds = 0.0F;
    float invincibleSeconds = 0.0F;
    float dangerLevel = 0.0F;
    float nearMissSoundCooldown = 0.0F;
    std::uint32_t shieldCount = 0;
    std::uint32_t nearMissCount = 0;
    std::uint32_t nearMissCharge = 0;
    std::uint32_t soundSequence = 0;
    std::uint32_t soundCode = SoundNone;
    std::uint32_t hapticSequence = 0;
    float hapticStrength = 0.0F;
    std::uint32_t randomState = 1;
    SurvivePalette palette{};
};

struct Player {};
struct Obstacle {
    ObstacleType type = ObstacleType::TopFall;
    std::uint32_t colorIndex = 0;
    bool didNearMiss = false;
    bool didCollide = false;
};

struct CollisionTag {};

}  // namespace onebox::survive30s
