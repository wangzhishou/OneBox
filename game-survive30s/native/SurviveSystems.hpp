#pragma once

#include "onebox/core/EngineContext.hpp"
#include "game-survive30s/native/SurviveComponents.hpp"

namespace onebox::survive30s {

inline constexpr float kWorldWidth = 720.0F;
inline constexpr float kWorldHeight = 1280.0F;
inline constexpr float kGameDuration = 30.0F;
inline constexpr float kPlayerRadius = kWorldWidth * 0.035F;
inline constexpr std::uint32_t kGameCommandTheme = static_cast<std::uint32_t>(CommonCommand::GameSpecific) + 1;

SurviveState* state(EngineContext& context);
void applyPalette(EngineContext& context);
float random(SurviveState& state, float minimum = 0.0F, float maximum = 1.0F);
void emitSound(EngineContext& context, std::uint32_t soundCode);
void roundSystem(EngineContext& context, float deltaSeconds);
void playerControlSystem(EngineContext& context, float deltaSeconds);
void obstacleMotionSystem(EngineContext& context, float deltaSeconds);
void nearMissSystem(EngineContext& context, float deltaSeconds);
void collisionSystem(EngineContext& context, float deltaSeconds);
void renderStateSystem(EngineContext& context, float deltaSeconds);
void snapshotSystem(EngineContext& context, float deltaSeconds);

}  // namespace onebox::survive30s
