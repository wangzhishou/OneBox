#pragma once

#include <functional>
#include <vector>

#include "onebox/core/Command.hpp"
#include "onebox/core/Components.hpp"
#include "onebox/core/Snapshot.hpp"
#include "onebox/core/WorkerThread.hpp"
#include "onebox/ecs/Registry.hpp"
#include "onebox/physics/CollisionEvent.hpp"
#include "onebox/physics/UniformGrid.hpp"

namespace onebox {

class EngineContext;
using System = void (*)(EngineContext& context, float deltaSeconds);

class SystemScheduler {
public:
    void addPrePhysics(System system) { prePhysics_.push_back(system); }
    void addPostPhysics(System system) { postPhysics_.push_back(system); }

    const std::vector<System>& prePhysics() const { return prePhysics_; }
    const std::vector<System>& postPhysics() const { return postPhysics_; }

private:
    std::vector<System> prePhysics_;
    std::vector<System> postPhysics_;
};

class EngineContext {
public:
    EngineContext(
        Registry& registry,
        CollisionEventBus& events,
        AudioEventQueue& audioEvents,
        SnapshotPublisher& snapshots,
        UniformGrid& grid,
        SystemScheduler& systems,
        WorkerThread& worker
    ) : registry_(registry),
        events_(events),
        audioEvents_(audioEvents),
        snapshots_(snapshots),
        grid_(grid),
        systems_(systems),
        worker_(worker) {}

    Registry& registry() { return registry_; }
    const Registry& registry() const { return registry_; }
    CollisionEventBus& events() { return events_; }
    AudioEventQueue& audioEvents() { return audioEvents_; }
    SnapshotPublisher& snapshots() { return snapshots_; }
    UniformGrid& grid() { return grid_; }
    const UniformGrid& grid() const { return grid_; }
    SystemScheduler& systems() { return systems_; }
    WorkerThread& worker() { return worker_; }

    float elapsedSeconds = 0.0F;
    Vec2 gravity{0.0F, 0.0F};

private:
    Registry& registry_;
    CollisionEventBus& events_;
    AudioEventQueue& audioEvents_;
    SnapshotPublisher& snapshots_;
    UniformGrid& grid_;
    SystemScheduler& systems_;
    WorkerThread& worker_;
};

}  // namespace onebox
