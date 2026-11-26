#pragma once

#include <array>
#include <cstdint>

#include "onebox/event/LockFreeQueue.hpp"

namespace onebox {

enum class CommonCommand : std::uint32_t {
    PointerDown = 1,
    PointerMove = 2,
    PointerUp = 3,
    Pause = 4,
    Resume = 5,
    Reset = 6,
    GameSpecific = 256,
};

struct Command {
    std::uint32_t type = 0;
    std::uint32_t payloadSize = 0;
    std::array<std::uint8_t, 64> payload{};
};

using CommandQueue = SpscRingBuffer<Command, 256>;

struct AudioEvent {
    float frequency = 440.0F;
    float amplitude = 0.2F;
    float durationSeconds = 0.12F;
};

using AudioEventQueue = SpscRingBuffer<AudioEvent, 512>;

}  // namespace onebox
