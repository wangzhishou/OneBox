#include "GlRenderer.hpp"

#include <android/log.h>

#include <algorithm>
#include <array>
#include <cmath>
#include <cstring>

namespace onebox::android {
namespace {

constexpr const char* kSdfVertex = R"(#version 300 es
precision highp float;
layout(location=0) in vec2 aCorner;
layout(location=1) in vec2 iPosition;
layout(location=2) in vec2 iSize;
layout(location=3) in vec4 iMeta;
layout(location=4) in vec4 iColor;
uniform vec2 uWorldSize;
uniform vec2 uCamera;
uniform vec2 uShapeClipScale;
out vec2 vLocal;
out vec4 vColor;
out float vShape;
out float vEmissive;
out float vSphericalShading;
void main() {
    float angle = iMeta.x;
    float c = cos(angle);
    float s = sin(angle);
    vec2 local = aCorner * iSize;
    vec2 rotated = vec2(local.x*c-local.y*s, local.x*s+local.y*c);
    vec2 center = iPosition - uCamera;
    vec2 clip = vec2(center.x/uWorldSize.x*2.0-1.0, 1.0-center.y/uWorldSize.y*2.0);
    clip += vec2(rotated.x*uShapeClipScale.x, -rotated.y*uShapeClipScale.y);
    gl_Position = vec4(clip, 0.0, 1.0);
    vLocal = aCorner;
    vColor = iColor;
    vShape = iMeta.y;
    vEmissive = iMeta.z;
    vSphericalShading = iMeta.w;
}
)";

constexpr const char* kSdfFragment = R"(#version 300 es
precision highp float;
in vec2 vLocal;
in vec4 vColor;
in float vShape;
in float vEmissive;
in float vSphericalShading;
out vec4 fragColor;
void main() {
    vec2 p = vLocal * 2.0;
    float distanceField;
    if (vShape < 0.5) {
        distanceField = length(p) - 1.0;
    } else {
        vec2 q = abs(p) - vec2(0.82);
        distanceField = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - 0.18;
    }
    float antialias = max(fwidth(distanceField), 0.006);
    float alpha = (1.0 - smoothstep(-antialias, antialias, distanceField)) * vColor.a;
    if (alpha < 0.002) discard;
    float emission = 0.8 + vEmissive * 1.5;
    vec3 light = vColor.rgb * emission;
    if (vSphericalShading > 0.0) {
        vec3 normal = vec3(p, sqrt(max(0.0, 1.0 - dot(p, p))));
        vec3 lightDirection = normalize(vec3(-0.45, -0.65, 0.8));
        float diffuse = 0.62 + 0.48 * max(dot(normal, lightDirection), 0.0);
        vec3 halfDirection = normalize(lightDirection + vec3(0.0, 0.0, 1.0));
        float highlight = pow(max(dot(normal, halfDirection), 0.0), 28.0) * 0.32;
        vec3 spherical = clamp((vColor.rgb * diffuse + vec3(highlight)) * emission, 0.0, 1.0);
        light = mix(light, spherical, clamp(vSphericalShading, 0.0, 1.0));
    }
    fragColor = vec4(light, alpha);
}
)";

constexpr const char* kFullscreenVertex = R"(#version 300 es
precision highp float;
out vec2 vUv;
void main() {
    vec2 position = gl_VertexID == 0 ? vec2(-1.0,-1.0) : (gl_VertexID == 1 ? vec2(3.0,-1.0) : vec2(-1.0,3.0));
    vUv = position * 0.5 + 0.5;
    gl_Position = vec4(position, 0.0, 1.0);
}
)";

constexpr const char* kThresholdFragment = R"(#version 300 es
precision highp float;
in vec2 vUv;
uniform sampler2D uTexture;
out vec4 fragColor;
void main() {
    vec3 color = texture(uTexture, vUv).rgb;
    float brightness = max(color.r, max(color.g, color.b));
    float contribution = max(brightness - 0.48, 0.0) / max(brightness, 0.001);
    fragColor = vec4(color * contribution, 1.0);
}
)";

