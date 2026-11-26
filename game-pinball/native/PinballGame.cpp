#include "game-pinball/native/PinballGame.hpp"

#include <algorithm>
#include <array>
#include <cmath>
#include <cstring>
#include <memory>

#include "game-pinball/native/PinballSystems.hpp"
#include "onebox/core/EngineContext.hpp"

namespace onebox::pinball {
namespace {

float readFloat(const Command& command, std::size_t offset) {
    float value = 0.0F;
    if (command.payloadSize >= offset + sizeof(float)) {
        std::memcpy(&value, command.payload.data() + offset, sizeof(float));
    }
    return value;
}

Entity addDecoration(
    EngineContext& context,
    Vec2 position,
    Vec2 size,
    std::array<float, 4> color,
    float emissive,
    float rotation = 0.0F,
    SdfShape shape = SdfShape::RoundedBox
) {
    const Entity entity = context.registry().create();
    auto& transform = context.registry().add<Transform>(entity);
    transform.position = position;
    transform.rotation = rotation;
    auto& render = context.registry().add<GlowRender>(entity);
    render.shape = shape;
    render.size = size;
    render.color = color;
    render.emissive = emissive;
    return entity;
}

Entity addWall(EngineContext& context, Vec2 position, Vec2 halfSize) {
    const Entity entity = context.registry().create();
    context.registry().add<Transform>(entity).position = position;
    context.registry().add<RigidBody>(entity);
    auto& collider = context.registry().add<BoxCollider>(entity);
    collider.halfSize = halfSize;
    collider.restitution = 0.98F;
    collider.layer = 2;
    auto& render = context.registry().add<GlowRender>(entity);
    render.shape = SdfShape::RoundedBox;
    render.size = halfSize * 2.0F;
    render.color = {0.12F, 0.55F, 0.72F, 0.65F};
    render.emissive = 0.8F;
    return entity;
}

struct PinballRegistrar {
    PinballRegistrar() {
        GameRegistry::registerGame("pinball", [] { return std::make_unique<PinballGame>(); });
    }
};

const PinballRegistrar registrar;

}  // namespace

void PinballGame::onAttach(EngineContext& context) {
    context.grid().configure({kWorldWidth, kWorldHeight}, 80.0F);
    context.gravity = {0.0F, 18.0F};
    context.events().subscribe<PaddleControlTag>();
    context.events().subscribe<BrickDamageTag>();
    context.events().subscribe<ComboScoreTag>();
    context.events().subscribe<PentatonicAudioTag>();
    context.events().subscribe<HapticsTag>();
    context.events().subscribe<CameraTag>();
    context.systems().addPrePhysics(paddleControlSystem);
    context.systems().addPrePhysics(ballMotionSystem);
    context.systems().addPostPhysics(brickDamageSystem);
    context.systems().addPostPhysics(comboScoreSystem);
    context.systems().addPostPhysics(brickCleanupSystem);
    context.systems().addPostPhysics(roundSystem);
    context.systems().addPostPhysics(pentatonicAudioSystem);
    context.systems().addPostPhysics(hapticsSystem);
    context.systems().addPostPhysics(cameraSystem);
    context.systems().addPostPhysics(snapshotSystem);
}

void PinballGame::onStart(EngineContext& context) {
    buildWorld(context, 0, 3, 1);
}

void PinballGame::onCommand(EngineContext& context, const Command& command) {
    auto* gameState = state(context);
    if (!gameState) return;
    const auto type = static_cast<CommonCommand>(command.type);
    if (type == CommonCommand::PointerDown || type == CommonCommand::PointerMove) {
        gameState->paddleTargetX = std::clamp(readFloat(command, 0) * kWorldWidth, 76.0F, kWorldWidth - 76.0F);
        return;
    }
    if (type == CommonCommand::Pause) {
        if (gameState->phase == Phase::Playing || gameState->phase == Phase::Ready) {
            gameState->phaseBeforePause = gameState->phase;
            gameState->phase = Phase::Paused;
            if (auto* body = context.registry().get<RigidBody>(gameState->ball)) body->dynamic = false;
        }
        return;
    }
    if (type == CommonCommand::Resume) {
        if (gameState->phase == Phase::Paused) {
            gameState->phase = gameState->phaseBeforePause;
            if (auto* body = context.registry().get<RigidBody>(gameState->ball)) {
                body->dynamic = gameState->phase == Phase::Playing;
            }
        }
        return;
    }
    if (type == CommonCommand::Reset) {
        buildWorld(context, 0, 3, 1);
        return;
    }
    if (command.type != kGameCommandLaunch) return;
    if (gameState->phase == Phase::GameOver) {
        buildWorld(context, 0, 3, 1);
    } else if (gameState->phase == Phase::LevelComplete) {
        buildWorld(context, gameState->score, gameState->lives, gameState->level + 1);
    }
    launch(context);
}

void PinballGame::onDetach(EngineContext& context) {
    context.registry().clear();
    context.events().clear();
}

void PinballGame::buildWorld(
    EngineContext& context,
    std::uint32_t score,
    std::uint32_t lives,
    std::uint32_t level
) {
    auto& registry = context.registry();
    registry.clear();
    context.events().clear();

    const Entity stateEntity = registry.create();
    auto& gameState = registry.add<PinballState>(stateEntity);
    gameState.score = score;
    gameState.lives = lives;
    gameState.level = level;
    registry.add<Camera2D>(stateEntity);

    addDecoration(context, {120, 220}, {330, 330}, {0.16F, 0.08F, 0.5F, 0.12F}, 0.8F, 0, SdfShape::Circle);
    addDecoration(context, {640, 650}, {430, 430}, {0.0F, 0.45F, 0.55F, 0.1F}, 0.8F, 0, SdfShape::Circle);
    addDecoration(context, {180, 1130}, {360, 360}, {0.35F, 0.04F, 0.4F, 0.09F}, 0.7F, 0, SdfShape::Circle);
    addDecoration(context, {340, 760}, {620, 16}, {0.1F, 0.65F, 0.8F, 0.08F}, 0.4F, 0.5F);
    addDecoration(context, {410, 900}, {560, 12}, {0.65F, 0.16F, 0.7F, 0.07F}, 0.4F, -0.65F);
    for (int index = 0; index < 34; ++index) {
        const float x = std::fmod(47.0F + index * 137.0F, kWorldWidth);
        const float y = std::fmod(83.0F + index * 239.0F, kWorldHeight);
        const float size = 1.5F + static_cast<float>(index % 3);
        addDecoration(context, {x, y}, {size, size}, {0.55F, 0.8F, 1.0F, 0.35F}, 0.8F, 0, SdfShape::Circle);
    }

    addWall(context, {-4.0F, 640.0F}, {8.0F, 648.0F});
    addWall(context, {724.0F, 640.0F}, {8.0F, 648.0F});
    addWall(context, {360.0F, -4.0F}, {368.0F, 8.0F});

    constexpr std::array<std::array<float, 4>, 6> colors{{
        {0.68F, 0.25F, 1.0F, 0.9F},
        {0.9F, 0.22F, 0.65F, 0.9F},
        {0.2F, 0.48F, 1.0F, 0.9F},
        {0.05F, 0.85F, 0.8F, 0.9F},
        {0.45F, 0.9F, 0.35F, 0.9F},
        {1.0F, 0.65F, 0.18F, 0.9F},
    }};
    for (int row = 0; row < 6; ++row) {
        for (int column = 0; column < 8; ++column) {
            const Entity brick = registry.create();
            registry.add<Transform>(brick).position = {80.0F + column * 80.0F, 180.0F + row * 48.0F};
            registry.add<RigidBody>(brick);
            auto& collider = registry.add<BoxCollider>(brick);
            collider.halfSize = {34.0F, 15.0F};
            collider.restitution = 0.96F;
            collider.layer = 4;
            const int hitPoints = row == 0 || (level > 1 && row == 1) ? 2 : 1;
            registry.add<Brick>(brick) = {hitPoints, 100U + static_cast<std::uint32_t>((5 - row) * 20)};
            auto& render = registry.add<GlowRender>(brick);
            render.shape = SdfShape::RoundedBox;
            render.size = {68.0F, 30.0F};
            render.color = colors[row];
            render.emissive = 0.55F + row * 0.08F;
            ++gameState.remainingBricks;
        }
    }

    constexpr std::array<Vec2, 3> bumperPositions{{{178, 520}, {542, 520}, {360, 680}}};
    for (const Vec2 position : bumperPositions) {
        const Entity bumper = registry.create();
        registry.add<Transform>(bumper).position = position;
        registry.add<RigidBody>(bumper);
        auto& collider = registry.add<CircleCollider>(bumper);
        collider.radius = 34.0F;
        collider.restitution = 1.08F;
        collider.layer = 8;
        registry.add<Bumper>(bumper);
        auto& render = registry.add<GlowRender>(bumper);
        render.size = {68.0F, 68.0F};
        render.color = {0.2F, 0.75F, 1.0F, 0.85F};
        render.emissive = 1.2F;
    }

    const Entity paddle = registry.create();
    registry.add<Transform>(paddle).position = {360.0F, 1160.0F};
    registry.add<RigidBody>(paddle);
    registry.add<Velocity>(paddle);
    auto& paddleCollider = registry.add<BoxCollider>(paddle);
    paddleCollider.halfSize = {72.0F, 12.0F};
    paddleCollider.restitution = 1.0F;
    paddleCollider.layer = 16;
    auto& paddleRender = registry.add<GlowRender>(paddle);
    paddleRender.shape = SdfShape::RoundedBox;
    paddleRender.size = {144.0F, 24.0F};
    paddleRender.color = {0.25F, 0.9F, 1.0F, 0.95F};
    paddleRender.emissive = 1.3F;

    const Entity ball = registry.create();
    registry.add<Transform>(ball).position = {360.0F, 1128.0F};
    registry.add<RigidBody>(ball).gravityScale = 0.35F;
    registry.add<Velocity>(ball);
    auto& ballCollider = registry.add<CircleCollider>(ball);
    ballCollider.radius = 11.0F;
    ballCollider.restitution = 0.98F;
    ballCollider.layer = 1;
    registry.add<Ball>(ball);
    auto& ballRender = registry.add<GlowRender>(ball);
    ballRender.size = {22.0F, 22.0F};
    ballRender.color = {0.85F, 0.98F, 1.0F, 1.0F};
    ballRender.emissive = 1.8F;

    gameState.paddle = paddle;
    gameState.ball = ball;
}

void PinballGame::launch(EngineContext& context) {
    auto* gameState = state(context);
    if (!gameState || gameState->phase != Phase::Ready) return;
    gameState->phase = Phase::Playing;
    auto* ball = context.registry().get<Ball>(gameState->ball);
    auto* body = context.registry().get<RigidBody>(gameState->ball);
    auto* velocity = context.registry().get<Velocity>(gameState->ball);
    if (ball) ball->attached = false;
    if (body) body->dynamic = true;
    if (velocity) {
        const float direction = gameState->paddleTargetX < kWorldWidth * 0.5F ? 0.32F : -0.32F;
        velocity->value = normalized(Vec2{direction, -1.0F}) * (570.0F + gameState->level * 20.0F);
    }
    context.audioEvents().push({392.0F, 0.18F, 0.16F});
}

}  // namespace onebox::pinball
