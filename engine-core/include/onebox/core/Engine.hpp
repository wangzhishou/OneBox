#pragma once

#include <memory>

#include "onebox/core/EngineContext.hpp"
#include "onebox/core/Game.hpp"

namespace onebox {

class Engine {
public:
    explicit Engine(std::unique_ptr<IGame> game);
    ~Engine();

    void start();
    void update(float deltaSeconds);
    bool dispatch(const Command& command);

    EngineContext& context() { return context_; }
    AudioEventQueue& audioEvents() { return audioEvents_; }

private:
    Registry registry_;
    CollisionEventBus events_;
    CommandQueue commands_;
    AudioEventQueue audioEvents_;
    SnapshotPublisher snapshots_;
    UniformGrid grid_;
    WorkerThread worker_;
    SystemScheduler systems_;
    EngineContext context_;
    std::unique_ptr<IGame> game_;
    bool started_ = false;
};

}  // namespace onebox
