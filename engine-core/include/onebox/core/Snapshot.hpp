#pragma once

#include <array>
#include <atomic>
#include <cstdint>

namespace onebox {

struct StateSnapshot {
    std::uint64_t sequence = 0;
    std::uint32_t eventSequence = 0;
    float eventStrength = 0.0F;
    std::array<std::uint32_t, 16> integers{};
    std::array<float, 16> floats{};
};

class SnapshotPublisher {
public:
    void publish(const StateSnapshot& snapshot) {
        const std::uint32_t index = 1U - published_.load(std::memory_order_relaxed);
        auto& slot = slots_[index];
        const std::uint32_t guard = slot.guard.load(std::memory_order_relaxed);
        slot.guard.store(guard + 1, std::memory_order_release);
        StateSnapshot next = snapshot;
        next.sequence = ++sequence_;
        slot.value = next;
        slot.guard.store(guard + 2, std::memory_order_release);
        published_.store(index, std::memory_order_release);
    }

    StateSnapshot read() const {
        const std::uint32_t index = published_.load(std::memory_order_acquire);
        const auto& slot = slots_[index];
        for (;;) {
            const std::uint32_t before = slot.guard.load(std::memory_order_acquire);
            if ((before & 1U) != 0U) continue;
            StateSnapshot result = slot.value;
            const std::uint32_t after = slot.guard.load(std::memory_order_acquire);
            if (before == after) return result;
        }
    }

private:
    struct Slot {
        std::atomic<std::uint32_t> guard{0};
        StateSnapshot value{};
    };

    Slot slots_[2]{};
    std::atomic<std::uint32_t> published_{0};
    std::uint64_t sequence_ = 0;
};

}  // namespace onebox
