#pragma once

#include <array>
#include <atomic>
#include <cstddef>

namespace onebox {

template<typename T, std::size_t Capacity>
class SpscRingBuffer {
    static_assert(Capacity > 1);

public:
    bool push(const T& value) {
        const std::size_t tail = tail_.load(std::memory_order_relaxed);
        const std::size_t next = (tail + 1) % Capacity;
        if (next == head_.load(std::memory_order_acquire)) return false;
        values_[tail] = value;
        tail_.store(next, std::memory_order_release);
        return true;
    }

    bool pop(T& value) {
        const std::size_t head = head_.load(std::memory_order_relaxed);
        if (head == tail_.load(std::memory_order_acquire)) return false;
        value = values_[head];
        head_.store((head + 1) % Capacity, std::memory_order_release);
        return true;
    }

    void clear() {
        T value{};
        while (pop(value)) {}
    }

private:
    std::array<T, Capacity> values_{};
    alignas(64) std::atomic<std::size_t> head_{0};
    alignas(64) std::atomic<std::size_t> tail_{0};
};

}  // namespace onebox
