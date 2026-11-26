#include "onebox/core/Engine.hpp"

#include "onebox/particles/ParticleSystem.hpp"
#include "onebox/physics/PhysicsSystem.hpp"

namespace onebox {

Engine::Engine(std::unique_ptr<IGame> game)
    : context_(registry_, events_, audioEvents_, snapshots_, grid_, systems_, worker_),
      game_(std::move(game)) {
    events_.subscribe<ParticleSystemTag>();
    if (game_) game_->onAttach(context_);
}

Engine::~Engine() {
    if (game_) game_->onDetach(context_);
}

void Engine::start() {
    if (started_ || !game_) return;
    started_ = true;
    game_->onStart(context_);
}

void Engine::update(float deltaSeconds) {
    if (!started_) return;
    Command command{};
    while (commands_.pop(command)) game_->onCommand(context_, command);

    context_.elapsedSeconds += deltaSeconds;
    for (const auto system : systems_.prePhysics()) system(context_, deltaSeconds);
    spatialGridSystem(context_, deltaSeconds);
    physicsSystem(context_, deltaSeconds);
    for (const auto system : systems_.postPhysics()) system(context_, deltaSeconds);
    particleSystem(context_, deltaSeconds);
}

bool Engine::dispatch(const Command& command) {
    return commands_.push(command);
}

}  // namespace onebox
