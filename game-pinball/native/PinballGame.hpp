#pragma once

#include "onebox/core/Game.hpp"

namespace onebox::pinball {

class PinballGame final : public IGame {
public:
    void onAttach(EngineContext& context) override;
    void onStart(EngineContext& context) override;
    void onCommand(EngineContext& context, const Command& command) override;
    void onDetach(EngineContext& context) override;

private:
    void buildWorld(EngineContext& context, std::uint32_t score, std::uint32_t lives, std::uint32_t level);
    void launch(EngineContext& context);
};

}  // namespace onebox::pinball
