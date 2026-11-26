#pragma once

#include <GLES3/gl3.h>

#include <vector>

#include "onebox/renderer/RenderGraph.hpp"

namespace onebox::android {

class GlRenderer final : public RenderBackend {
public:
    ~GlRenderer() override;

    bool initialize(int width, int height);
    void resize(int width, int height);
    void destroy();
    void clear(const Vec2& cameraOffset) override;
    void drawSdfBatch(const std::vector<SdfInstance>& instances) override;
    void drawParticles(const std::vector<SdfInstance>& instances) override;
    void postProcess(bool bloomEnabled) override;

private:
    struct Target {
        GLuint framebuffer = 0;
        GLuint texture = 0;
        int width = 0;
        int height = 0;
    };

    Target createTarget(int width, int height);
    void destroyTarget(Target& target);
    void drawInstances(const std::vector<SdfInstance>& instances);
    void drawFullscreen(GLuint program, GLuint texture, const char* textureUniform);
    void setTarget(const Target& target);

    int width_ = 0;
    int height_ = 0;
    Target scene_;
    Target bloomA_;
    Target bloomB_;
    GLuint sdfProgram_ = 0;
    GLuint thresholdProgram_ = 0;
    GLuint blurProgram_ = 0;
    GLuint compositeProgram_ = 0;
    GLuint vao_ = 0;
    GLuint emptyVao_ = 0;
    GLuint quadBuffer_ = 0;
    GLuint instanceBuffer_ = 0;
    std::vector<float> instanceData_;
    Vec2 cameraOffset_{};
};

}  // namespace onebox::android
