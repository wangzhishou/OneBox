#pragma once

#include <array>
#include <cstddef>
#include <cstdlib>
#include <typeindex>
#include <unordered_map>

#include "onebox/event/LockFreeQueue.hpp"

namespace onebox {

template<typename Event, std::size_t Capacity = 1024, std::size_t MaxSubscribers = 16>
class EventBus {
public:
    using Queue = SpscRingBuffer<Event, Capacity>;

    template<typename Tag>
    Queue& subscribe() {
        const auto type = std::type_index(typeid(Tag));
        const auto existing = subscribers_.find(type);
        if (existing != subscribers_.end()) return queues_[existing->second];
        const std::size_t index = subscribers_.size();
        if (index >= MaxSubscribers) std::abort();
        subscribers_.emplace(type, index);
        return queues_[index];
    }

    void publish(const Event& event) {
        for (std::size_t index = 0; index < subscribers_.size(); ++index) {
            queues_[index].push(event);
        }
    }

    void clear() {
        for (auto& queue : queues_) queue.clear();
    }

private:
    std::array<Queue, MaxSubscribers> queues_{};
    std::unordered_map<std::type_index, std::size_t> subscribers_;
};

}  // namespace onebox
