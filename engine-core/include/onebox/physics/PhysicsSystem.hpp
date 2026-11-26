#pragma once

namespace onebox {

class EngineContext;
void spatialGridSystem(EngineContext& context, float deltaSeconds);
void physicsSystem(EngineContext& context, float deltaSeconds);

}  // namespace onebox
