#include "game-survive30s/native/SurviveSystems.hpp"

#include <algorithm>
#include <array>
#include <cmath>
#include <vector>

namespace onebox::survive30s {
namespace {

constexpr std::array<std::array<float, 4>, 6> kObstacleColors{{
    {0.94F, 0.33F, 0.31F, 0.95F},
    {1.0F, 0.44F, 0.26F, 0.95F},
    {0.93F, 0.25F, 0.48F, 0.95F},
    {0.67F, 0.28F, 0.74F, 0.95F},
    {1.0F, 0.79F, 0.16F, 0.95F},
    {0.15F, 0.78F, 0.85F, 0.95F},
}};

float difficultyFactor(const SurviveState& state) {
    return std::clamp(state.elapsedSeconds / kGameDuration, 0.0F, 1.0F);
}

bool isVertical(ObstacleType type) {
    return type == ObstacleType::TopFall || type == ObstacleType::BottomRise;
}

Entity spawnObstacle(
    EngineContext& context,
    ObstacleType type,
    float radius,
    float verticalSpeed,
    float horizontalSpeed,
    float maxDrift
) {
    auto& registry = context.registry();
    auto& gameState = *state(context);
    const float x = random(gameState, radius, kWorldWidth - radius);
    const float y = random(gameState, radius, kWorldHeight - radius);
    Vec2 position{};
    Vec2 velocity{};
    switch (type) {
        case ObstacleType::TopFall:
            position = {x, -radius * 2.0F};
            velocity = {random(gameState, -maxDrift, maxDrift), verticalSpeed};
            break;
        case ObstacleType::BottomRise:
            position = {x, kWorldHeight + radius * 2.0F};
            velocity = {random(gameState, -maxDrift, maxDrift), -verticalSpeed};
            break;
        case ObstacleType::LeftFly:
            position = {-radius * 2.0F, y};
            velocity = {horizontalSpeed, random(gameState, -maxDrift * 0.6F, maxDrift * 0.6F)};
            break;
        case ObstacleType::RightFly:
            position = {kWorldWidth + radius * 2.0F, y};
            velocity = {-horizontalSpeed, random(gameState, -maxDrift * 0.6F, maxDrift * 0.6F)};
            break;
    }

    const Entity entity = registry.create();
    registry.add<Transform>(entity).position = position;
    registry.add<Velocity>(entity).value = velocity;
    registry.add<RigidBody>(entity);
    auto& collider = registry.add<CircleCollider>(entity);
    collider.radius = radius;
    collider.layer = 4;
    collider.mask = 1;
    collider.sensor = true;
    const std::uint32_t colorIndex = static_cast<std::uint32_t>(random(gameState, 0.0F, 6.0F)) % 6U;
    registry.add<Obstacle>(entity) = {type, colorIndex, false};
    auto& render = registry.add<GlowRender>(entity);
    render.size = {radius * 2.0F, radius * 2.0F};
    render.color = kObstacleColors[colorIndex];
    render.emissive = 0.85F;
    return entity;
}

void spawnObstacles(EngineContext& context) {
    auto& gameState = *state(context);
    const float difficulty = difficultyFactor(gameState);
    const int count = difficulty > 0.5F ? 1 + static_cast<int>(random(gameState, 0.0F, 2.0F)) : 1;
    const float verticalSpeed = kWorldHeight * random(
        gameState,
        0.35F,
        0.35F + difficulty * 0.55F
    );
    const float horizontalSpeed = kWorldWidth * random(
        gameState,
        0.32F,
        0.32F + difficulty * 0.53F
    );
    const float maxDrift = kWorldWidth * (0.16F * (0.45F + difficulty));
    for (int index = 0; index < count; ++index) {
        const float radius = kWorldWidth * random(gameState, 0.02F, 0.06F);
        ObstacleType type = ObstacleType::TopFall;
        if (gameState.difficulty == DifficultyPhase::Rush) {
            switch (static_cast<int>(random(gameState, 0.0F, 3.0F))) {
                case 0: type = ObstacleType::LeftFly; break;
                case 1: type = ObstacleType::RightFly; break;
                default: type = ObstacleType::TopFall; break;
            }
        } else if (gameState.difficulty == DifficultyPhase::Storm) {
            switch (static_cast<int>(random(gameState, 0.0F, 4.0F))) {
                case 0: type = ObstacleType::LeftFly; break;
                case 1: type = ObstacleType::RightFly; break;
                case 2: type = ObstacleType::BottomRise; break;
                default: type = ObstacleType::TopFall; break;
            }
        }
        spawnObstacle(context, type, radius, verticalSpeed, horizontalSpeed, maxDrift);
    }
}

void clearNearbyObstacles(EngineContext& context) {
    auto& registry = context.registry();
    const auto& gameState = *state(context);
    const auto* player = registry.get<Transform>(gameState.player);
    if (!player) return;
    const float blastDistance = kPlayerRadius * 3.2F;
    const float blastDistanceSquared = blastDistance * blastDistance;
    std::vector<Entity> removed;
    for (const Entity entity : registry.pool<Obstacle>().entities()) {
        const auto* obstacle = registry.get<Transform>(entity);
        if (!obstacle) continue;
        const Vec2 delta = obstacle->position - player->position;
        if (dot(delta, delta) <= blastDistanceSquared) removed.push_back(entity);
    }
    for (const Entity entity : removed) registry.destroy(entity);
}

}  // namespace

SurviveState* state(EngineContext& context) {
    auto& states = context.registry().pool<SurviveState>();
    return states.empty() ? nullptr : &states.front();
}

float random(SurviveState& state, float minimum, float maximum) {
    state.randomState ^= state.randomState << 13U;
    state.randomState ^= state.randomState >> 17U;
    state.randomState ^= state.randomState << 5U;
    const float value = static_cast<float>(state.randomState & 0xFFFFFFU) / static_cast<float>(0x1000000U);
    return minimum + value * (maximum - minimum);
}

void emitSound(EngineContext& context, std::uint32_t soundCode) {
    auto* gameState = state(context);
    if (!gameState) return;
    ++gameState->soundSequence;
    gameState->soundCode = soundCode;
}

void roundSystem(EngineContext& context, float deltaSeconds) {
    auto* gameState = state(context);
    if (!gameState || gameState->phase != GamePhase::Playing) return;
    gameState->elapsedSeconds = std::min(kGameDuration, gameState->elapsedSeconds + deltaSeconds);
    gameState->spawnSeconds += deltaSeconds;
    gameState->invincibleSeconds = std::max(0.0F, gameState->invincibleSeconds - deltaSeconds);
    gameState->nearMissSoundCooldown = std::max(0.0F, gameState->nearMissSoundCooldown - deltaSeconds);

    const auto difficulty = gameState->elapsedSeconds < 10.0F
        ? DifficultyPhase::Warmup
        : (gameState->elapsedSeconds < 20.0F ? DifficultyPhase::Rush : DifficultyPhase::Storm);
    if (difficulty != gameState->difficulty) {
        gameState->difficulty = difficulty;
        emitSound(context, SoundPhaseChange);
    }
    if (gameState->elapsedSeconds >= kGameDuration) {
        gameState->phase = GamePhase::Win;
        gameState->dangerLevel = 0.0F;
        gameState->invincibleSeconds = 0.0F;
        if (auto* body = context.registry().get<RigidBody>(gameState->player)) body->dynamic = false;
        emitSound(context, SoundWin);
    }
}

void playerControlSystem(EngineContext& context, float deltaSeconds) {
    auto* gameState = state(context);
    if (!gameState || gameState->phase != GamePhase::Playing) return;
    auto* player = context.registry().get<Transform>(gameState->player);
    if (!player) return;
    const Vec2 target{
        std::clamp(gameState->playerTarget.x, kPlayerRadius, kWorldWidth - kPlayerRadius),
        std::clamp(gameState->playerTarget.y, kPlayerRadius, kWorldHeight - kPlayerRadius),
    };
    const float follow = 1.0F - std::exp(-60.0F * deltaSeconds);
    player->position += (target - player->position) * follow;
    if (auto* velocity = context.registry().get<Velocity>(gameState->player)) velocity->value = {};
}

void obstacleMotionSystem(EngineContext& context, float deltaSeconds) {
    auto* gameState = state(context);
    if (!gameState || gameState->phase != GamePhase::Playing) return;
    auto& registry = context.registry();
    const auto entities = registry.pool<Obstacle>().entities();
    for (const Entity entity : entities) {
        const auto* obstacle = registry.get<Obstacle>(entity);
        auto* transform = registry.get<Transform>(entity);
        auto* velocity = registry.get<Velocity>(entity);
        const auto* collider = registry.get<CircleCollider>(entity);
        if (!obstacle || !transform || !velocity || !collider) continue;
        Vec2 position = transform->position + velocity->value * deltaSeconds;
        const float radius = collider->radius;
        const bool leftScreen = [&] {
            switch (obstacle->type) {
                case ObstacleType::TopFall: return position.y - radius > kWorldHeight;
                case ObstacleType::BottomRise: return position.y + radius < 0.0F;
                case ObstacleType::LeftFly: return position.x - radius > kWorldWidth;
                case ObstacleType::RightFly: return position.x + radius < 0.0F;
            }
            return false;
        }();
        if (leftScreen) {
            registry.destroy(entity);
            continue;
        }
        const float clampedX = std::clamp(position.x, radius, kWorldWidth - radius);
        const float clampedY = std::clamp(position.y, radius, kWorldHeight - radius);
        if (clampedX != position.x && isVertical(obstacle->type)) velocity->value.x = -velocity->value.x;
        if (clampedY != position.y && !isVertical(obstacle->type)) velocity->value.y = -velocity->value.y;
        transform->position = {clampedX, clampedY};
    }

    const float difficulty = difficultyFactor(*gameState);
    const int frames = std::clamp(static_cast<int>(28.0F - difficulty * 22.0F), 6, 28);
    const float interval = static_cast<float>(frames) / 60.0F;
    while (gameState->spawnSeconds >= interval) {
        gameState->spawnSeconds -= interval;
        spawnObstacles(context);
    }
}

void nearMissSystem(EngineContext& context, float) {
    auto* gameState = state(context);
    if (!gameState || gameState->phase != GamePhase::Playing) return;
    auto& registry = context.registry();
    const auto* player = registry.get<Transform>(gameState->player);
    if (!player) return;
    float nearestGap = 0.0F;
    bool hasObstacle = false;
    for (const Entity entity : registry.pool<Obstacle>().entities()) {
        const auto* obstacleTransform = registry.get<Transform>(entity);
        const auto* collider = registry.get<CircleCollider>(entity);
        auto* obstacle = registry.get<Obstacle>(entity);
        if (!obstacleTransform || !collider || !obstacle) continue;
        const Vec2 delta = player->position - obstacleTransform->position;
        const float distanceSquared = dot(delta, delta);
        const float safeRadius = kPlayerRadius + collider->radius;
        const float gap = distanceSquared - safeRadius * safeRadius;
        nearestGap = hasObstacle ? std::min(nearestGap, gap) : gap;
        hasObstacle = true;
        const float nearMissDistance = safeRadius * 1.35F;
        if (!obstacle->didNearMiss && distanceSquared < nearMissDistance * nearMissDistance) {
            obstacle->didNearMiss = true;
            ++gameState->nearMissCount;
            ++gameState->nearMissCharge;
            if (gameState->nearMissSoundCooldown <= 0.0F) {
                emitSound(context, SoundNearMiss);
                gameState->nearMissSoundCooldown = 0.15F;
            }
            if (gameState->nearMissCharge >= 3) {
                if (gameState->shieldCount < 2) {
                    ++gameState->shieldCount;
                    emitSound(context, SoundShieldGain);
                }
                gameState->nearMissCharge = gameState->shieldCount >= 2 ? 0 : gameState->nearMissCharge - 3;
            }
        }
    }
    gameState->dangerLevel = hasObstacle
        ? std::clamp(1.0F - nearestGap / (kPlayerRadius * kPlayerRadius * 10.0F), 0.0F, 1.0F)
        : 0.0F;
}

void collisionSystem(EngineContext& context, float) {
    auto* gameState = state(context);
    if (!gameState) return;
    auto& queue = context.events().subscribe<CollisionTag>();
    CollisionEvent event{};
    while (queue.pop(event)) {
        if (gameState->phase != GamePhase::Playing || gameState->invincibleSeconds > 0.0F) continue;
        if (!context.registry().has<Player>(event.entityA) || !context.registry().has<Obstacle>(event.entityB)) continue;
        if (gameState->shieldCount > 0) {
            --gameState->shieldCount;
            gameState->invincibleSeconds = 1.1F;
            clearNearbyObstacles(context);
            ++gameState->hapticSequence;
            gameState->hapticStrength = 0.6F;
            emitSound(context, SoundShieldBlock);
        } else {
            gameState->phase = GamePhase::GameOver;
            gameState->dangerLevel = 1.0F;
            if (auto* body = context.registry().get<RigidBody>(gameState->player)) body->dynamic = false;
            ++gameState->hapticSequence;
            gameState->hapticStrength = 1.0F;
            emitSound(context, SoundGameOver);
        }
    }
}

void renderStateSystem(EngineContext& context, float) {
    const auto* gameState = state(context);
    if (!gameState) return;
    auto& registry = context.registry();
    if (auto* render = registry.get<GlowRender>(gameState->dangerOverlay)) {
        render->color[3] = gameState->dangerLevel * 0.08F;
    }
    const auto* player = registry.get<Transform>(gameState->player);
    if (auto* ring = registry.get<Transform>(gameState->shieldRing); ring && player) {
        ring->position = player->position;
    }
    if (auto* ring = registry.get<GlowRender>(gameState->shieldRing)) {
        ring->color[3] = gameState->invincibleSeconds > 0.0F ? 0.42F : 0.0F;
        ring->size = {kPlayerRadius * 3.1F, kPlayerRadius * 3.1F};
    }
}

void snapshotSystem(EngineContext& context, float) {
    const auto* gameState = state(context);
    if (!gameState) return;
    StateSnapshot snapshot{};
    snapshot.eventSequence = gameState->hapticSequence;
    snapshot.eventStrength = gameState->hapticStrength;
    snapshot.integers[0] = static_cast<std::uint32_t>(gameState->phase);
    snapshot.integers[1] = gameState->shieldCount;
    snapshot.integers[2] = gameState->nearMissCount;
    snapshot.integers[3] = gameState->nearMissCharge;
    snapshot.integers[4] = static_cast<std::uint32_t>(gameState->difficulty);
    snapshot.integers[5] = gameState->soundSequence;
    snapshot.integers[6] = gameState->soundCode;
    snapshot.floats[0] = gameState->elapsedSeconds;
    snapshot.floats[1] = gameState->dangerLevel;
    snapshot.floats[2] = gameState->invincibleSeconds;
    context.snapshots().publish(snapshot);
}

}  // namespace onebox::survive30s
