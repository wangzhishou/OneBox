#include "game-survive30s/native/SurviveGame.hpp"

#include <algorithm>
#include <cstring>
#include <memory>
#include <random>

#include "game-survive30s/native/SurviveSystems.hpp"
#include "onebox/core/EngineContext.hpp"

namespace onebox::survive30s {
namespace {

float readFloat(const Command& command, std::size_t offset) {
    float value = 0.0F;
    if (command.payloadSize >= offset + sizeof(float)) {
        std::memcpy(&value, command.payload.data() + offset, sizeof(float));
    }
    return value;
}

std::array<float, 4> readColor(const Command& command, std::size_t offset) {
    std::uint32_t argb = 0;
    if (command.payloadSize >= offset + sizeof(argb)) {
        std::memcpy(&argb, command.payload.data() + offset, sizeof(argb));
    }
    return {
        static_cast<float>((argb >> 16U) & 0xFFU) / 255.0F,
        static_cast<float>((argb >> 8U) & 0xFFU) / 255.0F,
        static_cast<float>(argb & 0xFFU) / 255.0F,
        static_cast<float>((argb >> 24U) & 0xFFU) / 255.0F,
    };
}

struct SurviveRegistrar {
    SurviveRegistrar() {
        GameRegistry::registerGame("survive30s", [] { return std::make_unique<SurviveGame>(); });
    }
};

const SurviveRegistrar registrar;

}  // namespace

void SurviveGame::onAttach(EngineContext& context) {
    context.grid().configure({kWorldWidth, kWorldHeight}, 72.0F);
    context.gravity = {};
    context.events().subscribe<CollisionTag>();
    context.systems().addPrePhysics(roundSystem);
    context.systems().addPrePhysics(playerControlSystem);
    context.systems().addPrePhysics(obstacleMotionSystem);
    context.systems().addPrePhysics(nearMissSystem);
    context.systems().addPostPhysics(collisionSystem);
    context.systems().addPostPhysics(renderStateSystem);
    context.systems().addPostPhysics(snapshotSystem);
}

void SurviveGame::onStart(EngineContext& context) {
    buildWorld(context, GamePhase::Idle);
}

void SurviveGame::onCommand(EngineContext& context, const Command& command) {
    auto* gameState = state(context);
    if (!gameState) return;
    if (command.type == kGameCommandTheme) {
        gameState->palette.primary = readColor(command, 4);
        gameState->palette.secondary = readColor(command, 8);
        gameState->palette.tertiary = readColor(command, 12);
        gameState->palette.player = readColor(command, 20);
        applyPalette(context);
        return;
    }
    const auto type = static_cast<CommonCommand>(command.type);
    if (type == CommonCommand::PointerDown || type == CommonCommand::PointerMove) {
        gameState->playerTarget = {
            std::clamp(readFloat(command, 0), 0.0F, 1.0F) * kWorldWidth,
            std::clamp(readFloat(command, sizeof(float)), 0.0F, 1.0F) * kWorldHeight,
        };
        return;
    }
    if (type == CommonCommand::Pause) {
        if (gameState->phase == GamePhase::Playing) {
            gameState->phase = GamePhase::Paused;
            if (auto* body = context.registry().get<RigidBody>(gameState->player)) body->dynamic = false;
        }
        return;
    }
    if (type == CommonCommand::Resume) {
        if (gameState->phase == GamePhase::Paused) {
            gameState->phase = GamePhase::Playing;
            if (auto* body = context.registry().get<RigidBody>(gameState->player)) body->dynamic = true;
        }
        return;
    }
    if (type == CommonCommand::Reset) buildWorld(context, GamePhase::Playing);
}

void SurviveGame::onDetach(EngineContext& context) {
    context.registry().clear();
    context.events().clear();
}

void SurviveGame::buildWorld(EngineContext& context, GamePhase phase) {
    SurvivePalette palette;
    if (const auto* current = state(context)) palette = current->palette;
    auto& registry = context.registry();
    registry.clear();
    context.events().clear();

    const Entity stateEntity = registry.create();
    auto& gameState = registry.add<SurviveState>(stateEntity);
    gameState.phase = phase;
    gameState.palette = palette;
    registry.add<Camera2D>(stateEntity);
    std::random_device randomDevice;
    gameState.randomState = randomDevice() | 1U;

    const Entity danger = registry.create();
    registry.add<Transform>(danger).position = {360.0F, 640.0F};
    auto& dangerRender = registry.add<GlowRender>(danger);
    dangerRender.shape = SdfShape::RoundedBox;
    dangerRender.size = {720.0F, 1280.0F};
    dangerRender.color = palette.secondary;
    dangerRender.color[3] = 0.0F;
    gameState.dangerOverlay = danger;

    const Entity ring = registry.create();
    registry.add<Transform>(ring).position = gameState.playerTarget;
    auto& ringRender = registry.add<GlowRender>(ring);
    ringRender.size = {kPlayerRadius * 3.1F, kPlayerRadius * 3.1F};
    ringRender.color = palette.tertiary;
    ringRender.color[3] = 0.0F;
    ringRender.emissive = 0.12F;
    gameState.shieldRing = ring;

    const Entity player = registry.create();
    registry.add<Transform>(player).position = gameState.playerTarget;
    registry.add<Player>(player);
    registry.add<Velocity>(player);
    auto& body = registry.add<RigidBody>(player);
    body.dynamic = phase == GamePhase::Playing;
    body.gravityScale = 0.0F;
    auto& collider = registry.add<CircleCollider>(player);
    collider.radius = kPlayerRadius;
    collider.layer = 1;
    collider.mask = 4;
    collider.sensor = true;
    auto& render = registry.add<GlowRender>(player);
    render.size = {kPlayerRadius * 2.0F, kPlayerRadius * 2.0F};
    render.color = palette.player;
    render.emissive = 0.12F;
    render.sphericalShading = 1.0F;
    gameState.player = player;
    applyPalette(context);
}

}  // namespace onebox::survive30s
