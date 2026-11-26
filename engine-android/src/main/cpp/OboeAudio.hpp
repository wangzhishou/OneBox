#pragma once

#include <array>
#include <memory>

#include <oboe/Oboe.h>

#include "onebox/core/Command.hpp"

namespace onebox::android {

class OboeAudio final : public oboe::AudioStreamDataCallback {
public:
    explicit OboeAudio(AudioEventQueue& events) : events_(events) {}
    ~OboeAudio() override;

    bool start();
    void stop();
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream* stream,
        void* audioData,
        int32_t numFrames
    ) override;

private:
    struct Voice {
        float phase = 0.0F;
        float phaseStep = 0.0F;
        float amplitude = 0.0F;
        int32_t remaining = 0;
        int32_t total = 0;
    };

    void startVoice(const AudioEvent& event);

    AudioEventQueue& events_;
    std::shared_ptr<oboe::AudioStream> stream_;
    std::array<Voice, 32> voices_{};
    int32_t sampleRate_ = 48000;
};

}  // namespace onebox::android
