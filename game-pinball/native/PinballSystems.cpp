#include "game-pinball/native/PinballSystems.hpp"

#include <algorithm>
#include <array>
#include <cmath>
#include <vector>

namespace onebox::pinball {
namespace {

void setBallSize(Registry& registry, Entity entity, float radius) {
    if (auto* collider = registry.get<CircleCollider>(entity)) collider->radius = radius;
    if (auto* render = registry.get<GlowRender>(entity)) render->size = {radius * 2.0F, radius * 2.0F};
}

Vec2 rotated(const Vec2& value, float angle) {
    const float cosine = std::cos(angle);
    const float sine = std::sin(angle);
    return {value.x * cosine - value.y * sine, value.x * sine + value.y * cosine};
}

void splitBall(EngineContext& context, Entity source) {
    auto& registry = context.registry();
    const auto* velocity = registry.get<Velocity>(source);
    const auto* transform = registry.get<Transform>(source);
    const auto* ball = registry.get<Ball>(source);
    if (!velocity || !transform || !ball || length(velocity->value) < 0.001F) return;

    auto& balls = registry.pool<Ball>();
    if (balls.entities().size() >= kMaxBalls) return;
    const Vec2 position = transform->position;
    const Vec2 sourceVelocity = velocity->value;
    const float enlargeSeconds = ball->enlargeSeconds;
    Vec2 leftVelocity = rotated(sourceVelocity, -0.42F);
    Vec2 rightVelocity = rotated(sourceVelocity, 0.42F);
    if (leftVelocity.y > 0.0F) leftVelocity.y = -leftVelocity.y;
    if (rightVelocity.y > 0.0F) rightVelocity.y = -rightVelocity.y;
    addBall(context, position, leftVelocity, enlargeSeconds);
    if (balls.entities().size() < kMaxBalls) {
        addBall(context, position, rightVelocity, enlargeSeconds);
    }
}

void setAllBallsDynamic(Registry& registry, bool dynamic) {
    for (const Entity entity : registry.pool<Ball>().entities()) {
        if (auto* body = registry.get<RigidBody>(entity)) body->dynamic = dynamic;
    }
}

}  // namespace

PinballState* state(EngineContext& context) {
    auto& states = context.registry().pool<PinballState>();
    return states.empty() ? nullptr : &states.front();
}

Entity addBall(EngineContext& context, Vec2 position, Vec2 velocity, float enlargeSeconds) {
    auto& registry = context.registry();
    const auto* gameState = state(context);
    const float radius = enlargeSeconds > 0.0F ? kEnlargedBallRadius : kDefaultBallRadius;
    const Entity ball = registry.create();
    registry.add<Transform>(ball).position = position;
    auto& body = registry.add<RigidBody>(ball);
    body.dynamic = length(velocity) > 0.001F;
    body.gravityScale = 0.35F;
    registry.add<Velocity>(ball).value = velocity;
    auto& collider = registry.add<CircleCollider>(ball);
    collider.radius = radius;
    collider.restitution = 0.98F;
    collider.layer = 1;
    registry.add<Ball>(ball) = {!body.dynamic, enlargeSeconds};
    auto& render = registry.add<GlowRender>(ball);
    render.size = {radius * 2.0F, radius * 2.0F};
    render.color = gameState ? gameState->palette.ball : PinballPalette{}.ball;
    render.emissive = 0.2F;
    return ball;
}

void applyPalette(EngineContext& context) {
    auto* gameState = state(context);
    if (!gameState) return;
    auto& registry = context.registry();
    const auto& palette = gameState->palette;
    for (const Entity entity : registry.pool<Brick>().entities()) {
        const auto* brick = registry.get<Brick>(entity);
        auto* render = registry.get<GlowRender>(entity);
        if (!brick || !render) continue;
        switch (brick->effect) {
            case BrickEffect::Normal: render->color = palette.normalBrick; break;
            case BrickEffect::Split: render->color = palette.splitBrick; break;
            case BrickEffect::Enlarge: render->color = palette.enlargeBrick; break;
        }
    }
    for (const Entity entity : registry.pool<BorderTag>().entities()) {
        if (auto* render = registry.get<GlowRender>(entity)) render->color = palette.border;
    }
    for (const Entity entity : registry.pool<Ball>().entities()) {
        if (auto* render = registry.get<GlowRender>(entity)) render->color = palette.ball;
    }
    if (auto* render = registry.get<GlowRender>(gameState->paddle)) render->color = palette.splitBrick;
    for (auto& camera : registry.pool<Camera2D>().values()) {
        camera.clearColor = palette.background;
        camera.toneMappingExposure = 0.0F;
        camera.bloomStrength = 0.0F;
        camera.additiveBlending = false;
        camera.alignWorldToTop = true;
    }
}

void resetBall(EngineContext& context) {
    auto* gameState = state(context);
    if (!gameState) return;
    auto& registry = context.registry();
    gameState->phase = Phase::Ready;
    gameState->combo = 0;
    gameState->comboSeconds = 0.0F;
    if (!registry.has<Ball>(gameState->ball)) {
        gameState->ball = addBall(context, {360.0F, 1128.0F}, {});
    }
    std::vector<Entity> extraBalls;
    for (const Entity entity : registry.pool<Ball>().entities()) {
        if (entity != gameState->ball) extraBalls.push_back(entity);
    }
    for (const Entity entity : extraBalls) registry.destroy(entity);

    const auto* paddle = registry.get<Transform>(gameState->paddle);
    if (auto* transform = registry.get<Transform>(gameState->ball)) {
        transform->position = {paddle ? paddle->position.x : 360.0F, paddle ? paddle->position.y - 32.0F : 1128.0F};
    }
    if (auto* velocity = registry.get<Velocity>(gameState->ball)) velocity->value = {};
    if (auto* body = registry.get<RigidBody>(gameState->ball)) body->dynamic = false;
    if (auto* ball = registry.get<Ball>(gameState->ball)) *ball = {};
    setBallSize(registry, gameState->ball, kDefaultBallRadius);
}

void paddleControlSystem(EngineContext& context, float deltaSeconds) {
    auto* gameState = state(context);
    if (!gameState || gameState->phase == Phase::Paused) return;
    auto& registry = context.registry();
    auto* paddle = registry.get<Transform>(gameState->paddle);
    auto* paddleVelocity = registry.get<Velocity>(gameState->paddle);
    if (!paddle) return;
    const auto* collider = registry.get<BoxCollider>(gameState->paddle);
    const float margin = collider ? collider->halfSize.x + 12.0F : 76.0F;
    const float previousX = paddle->position.x;
    const float follow = 1.0F - std::exp(-26.0F * deltaSeconds);
    paddle->position.x = std::clamp(
        paddle->position.x + (gameState->paddleTargetX - paddle->position.x) * follow,
        margin,
        kWorldWidth - margin
    );
    if (paddleVelocity) paddleVelocity->value = {(paddle->position.x - previousX) / deltaSeconds, 0.0F};
    const auto* ball = registry.get<Ball>(gameState->ball);
    if (ball && ball->attached) {
        if (auto* transform = registry.get<Transform>(gameState->ball)) {
            transform->position = {paddle->position.x, paddle->position.y - 32.0F};
        }
    }
}

void ballMotionSystem(EngineContext& context, float deltaSeconds) {
    auto* gameState = state(context);
    if (!gameState || gameState->phase != Phase::Playing) return;
    auto& registry = context.registry();
    const float targetSpeed = 570.0F + std::min(gameState->level, 8U) * 20.0F;
    for (const Entity entity : registry.pool<Ball>().entities()) {
        auto* ball = registry.get<Ball>(entity);
        if (ball && ball->enlargeSeconds > 0.0F) {
            ball->enlargeSeconds = std::max(0.0F, ball->enlargeSeconds - deltaSeconds);
            if (ball->enlargeSeconds == 0.0F) setBallSize(registry, entity, kDefaultBallRadius);
        }
        auto* velocity = registry.get<Velocity>(entity);
        if (!velocity || length(velocity->value) < 0.001F) continue;
        Vec2 value = normalized(velocity->value) * targetSpeed;
        if (std::abs(value.y) < 170.0F) {
            value.y = (value.y < 0.0F ? -170.0F : 170.0F);
            value = normalized(value) * targetSpeed;
        }
        velocity->value = value;
    }
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

void brickEffectSystem(EngineContext& context, float) {
    auto& registry = context.registry();
    auto& queue = context.events().subscribe<BrickEffectTag>();
    CollisionEvent event{};
    while (queue.pop(event)) {
        if (!registry.has<Ball>(event.entityA)) continue;
        auto* brick = registry.get<Brick>(event.entityB);
        if (!brick || brick->hitPoints != -1) continue;
        if (brick->effect == BrickEffect::Split) {
            splitBall(context, event.entityA);
        } else if (brick->effect == BrickEffect::Enlarge) {
            if (auto* ball = registry.get<Ball>(event.entityA)) {
                ball->enlargeSeconds = kEnlargeDurationSeconds;
                setBallSize(registry, event.entityA, kEnlargedBallRadius);
            }
        }
        brick->hitPoints = -2;
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
    auto& registry = context.registry();
    gameState->audioCooldown = std::max(0.0F, gameState->audioCooldown - deltaSeconds);
    if (gameState->phase != Phase::Playing) return;
    if (gameState->comboSeconds > 0.0F) {
        gameState->comboSeconds = std::max(0.0F, gameState->comboSeconds - deltaSeconds);
        if (gameState->comboSeconds == 0.0F) gameState->combo = 0;
    }
    if (gameState->remainingBricks == 0) {
        gameState->phase = Phase::LevelComplete;
        setAllBallsDynamic(registry, false);
        return;
    }

    std::vector<Entity> survivors;
    std::vector<Entity> lost;
    for (const Entity entity : registry.pool<Ball>().entities()) {
        const auto* transform = registry.get<Transform>(entity);
        if (transform && transform->position.y < kWorldHeight + 40.0F) {
            survivors.push_back(entity);
        } else {
            lost.push_back(entity);
        }
    }
    if (lost.empty()) return;
    if (!survivors.empty()) {
        const bool primaryLost = std::find(lost.begin(), lost.end(), gameState->ball) != lost.end();
        for (const Entity entity : lost) registry.destroy(entity);
        if (primaryLost) gameState->ball = survivors.front();
        return;
    }

    const Entity keeper = lost.front();
    for (const Entity entity : lost) {
        if (entity != keeper) registry.destroy(entity);
    }
    gameState->ball = keeper;
    --gameState->lives;
    if (gameState->lives == 0) {
        gameState->phase = Phase::GameOver;
        if (auto* body = registry.get<RigidBody>(keeper)) body->dynamic = false;
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
    const float magnitude = camera.trauma * camera.trauma * 2.0F;
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
    std::uint32_t enlargedBalls = 0;
    float enlargeSeconds = 0.0F;
    const auto& balls = context.registry().pool<Ball>();
    for (const auto& ball : balls.values()) {
        if (ball.enlargeSeconds > 0.0F) ++enlargedBalls;
        enlargeSeconds = std::max(enlargeSeconds, ball.enlargeSeconds);
    }
    snapshot.integers[6] = static_cast<std::uint32_t>(balls.entities().size());
    snapshot.integers[7] = enlargedBalls;
    snapshot.floats[0] = gameState->comboSeconds / 2.5F;
    snapshot.floats[1] = enlargeSeconds;
    context.snapshots().publish(snapshot);
}

}  // namespace onebox::pinball
