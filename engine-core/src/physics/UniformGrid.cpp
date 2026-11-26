#include "onebox/physics/UniformGrid.hpp"

#include <algorithm>
#include <cmath>

#include "onebox/core/Components.hpp"

namespace onebox {

void UniformGrid::configure(Vec2 worldSize, float cellSize) {
    worldSize_ = worldSize;
    cellSize_ = std::max(8.0F, cellSize);
}

void UniformGrid::rebuild(const Registry& registry) {
    for (auto& [key, entities] : buckets_) entities.clear();
    const auto& transforms = registry.pool<Transform>();
    for (const Entity entity : transforms.entities()) {
        const Transform& transform = *transforms.get(entity);
        Vec2 minimum{};
        Vec2 maximum{};
        if (const auto* circle = registry.get<CircleCollider>(entity)) {
            minimum = transform.position - Vec2{circle->radius, circle->radius};
            maximum = transform.position + Vec2{circle->radius, circle->radius};
        } else if (const auto* box = registry.get<BoxCollider>(entity)) {
            const float cosine = std::abs(std::cos(transform.rotation));
            const float sine = std::abs(std::sin(transform.rotation));
            const Vec2 extent{
                box->halfSize.x * cosine + box->halfSize.y * sine,
                box->halfSize.x * sine + box->halfSize.y * cosine,
            };
            minimum = transform.position - extent;
            maximum = transform.position + extent;
        } else {
            continue;
        }
        const int minX = static_cast<int>(std::floor(minimum.x / cellSize_));
        const int minY = static_cast<int>(std::floor(minimum.y / cellSize_));
        const int maxX = static_cast<int>(std::floor(maximum.x / cellSize_));
        const int maxY = static_cast<int>(std::floor(maximum.y / cellSize_));
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) buckets_[key(x, y)].push_back(entity);
        }
    }
}

}  // namespace onebox
