#include "game-pinball/native/PinballGame.hpp"

#include <algorithm>
#include <array>
#include <cmath>
#include <cstring>
#include <memory>
#include <string_view>

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

std::uint32_t readUnsignedInt(const Command& command, std::size_t offset) {
    std::uint32_t value = 0;
    if (command.payloadSize >= offset + sizeof(value)) {
        std::memcpy(&value, command.payload.data() + offset, sizeof(value));
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

std::array<float, 4> brickColor(const PinballPalette& palette, BrickEffect effect) {
    switch (effect) {
        case BrickEffect::Split: return palette.splitBrick;
        case BrickEffect::Enlarge: return palette.enlargeBrick;
        case BrickEffect::Normal: return palette.normalBrick;
    }
    return palette.normalBrick;
}

Entity addWall(EngineContext& context, Vec2 position, Vec2 halfSize) {
    const Entity entity = context.registry().create();
    context.registry().add<Transform>(entity).position = position;
    context.registry().add<RigidBody>(entity);
    auto& collider = context.registry().add<BoxCollider>(entity);
    collider.halfSize = halfSize;
    collider.restitution = 0.98F;
    collider.layer = 2;
    return entity;
}

void addBorderBlock(EngineContext& context, Vec2 position, Vec2 size = {38.0F, 38.0F}) {
    auto& registry = context.registry();
    const Entity border = registry.create();
    registry.add<Transform>(border).position = position;
    registry.add<RigidBody>(border);
    auto& collider = registry.add<BoxCollider>(border);
    collider.halfSize = size * 0.5F;
    collider.restitution = 0.98F;
    collider.layer = 2;
    registry.add<BorderTag>(border);
    auto& render = registry.add<GlowRender>(border);
    render.shape = SdfShape::RoundedBox;
    render.size = size;
    render.color = state(context)->palette.border;
    render.emissive = 0.12F;
}

void addLabelBar(
    EngineContext& context,
    Entity brick,
    Vec2 position,
    Vec2 offset,
    Vec2 size,
    float scale,
    std::array<float, 4> color
) {
    auto& registry = context.registry();
    const Entity label = registry.create();
    registry.add<Transform>(label).position = position + offset * scale;
    registry.add<BrickLabel>(label).brick = brick;
    auto& render = registry.add<GlowRender>(label);
    render.shape = SdfShape::RoundedBox;
    render.size = size * scale;
    render.color = color;
}

void addSplitLabel(
    EngineContext& context,
    Entity brick,
    Vec2 position,
    Vec2 brickSize,
    std::array<float, 4> color
) {
    const float scale = std::clamp(std::min(brickSize.x / 40.0F, brickSize.y / 24.0F), 0.8F, 1.1F);
    addLabelBar(context, brick, position, {-9.0F, 0.0F}, {8.0F, 2.4F}, scale, color);
    addLabelBar(context, brick, position, {-9.0F, 0.0F}, {2.4F, 8.0F}, scale, color);
    addLabelBar(context, brick, position, {7.0F, -7.0F}, {10.0F, 2.4F}, scale, color);
    addLabelBar(context, brick, position, {7.0F, 0.0F}, {10.0F, 2.4F}, scale, color);
    addLabelBar(context, brick, position, {7.0F, 7.0F}, {10.0F, 2.4F}, scale, color);
    addLabelBar(context, brick, position, {12.0F, -3.5F}, {2.4F, 6.0F}, scale, color);
    addLabelBar(context, brick, position, {12.0F, 3.5F}, {2.4F, 6.0F}, scale, color);
}

void addBrick(
    EngineContext& context,
    Vec2 position,
    BrickEffect effect,
    int hitPoints,
    std::uint32_t score,
    Vec2 size = {38.0F, 38.0F}
) {
    auto& registry = context.registry();
    auto* gameState = state(context);
    const Entity brick = registry.create();
    registry.add<Transform>(brick).position = position;
    registry.add<RigidBody>(brick);
    auto& collider = registry.add<BoxCollider>(brick);
    collider.halfSize = size * 0.5F;
    collider.restitution = 0.96F;
    collider.layer = 4;
    registry.add<Brick>(brick) = {hitPoints, score, effect};
    auto& render = registry.add<GlowRender>(brick);
    render.shape = SdfShape::RoundedBox;
    render.size = size;
    render.color = brickColor(gameState->palette, effect);
    render.emissive = 0.12F;
    if (effect == BrickEffect::Split) {
        addSplitLabel(context, brick, position, size, gameState->palette.splitBrickContent);
    }
    ++gameState->remainingBricks;
}

void buildFirstLevel(EngineContext& context) {
    for (int row = 0; row < 16; ++row) {
        for (int column = 0; column < 16; ++column) {
            const Vec2 position{30.0F + column * 44.0F, 200.0F + row * 42.0F};
            const BrickEffect effect = row == 5
                ? BrickEffect::Enlarge
                : ((row + column) % 4 != 0 ? BrickEffect::Split : BrickEffect::Normal);
            addBrick(context, position, effect, 1, 100);
        }
    }
}

void buildEnclosedLevel(EngineContext& context, bool narrowEntrance) {
    constexpr std::array<std::array<int, 2>, 15> splitPositions{{
        {{1, 4}}, {{1, 12}}, {{2, 2}}, {{2, 9}}, {{3, 6}}, {{3, 13}},
        {{6, 3}}, {{6, 11}}, {{7, 7}}, {{8, 1}}, {{8, 14}}, {{9, 5}},
        {{12, 4}}, {{12, 5}}, {{12, 11}},
    }};
    constexpr std::array<std::array<int, 2>, 3> extraEnlargePositions{{
        {{2, 7}}, {{7, 4}}, {{9, 10}},
    }};
    for (int row = 0; row < 16; ++row) {
        for (int column = 0; column < 16; ++column) {
            const Vec2 position{30.0F + column * 44.0F, 200.0F + row * 42.0F};
            const bool isBottomBorder = row == 15 && (
                column <= 4 || column >= 11 ||
                (narrowEntrance && column >= 5 && column <= 10 && column != 9 && column != 10)
            );
            if (row == 0 || column == 0 || column == 15 || isBottomBorder) {
                addBorderBlock(context, position);
                continue;
            }
            if (row == 15) continue;
            if (row == 12 && column >= 6 && column <= 9) continue;
            if (row == 13 && column >= 5 && column <= 10) continue;
            if (row == 14 && column >= 4 && column <= 11) continue;

            BrickEffect effect = row == 5 ? BrickEffect::Enlarge : BrickEffect::Normal;
            const bool isSplit = std::any_of(
                splitPositions.begin(),
                splitPositions.end(),
                [row, column](const auto& cell) { return cell[0] == row && cell[1] == column; }
            );
            const bool isExtraEnlarge = std::any_of(
                extraEnlargePositions.begin(),
                extraEnlargePositions.end(),
                [row, column](const auto& cell) { return cell[0] == row && cell[1] == column; }
            );
            if (isSplit) effect = BrickEffect::Split;
            if (isExtraEnlarge) effect = BrickEffect::Enlarge;
            if (narrowEntrance && row == 11 && (column == 9 || column == 10)) {
                effect = BrickEffect::Split;
            }
            addBrick(context, position, effect, 1, 100);
        }
    }
    if (narrowEntrance) {
        addBorderBlock(context, {417.5F, 830.0F}, {33.0F, 38.0F});
        addBorderBlock(context, {478.5F, 830.0F}, {33.0F, 38.0F});
    }
}

using LevelGrid = std::array<std::string_view, 16>;

constexpr std::array<LevelGrid, 5> kDesignedLevels{{
    {{
        "################",
        "#nnnnnnnnnnnnnn#",
        "#nnnnnnssnnnnnn#",
        "#nnnnnnssnnnnnn#",
        "#nneennnnnneenn#",
        "#nnnnnnnnnnnnnn#",
        "#nnnnnnssnnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnn....nnnnn#",
        "#nnnnn....nnnnn#",
        "#nnnnnn..nnnnnn#",
        "#######..#######",
    }},
    {{
        "################",
        "#nnnnnnnnnnnnnn#",
        "#nnneennnneennn#",
        "#nnnnnnnnnnnnnn#",
        "#nnnnnnssnnnnnn#",
        "#nnnnnnnnnnnnnn#",
        "#nnn..nnnn..nnn#",
        "#nnn..nnnn..nnn#",
        "#nnn..nnnn..nnn#",
        "#nnn..nssn..nnn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#######..#######",
    }},
    {{
        "################",
        "#nnnnnnnnnnnnnn#",
        "#nnnnnnssnnnnnn#",
        "#nnn........nnn#",
        "#nnnnnn....nnnn#",
        "#nnnnnen....enn#",
        "#nnnnnnn....nnn#",
        "#nnnnnn....nnnn#",
        "#nnnnn....nnnnn#",
        "#nnnn....nnnnnn#",
        "#nne....ennnnnn#",
        "#nnn....nnnnnnn#",
        "#nnnn....nnnnnn#",
        "#nnnnn....nnnnn#",
        "#nnnnnnssnnnnnn#",
        "#######..#######",
    }},
    {{
        "################",
        "#nnnnnnnnnnnnnn#",
        "#nneennnnnneenn#",
        "#nnnnnnnnnnnnnn#",
        "#nnnnnnssnnnnnn#",
        "#nnne......ennn#",
        "#nns........snn#",
        "#nnn........nnn#",
        "#nne........enn#",
        "#nns........snn#",
        "#nnne......ennn#",
        "#nnnnn....nnnnn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#######..#######",
    }},
    {{
        "################",
        "#nneennnnnneenn#",
        "#nnnnnnssnnnnnn#",
        "#nnennn..nnnenn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnen..nennnn#",
        "#nnnnnn..nnnnnn#",
        "#......ss......#",
        "#..............#",
        "#nnnnnn..nnnnnn#",
        "#nnssnn..nnssnn#",
        "#nnnnnn..nnnnnn#",
        "#nneenn..nneenn#",
        "#nnnnnn..nnnnnn#",
        "#nnnnnn..nnnnnn#",
        "#######..#######",
    }},
}};

void buildDesignedLevel(EngineContext& context, std::uint32_t level) {
    const auto& grid = kDesignedLevels[(level - 4U) % kDesignedLevels.size()];
    for (std::size_t row = 0; row < grid.size(); ++row) {
        for (std::size_t column = 0; column < grid[row].size(); ++column) {
            const Vec2 position{
                30.0F + static_cast<float>(column) * 44.0F,
                200.0F + static_cast<float>(row) * 42.0F,
            };
            switch (grid[row][column]) {
                case '#': addBorderBlock(context, position); break;
                case 'n': addBrick(context, position, BrickEffect::Normal, 1, 100); break;
                case 's': addBrick(context, position, BrickEffect::Split, 1, 100); break;
                case 'e': addBrick(context, position, BrickEffect::Enlarge, 1, 100); break;
                default: break;
            }
        }
    }
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
    context.events().subscribe<BrickEffectTag>();
    context.events().subscribe<ComboScoreTag>();
    context.events().subscribe<PentatonicAudioTag>();
    context.events().subscribe<HapticsTag>();
    context.events().subscribe<CameraTag>();
    context.systems().addPrePhysics(paddleControlSystem);
    context.systems().addPrePhysics(ballMotionSystem);
    context.systems().addPostPhysics(paddleBounceSystem);
    context.systems().addPostPhysics(brickDamageSystem);
    context.systems().addPostPhysics(comboScoreSystem);
    context.systems().addPostPhysics(brickEffectSystem);
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
    if (command.type == kGameCommandTheme) {
        gameState->palette.border = readColor(command, 0);
        gameState->palette.normalBrick = readColor(command, 4);
        gameState->palette.splitBrick = readColor(command, 8);
        gameState->palette.enlargeBrick = readColor(command, 12);
        gameState->palette.splitBrickContent = readColor(command, 16);
        gameState->palette.ball = readColor(command, 20);
        applyPalette(context);
        return;
    }
    if (command.type == kGameCommandSelectLevel) {
        if (gameState->phase != Phase::Playing) {
            const auto level = std::clamp(readUnsignedInt(command, 0), 1U, kMaxSelectableLevel);
            buildWorld(context, 0, 3, level);
        }
        return;
    }

    const auto type = static_cast<CommonCommand>(command.type);
    if (type == CommonCommand::PointerDown || type == CommonCommand::PointerMove) {
        gameState->paddleTargetX = std::clamp(readFloat(command, 0) * kWorldWidth, 76.0F, kWorldWidth - 76.0F);
        return;
    }
    if (type == CommonCommand::Pause) {
        if (gameState->phase == Phase::Playing || gameState->phase == Phase::Ready) {
            gameState->phaseBeforePause = gameState->phase;
            gameState->phase = Phase::Paused;
            for (const Entity entity : context.registry().pool<Ball>().entities()) {
                if (auto* body = context.registry().get<RigidBody>(entity)) body->dynamic = false;
            }
        }
        return;
    }
    if (type == CommonCommand::Resume) {
        if (gameState->phase == Phase::Paused) {
            gameState->phase = gameState->phaseBeforePause;
            if (gameState->phase == Phase::Playing) {
                for (const Entity entity : context.registry().pool<Ball>().entities()) {
                    if (auto* body = context.registry().get<RigidBody>(entity)) body->dynamic = true;
                }
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
        gameState = state(context);
    } else if (gameState->phase == Phase::LevelComplete) {
        buildWorld(context, gameState->score, gameState->lives, gameState->level + 1);
        gameState = state(context);
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
    PinballPalette palette;
    if (const auto* current = state(context)) palette = current->palette;
    auto& registry = context.registry();
    registry.clear();
    context.events().clear();

    const Entity stateEntity = registry.create();
    auto& gameState = registry.add<PinballState>(stateEntity);
    gameState.score = score;
    gameState.lives = lives;
    gameState.level = level;
    gameState.palette = palette;
    registry.add<Camera2D>(stateEntity);

    addWall(context, {-4.0F, 640.0F}, {8.0F, 648.0F});
    addWall(context, {724.0F, 640.0F}, {8.0F, 648.0F});
    addWall(context, {360.0F, -4.0F}, {368.0F, 8.0F});

    if (level == 1) {
        buildFirstLevel(context);
    } else if (level == 2 || level == 3) {
        buildEnclosedLevel(context, level == 3);
    } else {
        buildDesignedLevel(context, level);
    }

    const float paddleWidth = level == 1 ? 220.0F : (level == 2 ? 180.0F : 144.0F);
    const Entity paddle = registry.create();
    registry.add<Transform>(paddle).position = {360.0F, kPaddleY};
    registry.add<RigidBody>(paddle);
    registry.add<Velocity>(paddle);
    auto& paddleCollider = registry.add<BoxCollider>(paddle);
    paddleCollider.halfSize = {paddleWidth * 0.5F, 10.0F};
    paddleCollider.restitution = 1.0F;
    paddleCollider.layer = 16;
    registry.add<PaddleTag>(paddle);
    auto& paddleRender = registry.add<GlowRender>(paddle);
    paddleRender.shape = SdfShape::RoundedBox;
    paddleRender.size = {paddleWidth, 20.0F};
    paddleRender.color = gameState.palette.ball;
    paddleRender.emissive = 0.12F;
    paddleRender.sphericalShading = 1.0F;

    gameState.paddle = paddle;
    gameState.ball = addBall(context, {360.0F, kPaddleY - 32.0F}, {});
    applyPalette(context);
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
        const float direction = gameState->level >= 3
            ? 0.0F
            : (gameState->paddleTargetX < kWorldWidth * 0.5F ? 0.32F : -0.32F);
        velocity->value = normalized(Vec2{direction, -1.0F}) * (570.0F + gameState->level * 20.0F);
    }
    context.audioEvents().push({392.0F, 0.18F, 0.16F});
}

}  // namespace onebox::pinball
