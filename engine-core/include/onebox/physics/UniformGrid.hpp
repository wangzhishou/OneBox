#pragma once

#include <cstdint>
#include <unordered_map>
#include <vector>

#include "onebox/ecs/Registry.hpp"
#include "onebox/math/Vec2.hpp"

namespace onebox {

class UniformGrid {
public:
    void configure(Vec2 worldSize, float cellSize);
    void rebuild(const Registry& registry);

    template<typename Callback>
    void forEachNearby(Vec2 minimum, Vec2 maximum, Callback&& callback) const {
        if (cellSize_ <= 0.0F) return;
        const int minX = static_cast<int>(std::floor(minimum.x / cellSize_));
        const int minY = static_cast<int>(std::floor(minimum.y / cellSize_));
        const int maxX = static_cast<int>(std::floor(maximum.x / cellSize_));
        const int maxY = static_cast<int>(std::floor(maximum.y / cellSize_));
        ++queryStamp_;
        if (queryStamp_ == 0) {
            seen_.clear();
            queryStamp_ = 1;
        }
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                const auto bucket = buckets_.find(key(x, y));
                if (bucket == buckets_.end()) continue;
                for (const Entity entity : bucket->second) {
                    if (seen_.size() <= entity) seen_.resize(entity + 1, 0);
                    if (seen_[entity] == queryStamp_) continue;
                    seen_[entity] = queryStamp_;
                    callback(entity);
                }
            }
        }
    }

private:
    static std::uint64_t key(int x, int y) {
        return (static_cast<std::uint64_t>(static_cast<std::uint32_t>(x)) << 32U)
            | static_cast<std::uint32_t>(y);
    }

    Vec2 worldSize_{720.0F, 1280.0F};
    float cellSize_ = 96.0F;
    std::unordered_map<std::uint64_t, std::vector<Entity>> buckets_;
    mutable std::vector<std::uint32_t> seen_;
    mutable std::uint32_t queryStamp_ = 0;
};

}  // namespace onebox
