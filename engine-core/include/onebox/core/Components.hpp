#pragma once

#include <array>
#include <cstdint>

#include "onebox/math/Vec2.hpp"

namespace onebox {

struct Transform {
    Vec2 position{};
    float rotation = 0.0F;
};

struct Velocity {
    Vec2 value{};
};

struct RigidBody {
    bool dynamic = false;
    float gravityScale = 1.0F;
};

struct CircleCollider {
    float radius = 1.0F;
    float restitution = 0.9F;
    float friction = 0.0F;
    std::uint16_t layer = 1;
    std::uint16_t mask = 0xFFFFU;
    bool sensor = false;
};

struct BoxCollider {
    Vec2 halfSize{1.0F, 1.0F};
    float restitution = 0.9F;
    float friction = 0.0F;
    std::uint16_t layer = 1;
    std::uint16_t mask = 0xFFFFU;
    bool sensor = false;
};

enum class SdfShape : std::uint32_t { Circle = 0, RoundedBox = 1 };

struct GlowRender {
    SdfShape shape = SdfShape::Circle;
    Vec2 size{1.0F, 1.0F};
    std::array<float, 4> color{1.0F, 1.0F, 1.0F, 1.0F};
    float emissive = 0.0F;
};

struct Particle {
    float remaining = 0.0F;
    float lifetime = 0.0F;
};

struct Camera2D {
    Vec2 offset{};
    float trauma = 0.0F;
};

}  // namespace onebox
