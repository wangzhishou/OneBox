#pragma once

#include "game-pinball/native/PinballComponents.hpp"
#include "onebox/core/EngineContext.hpp"

namespace onebox::pinball {

inline constexpr float kWorldWidth = 720.0F;
inline constexpr float kWorldHeight = 1280.0F;
inline constexpr std::uint32_t kGameCommandLaunch = static_cast<std::uint32_t>(CommonCommand::GameSpecific);

PinballState* state(EngineContext& context);
void resetBall(EngineContext& context);
void paddleControlSystem(EngineContext& context, float deltaSeconds);
void ballMotionSystem(EngineContext& context, float deltaSeconds);
void brickDamageSystem(EngineContext& context, float deltaSeconds);
void comboScoreSystem(EngineContext& context, float deltaSeconds);
void brickCleanupSystem(EngineContext& context, float deltaSeconds);
void roundSystem(EngineContext& context, float deltaSeconds);
void pentatonicAudioSystem(EngineContext& context, float deltaSeconds);
void hapticsSystem(EngineContext& context, float deltaSeconds);
void cameraSystem(EngineContext& context, float deltaSeconds);
void snapshotSystem(EngineContext& context, float deltaSeconds);

}  // namespace onebox::pinball