constexpr const char* kBlurFragment = R"(#version 300 es
precision highp float;
in vec2 vUv;
uniform sampler2D uTexture;
uniform vec2 uDirection;
out vec4 fragColor;
void main() {
    vec3 color = texture(uTexture, vUv).rgb * 0.227027;
    color += texture(uTexture, vUv + uDirection * 1.384615).rgb * 0.316216;
    color += texture(uTexture, vUv - uDirection * 1.384615).rgb * 0.316216;
    color += texture(uTexture, vUv + uDirection * 3.230769).rgb * 0.070270;
    color += texture(uTexture, vUv - uDirection * 3.230769).rgb * 0.070270;
    fragColor = vec4(color, 1.0);
}
)";

constexpr const char* kCompositeFragment = R"(#version 300 es
precision highp float;
in vec2 vUv;
uniform sampler2D uTexture;
uniform sampler2D uBloom;
uniform float uBloomStrength;
uniform float uExposure;
out vec4 fragColor;
void main() {
    vec4 sceneSample = texture(uTexture, vUv);
    vec3 bloom = texture(uBloom, vUv).rgb * uBloomStrength;
    vec2 centered = vUv * 2.0 - 1.0;
    float vignette = 1.0 - dot(centered, centered) * 0.16;
    vec3 color = (sceneSample.rgb + bloom * 0.8) * vignette;
    if (uExposure > 0.0) color = vec3(1.0) - exp(-color * uExposure);
    fragColor = vec4(color, sceneSample.a);
}
)";

GLuint compileShader(GLenum type, const char* source) {
    const GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);
    GLint compiled = GL_FALSE;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (compiled == GL_TRUE) return shader;
    std::array<char, 1024> log{};
    glGetShaderInfoLog(shader, log.size(), nullptr, log.data());
    __android_log_write(ANDROID_LOG_ERROR, "OneBoxEngine", log.data());
    glDeleteShader(shader);
    return 0;
}

GLuint createProgram(const char* vertexSource, const char* fragmentSource) {
    const GLuint vertex = compileShader(GL_VERTEX_SHADER, vertexSource);
    const GLuint fragment = compileShader(GL_FRAGMENT_SHADER, fragmentSource);
    if (vertex == 0 || fragment == 0) {
        if (vertex != 0) glDeleteShader(vertex);
        if (fragment != 0) glDeleteShader(fragment);
        return 0;
    }
    const GLuint program = glCreateProgram();
    glAttachShader(program, vertex);
    glAttachShader(program, fragment);
    glLinkProgram(program);
    glDeleteShader(vertex);
    glDeleteShader(fragment);
    GLint linked = GL_FALSE;
    glGetProgramiv(program, GL_LINK_STATUS, &linked);
    if (linked == GL_TRUE) return program;
    std::array<char, 1024> log{};
    glGetProgramInfoLog(program, log.size(), nullptr, log.data());
    __android_log_write(ANDROID_LOG_ERROR, "OneBoxEngine", log.data());
    glDeleteProgram(program);
    return 0;
}

}  // namespace

GlRenderer::~GlRenderer() {
    destroy();
}

bool GlRenderer::initialize(int width, int height) {
    sdfProgram_ = createProgram(kSdfVertex, kSdfFragment);
    thresholdProgram_ = createProgram(kFullscreenVertex, kThresholdFragment);
    blurProgram_ = createProgram(kFullscreenVertex, kBlurFragment);
    compositeProgram_ = createProgram(kFullscreenVertex, kCompositeFragment);
    if (sdfProgram_ == 0 || thresholdProgram_ == 0 || blurProgram_ == 0 || compositeProgram_ == 0) return false;

    glGenVertexArrays(1, &vao_);
    glGenVertexArrays(1, &emptyVao_);
    glBindVertexArray(vao_);
    glGenBuffers(1, &quadBuffer_);
    glBindBuffer(GL_ARRAY_BUFFER, quadBuffer_);
    constexpr std::array<float, 12> quad{-0.5F,-0.5F, 0.5F,-0.5F, -0.5F,0.5F, -0.5F,0.5F, 0.5F,-0.5F, 0.5F,0.5F};
    glBufferData(GL_ARRAY_BUFFER, sizeof(quad), quad.data(), GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), nullptr);
    glGenBuffers(1, &instanceBuffer_);
    glBindBuffer(GL_ARRAY_BUFFER, instanceBuffer_);
    constexpr GLsizei stride = 12 * sizeof(float);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, stride, nullptr);
    glVertexAttribDivisor(1, 1);
    glEnableVertexAttribArray(2);
    glVertexAttribPointer(2, 2, GL_FLOAT, GL_FALSE, stride, reinterpret_cast<void*>(2 * sizeof(float)));
    glVertexAttribDivisor(2, 1);
    glEnableVertexAttribArray(3);
    glVertexAttribPointer(3, 4, GL_FLOAT, GL_FALSE, stride, reinterpret_cast<void*>(4 * sizeof(float)));
    glVertexAttribDivisor(3, 1);
    glEnableVertexAttribArray(4);
    glVertexAttribPointer(4, 4, GL_FLOAT, GL_FALSE, stride, reinterpret_cast<void*>(8 * sizeof(float)));
    glVertexAttribDivisor(4, 1);
    resize(width, height);
    return true;
}

