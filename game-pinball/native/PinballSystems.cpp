#include "game-pinball/native/PinballSystems.hpp"

#include <algorithm>
#include <array>
#include <cmath>
#include <vector>

namespace onebox::pinball {

PinballState* state(EngineContext& context) {
    auto& states = context.registry().pool<PinballState>();
    return states.empty() ? nullptr : &states.front();
}

void resetBall(EngineContext& context) {
    auto* gameState = state(context);
    if (!gameState) return;
    gameState->phase = Phase::Ready;
    gameState->combo = 0;
    gameState->comboSeconds = 0.0F;
    if (auto* transform = context.registry().get<Transform>(gameState->ball)) {
        const auto* paddle = context.registry().get<Transform>(gameState->paddle);
        transform->position = {paddle ? paddle->position.x : 360.0F, 1128.0F};
    }
    if (auto* velocity = context.registry().get<Velocity>(gameState->ball)) velocity->value = {};
    if (auto* body = context.registry().get<RigidBody>(gameState->ball)) body->dynamic = false;
    if (auto* ball = context.registry().get<Ball>(gameState->ball)) ball->attached = true;
}

void paddleControlSystem(EngineContext& context, float deltaSeconds) {
    auto* gameState = state(context);
    if (!gameState || gameState->phase == Phase::Paused) return;
    auto* paddle = context.registry().get<Transform>(gameState->paddle);
    auto* paddleVelocity = context.registry().get<Velocity>(gameState->paddle);
    if (!paddle) return;
    const float previousX = paddle->position.x;
    const float follow = 1.0F - std::exp(-26.0F * deltaSeconds);
    paddle->position.x = std::clamp(
        paddle->position.x + (gameState->paddleTargetX - paddle->position.x) * follow,
        76.0F,
        kWorldWidth - 76.0F
    );
    if (paddleVelocity) paddleVelocity->value = {(paddle->position.x - previousX) / deltaSeconds, 0.0F};
    const auto* ball = context.registry().get<Ball>(gameState->ball);
    if (ball && ball->attached) {
        if (auto* transform = context.registry().get<Transform>(gameState->ball)) {
            transform->position = {paddle->position.x, paddle->position.y - 32.0F};
        }
    }
}

void ballMotionSystem(EngineContext& context, float) {
    auto* gameState = state(context);
    if (!gameState || gameState->phase != Phase::Playing) return;
    auto* velocity = context.registry().get<Velocity>(gameState->ball);
    if (!velocity) return;
    const float targetSpeed = 570.0F + std::min(gameState->level, 8U) * 20.0F;
    Vec2 value = normalized(velocity->value) * targetSpeed;
    if (std::abs(value.y) < 170.0F) {
        value.y = (value.y < 0.0F ? -170.0F : 170.0F);
        value = normalized(value) * targetSpeed;
    }
    velocity->value = value;
}

void brickDamageSystem(EngineContext& context, float) {
    auto& queue = context.events().subscribe<BrickDamageTag>();
    CollisionEvent event{};
    while (queue.pop(event)) {
        if (!context.registry().has<Ball>(event.entityA)) continue;
        if (auto* brick = context.registry().get<Brick>(event.entityB)) {
            if (brick->hitPoints > 0) --brick->hitPoints;
        }
    }
}

void comboScoreSystem(EngineContext& context, float) {
    auto* gameState = state(context);
    if (!gameState) return;
    auto& queue = context.events().subscribe<ComboScoreTag>();
    CollisionEvent event{};
    while (queue.pop(event)) {
        if (!context.registry().has<Ball>(event.entityA)) continue;
        if (auto* brick = context.registry().get<Brick>(event.entityB); brick && brick->hitPoints == 0) {
            ++gameState->combo;
            gameState->comboSeconds = 2.5F;
            const std::uint32_t multiplier = 1U + std::min(gameState->combo / 4U, 5U);
            gameState->score += brick->score * multiplier;
            --gameState->remainingBricks;
            brick->hitPoints = -1;
        } else if (const auto* bumper = context.registry().get<Bumper>(event.entityB)) {
            gameState->score += bumper->score;
        }
    }
}

void brickCleanupSystem(EngineContext& context, float) {
    auto& registry = context.registry();
    std::vector<Entity> destroyed;
    for (const Entity entity : registry.pool<Brick>().entities()) {
        if (registry.get<Brick>(entity)->hitPoints < 0) destroyed.push_back(entity);
    }
    for (const Entity entity : destroyed) registry.destroy(entity);
}

void roundSystem(EngineContext& context, float deltaSeconds) {
    auto* gameState = state(context);
    if (!gameState) return;
    gameState->audioCooldown = std::max(0.0F, gameState->audioCooldown - deltaSeconds);
    if (gameState->phase != Phase::Playing) return;
    if (gameState->comboSeconds > 0.0F) {
        gameState->comboSeconds = std::max(0.0F, gameState->comboSeconds - deltaSeconds);
        if (gameState->comboSeconds == 0.0F) gameState->combo = 0;
    }
    if (gameState->remainingBricks == 0) {
        gameState->phase = Phase::LevelComplete;
        if (auto* body = context.registry().get<RigidBody>(gameState->ball)) body->dynamic = false;
        return;
    }
    const auto* ballTransform = context.registry().get<Transform>(gameState->ball);
    if (!ballTransform || ballTransform->position.y < kWorldHeight + 40.0F) return;
    --gameState->lives;
    if (gameState->lives == 0) {
        gameState->phase = Phase::GameOver;
        if (auto* body = context.registry().get<RigidBody>(gameState->ball)) body->dynamic = false;
    } else {
        resetBall(context);
    }
}

void pentatonicAudioSystem(EngineContext& context, float) {
    auto* gameState = state(context);
    if (!gameState) return;
    auto& queue = context.events().subscribe<PentatonicAudioTag>();
    CollisionEvent event{};
    while (queue.pop(event)) {
        if (event.impulse < 12.0F || gameState->audioCooldown > 0.0F) continue;
        constexpr std::array<float, 6> scale{261.63F, 293.66F, 329.63F, 392.0F, 440.0F, 523.25F};
        const float frequency = scale[gameState->combo % scale.size()];
        const float amplitude = std::clamp(0.08F + event.impulse * 0.00035F, 0.08F, 0.32F);
        context.audioEvents().push({frequency, amplitude, 0.14F});
        gameState->audioCooldown = 0.045F;
    }
}

void hapticsSystem(EngineContext& context, float) {
    auto* gameState = state(context);
    if (!gameState) return;
    auto& queue = context.events().subscribe<HapticsTag>();
    CollisionEvent event{};
    while (queue.pop(event)) {
        if (event.impulse < 30.0F) continue;
        ++gameState->hapticSequence;
        gameState->hapticStrength = std::clamp(event.impulse / 750.0F, 0.15F, 1.0F);
    }
}

void cameraSystem(EngineContext& context, float deltaSeconds) {
    auto& queue = context.events().subscribe<CameraTag>();
    CollisionEvent event{};
    auto& cameras = context.registry().pool<Camera2D>();
    if (cameras.empty()) return;
    auto& camera = cameras.front();
    while (queue.pop(event)) camera.trauma = std::min(1.0F, camera.trauma + event.impulse * 0.00035F);
    camera.trauma = std::max(0.0F, camera.trauma - deltaSeconds * 2.8F);
    const float magnitude = camera.trauma * camera.trauma * 4.0F;
    camera.offset = {
        std::sin(context.elapsedSeconds * 71.0F) * magnitude,
        std::cos(context.elapsedSeconds * 59.0F) * magnitude,
    };
}

void snapshotSystem(EngineContext& context, float) {
    const auto* gameState = state(context);
    if (!gameState) return;
    StateSnapshot snapshot{};
    snapshot.eventSequence = gameState->hapticSequence;
    snapshot.eventStrength = gameState->hapticStrength;
    snapshot.integers[0] = gameState->score;
    snapshot.integers[1] = gameState->combo;
    snapshot.integers[2] = gameState->lives;
    snapshot.integers[3] = gameState->remainingBricks;
    snapshot.integers[4] = static_cast<std::uint32_t>(gameState->phase);
    snapshot.integers[5] = gameState->level;
    snapshot.floats[0] = gameState->comboSeconds / 2.5F;
    context.snapshots().publish(snapshot);
}

}  // namespace onebox::pinball
