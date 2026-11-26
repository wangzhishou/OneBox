#pragma once

#include <functional>
#include <memory>
#include <string>
#include <unordered_map>

namespace onebox {

class EngineContext;
struct Command;

class IGame {
public:
    virtual ~IGame() = default;
    virtual void onAttach(EngineContext& context) = 0;
    virtual void onStart(EngineContext& context) = 0;
    virtual void onCommand(EngineContext& context, const Command& command) = 0;
    virtual void onDetach(EngineContext& context) = 0;
};

using GameFactory = std::function<std::unique_ptr<IGame>()>;

class GameRegistry {
public:
    static bool registerGame(const std::string& id, GameFactory factory);
    static std::unique_ptr<IGame> create(const std::string& id);

private:
    static std::unordered_map<std::string, GameFactory>& factories();
};

}  // namespace onebox
