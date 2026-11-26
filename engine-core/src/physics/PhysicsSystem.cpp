#include "onebox/physics/PhysicsSystem.hpp"

#include <algorithm>
#include <cmath>

#include "onebox/core/Components.hpp"
#include "onebox/core/EngineContext.hpp"

namespace onebox {
namespace {

struct Contact {
    Vec2 normal{};
    Vec2 point{};
    float penetration = 0.0F;
    bool hit = false;
};

Vec2 rotate(const Vec2& value, float angle) {
    const float cosine = std::cos(angle);
    const float sine = std::sin(angle);
    return {value.x * cosine - value.y * sine, value.x * sine + value.y * cosine};
}

Contact circleCircle(
    const Transform& aTransform,
    const CircleCollider& aCollider,
    const Transform& bTransform,
    const CircleCollider& bCollider
) {
    const Vec2 delta = aTransform.position - bTransform.position;
    const float radius = aCollider.radius + bCollider.radius;
    const float distanceSquared = dot(delta, delta);
    if (distanceSquared >= radius * radius) return {};
    const float distance = std::sqrt(std::max(distanceSquared, 0.000001F));
    const Vec2 normal = distanceSquared > 0.000001F ? delta / distance : Vec2{0.0F, -1.0F};
    return {normal, aTransform.position - normal * aCollider.radius, radius - distance, true};
}

Contact circleBox(
    const Transform& circleTransform,
    const CircleCollider& circle,
    const Transform& boxTransform,
    const BoxCollider& box
) {
    const Vec2 local = rotate(circleTransform.position - boxTransform.position, -boxTransform.rotation);
    const Vec2 closest{
        std::clamp(local.x, -box.halfSize.x, box.halfSize.x),
        std::clamp(local.y, -box.halfSize.y, box.halfSize.y),
    };
    const Vec2 delta = local - closest;
    const float distanceSquared = dot(delta, delta);
    Vec2 normalLocal{};
    float penetration = 0.0F;
    if (distanceSquared > 0.000001F) {
        if (distanceSquared >= circle.radius * circle.radius) return {};
        const float distance = std::sqrt(distanceSquared);
        normalLocal = delta / distance;
        penetration = circle.radius - distance;
    } else {
        const float xGap = box.halfSize.x - std::abs(local.x);
        const float yGap = box.halfSize.y - std::abs(local.y);
        if (xGap < yGap) {
            normalLocal = {local.x >= 0.0F ? 1.0F : -1.0F, 0.0F};
            penetration = circle.radius + xGap;
        } else {
            normalLocal = {0.0F, local.y >= 0.0F ? 1.0F : -1.0F};
            penetration = circle.radius + yGap;
        }
    }
    const Vec2 normal = rotate(normalLocal, boxTransform.rotation);
    return {normal, circleTransform.position - normal * circle.radius, penetration, true};
}

bool layersMatch(std::uint16_t aLayer, std::uint16_t aMask, std::uint16_t bLayer, std::uint16_t bMask) {
    return (aMask & bLayer) != 0U && (bMask & aLayer) != 0U;
}

void resolveContact(
    EngineContext& context,
    Entity a,
    Entity b,
    const Contact& contact,
    float restitution,
    float friction,
    bool sensor
) {
    if (!contact.hit) return;
    auto& registry = context.registry();
    auto* velocity = registry.get<Velocity>(a);
    const auto* otherVelocity = registry.get<Velocity>(b);
    const Vec2 bVelocity = otherVelocity ? otherVelocity->value : Vec2{};
    const float approach = velocity ? dot(velocity->value - bVelocity, contact.normal) : 0.0F;
    const float impulse = std::max(0.0F, -approach);
    if (!sensor) {
        registry.get<Transform>(a)->position += contact.normal * (contact.penetration + 0.001F);
        if (velocity && approach < 0.0F) {
            velocity->value -= contact.normal * ((1.0F + restitution) * approach);
            const Vec2 normalVelocity = contact.normal * dot(velocity->value, contact.normal);
            const Vec2 tangentVelocity = velocity->value - normalVelocity;
            velocity->value -= tangentVelocity * std::clamp(friction, 0.0F, 1.0F);
        }
    }
    context.events().publish({a, b, impulse, contact.normal, contact.point});
}

}  // namespace

void spatialGridSystem(EngineContext& context, float) {
    context.grid().rebuild(context.registry());
}

void physicsSystem(EngineContext& context, float deltaSeconds) {
    auto& registry = context.registry();
    auto& bodies = registry.pool<RigidBody>();
    const auto entities = bodies.entities();
    for (const Entity entity : entities) {
        auto* body = bodies.get(entity);
        auto* transform = registry.get<Transform>(entity);
        auto* velocity = registry.get<Velocity>(entity);
        auto* circle = registry.get<CircleCollider>(entity);
        if (!body || !body->dynamic || !transform || !velocity || !circle) continue;

        velocity->value += context.gravity * (body->gravityScale * deltaSeconds);
        transform->position += velocity->value * deltaSeconds;
        const Vec2 radius{circle->radius, circle->radius};
        context.grid().forEachNearby(
            transform->position - radius,
            transform->position + radius,
            [&](Entity other) {
                if (other == entity) return;
                const auto* otherBody = registry.get<RigidBody>(other);
                const auto* otherTransform = registry.get<Transform>(other);
                if (!otherBody || otherBody->dynamic || !otherTransform) return;
                if (const auto* otherCircle = registry.get<CircleCollider>(other)) {
                    if (!layersMatch(circle->layer, circle->mask, otherCircle->layer, otherCircle->mask)) return;
                    const Contact contact = circleCircle(*transform, *circle, *otherTransform, *otherCircle);
                    resolveContact(
                        context,
                        entity,
                        other,
                        contact,
                        std::max(circle->restitution, otherCircle->restitution),
                        std::max(circle->friction, otherCircle->friction),
                        circle->sensor || otherCircle->sensor
                    );
                    return;
                }
                if (const auto* box = registry.get<BoxCollider>(other)) {
                    if (!layersMatch(circle->layer, circle->mask, box->layer, box->mask)) return;
                    const Contact contact = circleBox(*transform, *circle, *otherTransform, *box);
                    resolveContact(
                        context,
                        entity,
                        other,
                        contact,
                        std::max(circle->restitution, box->restitution),
                        std::max(circle->friction, box->friction),
                        circle->sensor || box->sensor
                    );
                }
            }
        );
    }
}

}  // namespace onebox