GlRenderer::Target GlRenderer::createTarget(int width, int height) {
    Target target{};
    target.width = width;
    target.height = height;
    glGenTextures(1, &target.texture);
    glBindTexture(GL_TEXTURE_2D, target.texture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glGenFramebuffers(1, &target.framebuffer);
    glBindFramebuffer(GL_FRAMEBUFFER, target.framebuffer);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, target.texture, 0);
    return target;
}

void GlRenderer::destroyTarget(Target& target) {
    if (target.framebuffer != 0) glDeleteFramebuffers(1, &target.framebuffer);
    if (target.texture != 0) glDeleteTextures(1, &target.texture);
    target = {};
}

void GlRenderer::resize(int width, int height) {
    width = std::max(1, width);
    height = std::max(1, height);
    if (width == width_ && height == height_) return;
    width_ = width;
    height_ = height;
    destroyTarget(scene_);
    destroyTarget(bloomA_);
    destroyTarget(bloomB_);
    scene_ = createTarget(width_, height_);
    bloomA_ = createTarget(std::max(1, width_ / 2), std::max(1, height_ / 2));
    bloomB_ = createTarget(std::max(1, width_ / 2), std::max(1, height_ / 2));
}

void GlRenderer::destroy() {
    destroyTarget(scene_);
    destroyTarget(bloomA_);
    destroyTarget(bloomB_);
    if (sdfProgram_ != 0) glDeleteProgram(sdfProgram_);
    if (thresholdProgram_ != 0) glDeleteProgram(thresholdProgram_);
    if (blurProgram_ != 0) glDeleteProgram(blurProgram_);
    if (compositeProgram_ != 0) glDeleteProgram(compositeProgram_);
    if (quadBuffer_ != 0) glDeleteBuffers(1, &quadBuffer_);
    if (instanceBuffer_ != 0) glDeleteBuffers(1, &instanceBuffer_);
    if (vao_ != 0) glDeleteVertexArrays(1, &vao_);
    if (emptyVao_ != 0) glDeleteVertexArrays(1, &emptyVao_);
    sdfProgram_ = thresholdProgram_ = blurProgram_ = compositeProgram_ = 0;
    quadBuffer_ = instanceBuffer_ = vao_ = emptyVao_ = 0;
    width_ = height_ = 0;
}

void GlRenderer::clear(const Camera2D& camera) {
    cameraOffset_ = camera.offset;
    toneMappingExposure_ = camera.toneMappingExposure;
    bloomStrength_ = camera.bloomStrength;
    alignWorldToTop_ = camera.alignWorldToTop;
    fillViewport_ = camera.fillViewport;
    glBindFramebuffer(GL_FRAMEBUFFER, scene_.framebuffer);
    glViewport(0, 0, width_, height_);
    glDisable(GL_DEPTH_TEST);
    glClearColor(camera.clearColor[0], camera.clearColor[1], camera.clearColor[2], camera.clearColor[3]);
    glClear(GL_COLOR_BUFFER_BIT);
    glEnable(GL_BLEND);
    if (camera.additiveBlending) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
    } else {
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    }
}

void GlRenderer::drawSdfBatch(const std::vector<SdfInstance>& instances) {
    drawInstances(instances);
}

void GlRenderer::drawParticles(const std::vector<SdfInstance>& instances) {
    drawInstances(instances);
}

