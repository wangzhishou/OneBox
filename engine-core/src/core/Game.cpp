#include "onebox/core/Game.hpp"

namespace onebox {

bool GameRegistry::registerGame(const std::string& id, GameFactory factory) {
    return factories().emplace(id, std::move(factory)).second;
}

std::unique_ptr<IGame> GameRegistry::create(const std::string& id) {
    const auto& allFactories = factories();
    const auto factory = allFactories.find(id);
    return factory == allFactories.end() ? nullptr : factory->second();
}

std::unordered_map<std::string, GameFactory>& GameRegistry::factories() {
    static std::unordered_map<std::string, GameFactory> value;
    return value;
}

}  // namespace onebox
