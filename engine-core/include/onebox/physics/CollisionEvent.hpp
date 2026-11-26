#pragma once

#include "onebox/ecs/Registry.hpp"
#include "onebox/event/EventBus.hpp"
#include "onebox/math/Vec2.hpp"

namespace onebox {

struct CollisionEvent {
    Entity entityA = kInvalidEntity;
    Entity entityB = kInvalidEntity;
    float impulse = 0.0F;
    Vec2 normal{};
    Vec2 point{};
};

using CollisionEventBus = EventBus<CollisionEvent>;

}  // namespace onebox
