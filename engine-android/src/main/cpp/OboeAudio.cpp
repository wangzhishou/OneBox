#include "OboeAudio.hpp"

#include <algorithm>
#include <cmath>

namespace onebox::android {

OboeAudio::~OboeAudio() {
    stop();
}

bool OboeAudio::start() {
    if (stream_) return true;
    oboe::AudioStreamBuilder builder;
    const oboe::Result result = builder.setDirection(oboe::Direction::Output)
        ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
        ->setSharingMode(oboe::SharingMode::Exclusive)
        ->setFormat(oboe::AudioFormat::Float)
        ->setChannelCount(2)
        ->setDataCallback(this)
        ->openStream(stream_);
    if (result != oboe::Result::OK) {
        stream_.reset();
        oboe::AudioStreamBuilder fallback;
        if (fallback.setDirection(oboe::Direction::Output)
                ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
                ->setFormat(oboe::AudioFormat::Float)
                ->setChannelCount(2)
                ->setDataCallback(this)
                ->openStream(stream_) != oboe::Result::OK) {
            stream_.reset();
            return false;
        }
    }
    sampleRate_ = stream_->getSampleRate();
    return stream_->requestStart() == oboe::Result::OK;
}

void OboeAudio::stop() {
    if (!stream_) return;
    stream_->stop();
    stream_->close();
    stream_.reset();
    for (auto& voice : voices_) voice = {};
}

void OboeAudio::startVoice(const AudioEvent& event) {
    auto voice = std::find_if(voices_.begin(), voices_.end(), [](const Voice& value) {
        return value.remaining <= 0;
    });
    if (voice == voices_.end()) {
        voice = std::min_element(voices_.begin(), voices_.end(), [](const Voice& a, const Voice& b) {
            return a.amplitude < b.amplitude;
        });
    }
    voice->phase = 0.0F;
    voice->phaseStep = 2.0F * 3.14159265F * event.frequency / static_cast<float>(sampleRate_);
    voice->amplitude = event.amplitude;
    voice->remaining = std::max(1, static_cast<int32_t>(event.durationSeconds * sampleRate_));
    voice->total = voice->remaining;
}

oboe::DataCallbackResult OboeAudio::onAudioReady(
    oboe::AudioStream*,
    void* audioData,
    int32_t numFrames
) {
    AudioEvent event{};
    while (events_.pop(event)) startVoice(event);
    auto* output = static_cast<float*>(audioData);
    std::fill(output, output + numFrames * 2, 0.0F);
    for (auto& voice : voices_) {
        if (voice.remaining <= 0) continue;
        for (int32_t frame = 0; frame < numFrames; ++frame) {
            if (voice.remaining <= 0) break;
            const float progress = 1.0F - static_cast<float>(voice.remaining) / static_cast<float>(voice.total);
            const float attack = std::min(1.0F, progress * 45.0F);
            const float release = std::min(1.0F, static_cast<float>(voice.remaining) / (sampleRate_ * 0.045F));
            const float envelope = attack * release;
            const float sample = (std::sin(voice.phase) + std::sin(voice.phase * 2.0F) * 0.16F)
                * voice.amplitude * envelope;
            output[frame * 2] += sample;
            output[frame * 2 + 1] += sample;
            voice.phase += voice.phaseStep;
            if (voice.phase > 6.2831853F) voice.phase -= 6.2831853F;
            --voice.remaining;
        }
    }
    for (int32_t index = 0; index < numFrames * 2; ++index) {
        output[index] = std::clamp(output[index], -0.95F, 0.95F);
    }
    return oboe::DataCallbackResult::Continue;
}

}  // namespace onebox::android
