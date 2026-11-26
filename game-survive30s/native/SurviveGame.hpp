#pragma once

#include "game-survive30s/native/SurviveComponents.hpp"
#include "onebox/core/Game.hpp"

namespace onebox::survive30s {

class SurviveGame final : public IGame {
public:
    void onAttach(EngineContext& context) override;
    void onStart(EngineContext& context) override;
    void onCommand(EngineContext& context, const Command& command) override;
    void onDetach(EngineContext& context) override;

private:
    void buildWorld(EngineContext& context, GamePhase phase);
};

}  // namespace onebox::survive30s
