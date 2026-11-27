#include "onebox/renderer/RenderGraph.hpp"

namespace onebox {
namespace {

SdfInstance makeInstance(Entity entity, const Registry& registry) {
    const auto& transform = *registry.get<Transform>(entity);
    const auto& render = *registry.get<GlowRender>(entity);
    return {
        transform.position,
        render.size,
        transform.rotation,
        render.shape,
        render.emissive,
        render.sphericalShading,
        render.color,
    };
}

}  // namespace

void ClearPass::execute(const Registry& registry, RenderBackend& backend) {
    const auto& cameras = registry.pool<Camera2D>();
    if (cameras.empty()) {
        backend.clear(Camera2D{});
        return;
    }
    backend.clear(cameras.front());
}

void SDFBatchPass::execute(const Registry& registry, RenderBackend& backend) {
    std::vector<SdfInstance> instances;
    const auto& renders = registry.pool<GlowRender>();
    instances.reserve(renders.entities().size());
    for (const Entity entity : renders.entities()) {
        if (registry.has<Particle>(entity) || !registry.has<Transform>(entity)) continue;
        instances.push_back(makeInstance(entity, registry));
    }
    backend.drawSdfBatch(instances);
}

void ParticlePass::execute(const Registry& registry, RenderBackend& backend) {
    std::vector<SdfInstance> instances;
    for (const Entity entity : registry.pool<Particle>().entities()) {
        if (!registry.has<Transform>(entity) || !registry.has<GlowRender>(entity)) continue;
        instances.push_back(makeInstance(entity, registry));
    }
    backend.drawParticles(instances);
}

void PostProcessPass::execute(const Registry&, RenderBackend& backend) {
    backend.postProcess(enabled_);
}

void RenderGraph::addPass(std::unique_ptr<RenderPass> pass) {
    passes_.push_back(std::move(pass));
}

void RenderGraph::execute(const Registry& registry, RenderBackend& backend) {
    for (auto& pass : passes_) pass->execute(registry, backend);
}

}  // namespace onebox
