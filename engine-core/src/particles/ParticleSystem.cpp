#include "onebox/particles/ParticleSystem.hpp"

#include <algorithm>
#include <array>
#include <cmath>
#include <cstdint>
#include <vector>

#include "onebox/core/Components.hpp"
#include "onebox/core/EngineContext.hpp"

namespace onebox {

namespace {

float random(std::uint32_t& state) {
    state ^= state << 13U;
    state ^= state >> 17U;
    state ^= state << 5U;
    return static_cast<float>(state & 0xFFFFFFU) / static_cast<float>(0x1000000U);
}

void spawnSparks(EngineContext& context, const CollisionEvent& event, std::uint32_t& randomState) {
    auto& registry = context.registry();
    const auto* render = registry.get<GlowRender>(event.entityB);
    const std::array<float, 4> color = render
        ? render->color
        : std::array<float, 4>{0.3F, 0.85F, 1.0F, 1.0F};
    for (int index = 0; index < 15; ++index) {
        const float angle = random(randomState) * 6.2831853F;
        const float speed = 90.0F + random(randomState) * 230.0F;
        const float lifetime = 0.22F + random(randomState) * 0.35F;
        const Entity particle = registry.create();
        registry.add<Transform>(particle).position = event.point;
        registry.add<Velocity>(particle).value = {std::cos(angle) * speed, std::sin(angle) * speed};
        auto& glow = registry.add<GlowRender>(particle);
        glow.shape = SdfShape::Circle;
        glow.size = {3.0F + random(randomState) * 5.0F, 3.0F + random(randomState) * 5.0F};
        glow.color = color;
        glow.emissive = 1.2F;
        registry.add<Particle>(particle) = {lifetime, lifetime};
    }
}

}  // namespace

void particleSystem(EngineContext& context, float deltaSeconds) {
    auto& queue = context.events().subscribe<ParticleSystemTag>();
    CollisionEvent event{};
    std::uint32_t randomState = static_cast<std::uint32_t>(context.elapsedSeconds * 1000000.0F) | 1U;
    while (queue.pop(event)) {
        if (event.impulse > 8.0F) spawnSparks(context, event, randomState);
    }

    auto& registry = context.registry();
    auto& particles = registry.pool<Particle>();
    std::vector<Entity> expired;
    for (const Entity entity : particles.entities()) {
        auto* particle = particles.get(entity);
        particle->remaining -= deltaSeconds;
        if (particle->remaining <= 0.0F) {
            expired.push_back(entity);
            continue;
        }
        if (auto* render = registry.get<GlowRender>(entity)) {
            render->color[3] = std::clamp(particle->remaining / particle->lifetime, 0.0F, 1.0F);
        }
    }
    for (const Entity entity : expired) registry.destroy(entity);
}

}  // namespace onebox