void GlRenderer::drawInstances(const std::vector<SdfInstance>& instances) {
    if (instances.empty()) return;
    instanceData_.clear();
    instanceData_.reserve(instances.size() * 12);
    for (const auto& instance : instances) {
        instanceData_.insert(instanceData_.end(), {
            instance.position.x, instance.position.y,
            instance.size.x, instance.size.y,
            instance.rotation, static_cast<float>(instance.shape), instance.emissive, instance.sphericalShading,
            instance.color[0], instance.color[1], instance.color[2], instance.color[3],
        });
    }
    glBindFramebuffer(GL_FRAMEBUFFER, scene_.framebuffer);
    const float scale = std::min(width_ / 720.0F, height_ / 1280.0F);
    if (fillViewport_) {
        glViewport(0, 0, width_, height_);
    } else {
        const int viewportWidth = static_cast<int>(720.0F * scale);
        const int viewportHeight = static_cast<int>(1280.0F * scale);
        const int viewportY = alignWorldToTop_ ? height_ - viewportHeight : (height_ - viewportHeight) / 2;
        glViewport((width_ - viewportWidth) / 2, viewportY, viewportWidth, viewportHeight);
    }
    glUseProgram(sdfProgram_);
    glUniform2f(glGetUniformLocation(sdfProgram_, "uWorldSize"), 720.0F, 1280.0F);
    glUniform2f(glGetUniformLocation(sdfProgram_, "uCamera"), cameraOffset_.x, cameraOffset_.y);
    const float shapeClipX = fillViewport_ ? 2.0F * scale / width_ : 2.0F / 720.0F;
    const float shapeClipY = fillViewport_ ? 2.0F * scale / height_ : 2.0F / 1280.0F;
    glUniform2f(glGetUniformLocation(sdfProgram_, "uShapeClipScale"), shapeClipX, shapeClipY);
    glBindVertexArray(vao_);
    glBindBuffer(GL_ARRAY_BUFFER, instanceBuffer_);
    glBufferData(GL_ARRAY_BUFFER, instanceData_.size() * sizeof(float), instanceData_.data(), GL_DYNAMIC_DRAW);
    glDrawArraysInstanced(GL_TRIANGLES, 0, 6, static_cast<GLsizei>(instances.size()));
}

void GlRenderer::setTarget(const Target& target) {
    glBindFramebuffer(GL_FRAMEBUFFER, target.framebuffer);
    glViewport(0, 0, target.width, target.height);
}

void GlRenderer::drawFullscreen(GLuint program, GLuint texture, const char* textureUniform) {
    glUseProgram(program);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);
    glUniform1i(glGetUniformLocation(program, textureUniform), 0);
    glBindVertexArray(emptyVao_);
    glDrawArrays(GL_TRIANGLES, 0, 3);
}

void GlRenderer::postProcess(bool bloomEnabled) {
    glDisable(GL_BLEND);
    if (bloomEnabled) {
        setTarget(bloomA_);
        drawFullscreen(thresholdProgram_, scene_.texture, "uTexture");
        setTarget(bloomB_);
        glUseProgram(blurProgram_);
        glUniform2f(glGetUniformLocation(blurProgram_, "uDirection"), 1.0F / bloomA_.width, 0.0F);
        drawFullscreen(blurProgram_, bloomA_.texture, "uTexture");
        setTarget(bloomA_);
        glUseProgram(blurProgram_);
        glUniform2f(glGetUniformLocation(blurProgram_, "uDirection"), 0.0F, 1.0F / bloomB_.height);
        drawFullscreen(blurProgram_, bloomB_.texture, "uTexture");
    }
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glViewport(0, 0, width_, height_);
    glUseProgram(compositeProgram_);
    glUniform1f(glGetUniformLocation(compositeProgram_, "uBloomStrength"), bloomEnabled ? bloomStrength_ : 0.0F);
    glUniform1f(glGetUniformLocation(compositeProgram_, "uExposure"), toneMappingExposure_);
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, bloomA_.texture);
    glUniform1i(glGetUniformLocation(compositeProgram_, "uBloom"), 1);
    glActiveTexture(GL_TEXTURE0);
    drawFullscreen(compositeProgram_, scene_.texture, "uTexture");
}

}  // namespace onebox::android
