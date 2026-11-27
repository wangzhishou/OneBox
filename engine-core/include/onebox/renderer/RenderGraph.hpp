#pragma once

#include <array>
#include <memory>
#include <vector>

#include "onebox/core/Components.hpp"
#include "onebox/ecs/Registry.hpp"

namespace onebox {

struct SdfInstance {
    Vec2 position{};
    Vec2 size{};
    float rotation = 0.0F;
    SdfShape shape = SdfShape::Circle;
    float emissive = 0.0F;
    float sphericalShading = 0.0F;
    std::array<float, 4> color{};
};

class RenderBackend {
public:
    virtual ~RenderBackend() = default;
    virtual void clear(const Camera2D& camera) = 0;
    virtual void drawSdfBatch(const std::vector<SdfInstance>& instances) = 0;
    virtual void drawParticles(const std::vector<SdfInstance>& instances) = 0;
    virtual void postProcess(bool bloomEnabled) = 0;
};

class RenderPass {
public:
    virtual ~RenderPass() = default;
    virtual void execute(const Registry& registry, RenderBackend& backend) = 0;
};

class ClearPass final : public RenderPass {
public:
    void execute(const Registry& registry, RenderBackend& backend) override;
};

class SDFBatchPass final : public RenderPass {
public:
    void execute(const Registry& registry, RenderBackend& backend) override;
};

class ParticlePass final : public RenderPass {
public:
    void execute(const Registry& registry, RenderBackend& backend) override;
};

class PostProcessPass final : public RenderPass {
public:
    explicit PostProcessPass(bool enabled = true) : enabled_(enabled) {}
    void execute(const Registry& registry, RenderBackend& backend) override;

private:
    bool enabled_;
};

class RenderGraph {
public:
    void addPass(std::unique_ptr<RenderPass> pass);
    void execute(const Registry& registry, RenderBackend& backend);

private:
    std::vector<std::unique_ptr<RenderPass>> passes_;
};

}  // namespace onebox
