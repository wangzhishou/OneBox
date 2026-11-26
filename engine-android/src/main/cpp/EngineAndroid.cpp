#include <EGL/egl.h>
#include <GLES3/gl3.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <jni.h>
#include <swappy/swappyGL.h>

#include <algorithm>
#include <chrono>
#include <cstring>
#include <memory>
#include <mutex>
#include <string>
#include <thread>
#include <utility>

#include "GlRenderer.hpp"
#include "OboeAudio.hpp"
#include "onebox/core/Engine.hpp"

namespace onebox::android {
namespace {

class RenderRuntime {
public:
    RenderRuntime(Engine& engine, ANativeWindow* window, JNIEnv* env, jobject activity)
        : engine_(engine), window_(window), width_(ANativeWindow_getWidth(window)),
          height_(ANativeWindow_getHeight(window)) {
        env->GetJavaVM(&jvm_);
        if (activity) activity_ = env->NewGlobalRef(activity);
        thread_ = std::thread(&RenderRuntime::run, this);
    }

    ~RenderRuntime() {
        running_ = false;
        if (thread_.joinable()) thread_.join();
        if (window_) ANativeWindow_release(window_);
        JNIEnv* env = nullptr;
        if (jvm_ && jvm_->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_EDETACHED) {
            if (jvm_->AttachCurrentThread(&env, nullptr) == JNI_OK) {
                if (activity_) env->DeleteGlobalRef(activity_);
                jvm_->DetachCurrentThread();
            }
        } else if (env && activity_) {
            env->DeleteGlobalRef(activity_);
        }
    }

    void resize(int width, int height) {
        width_ = std::max(1, width);
        height_ = std::max(1, height);
    }

private:
    void run() {
        EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        if (display == EGL_NO_DISPLAY || eglInitialize(display, nullptr, nullptr) != EGL_TRUE) return;
        const EGLint configAttributes[] = {
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_NONE,
        };
        EGLConfig config = nullptr;
        EGLint configCount = 0;
        if (eglChooseConfig(display, configAttributes, &config, 1, &configCount) != EGL_TRUE || configCount == 0) {
            eglTerminate(display);
            return;
        }
        const EGLint contextAttributes[] = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE};
        EGLContext context = eglCreateContext(display, config, EGL_NO_CONTEXT, contextAttributes);
        EGLSurface surface = eglCreateWindowSurface(display, config, window_, nullptr);
        if (context == EGL_NO_CONTEXT || surface == EGL_NO_SURFACE) {
            if (surface != EGL_NO_SURFACE) eglDestroySurface(display, surface);
            if (context != EGL_NO_CONTEXT) eglDestroyContext(display, context);
            eglTerminate(display);
            return;
        }
        if (eglMakeCurrent(display, surface, surface, context) != EGL_TRUE) {
            eglDestroySurface(display, surface);
            eglDestroyContext(display, context);
            eglTerminate(display);
            return;
        }

        JNIEnv* env = nullptr;
        jvm_->AttachCurrentThread(&env, nullptr);
        const bool swappyInitialized = activity_ && SwappyGL_init(env, activity_);
        const bool useSwappy = swappyInitialized
            && SwappyGL_setWindow(window_)
            && SwappyGL_isEnabled();
        if (useSwappy) SwappyGL_setSwapIntervalNS(8'333'333);
        GlRenderer renderer;
        if (!renderer.initialize(width_, height_)) {
            if (swappyInitialized) SwappyGL_destroy();
            jvm_->DetachCurrentThread();
            eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
            eglDestroySurface(display, surface);
            eglDestroyContext(display, context);
            eglTerminate(display);
            return;
        }
        RenderGraph graph;
        graph.addPass(std::make_unique<ClearPass>());
        graph.addPass(std::make_unique<SDFBatchPass>());
        graph.addPass(std::make_unique<ParticlePass>());
        graph.addPass(std::make_unique<PostProcessPass>(true));

        constexpr double fixedStep = 1.0 / 120.0;
        auto previous = std::chrono::steady_clock::now();
        double accumulator = 0.0;
        while (running_) {
            const auto now = std::chrono::steady_clock::now();
            const double frameSeconds = std::min(0.05, std::chrono::duration<double>(now - previous).count());
            previous = now;
            accumulator += frameSeconds;
            int steps = 0;
            while (accumulator >= fixedStep && steps < 6) {
                engine_.update(static_cast<float>(fixedStep));
                accumulator -= fixedStep;
                ++steps;
            }
            renderer.resize(width_, height_);
            graph.execute(engine_.context().registry(), renderer);
            if (useSwappy) {
                SwappyGL_swap(display, surface);
            } else {
                eglSwapBuffers(display, surface);
            }
        }

        renderer.destroy();
        if (swappyInitialized) SwappyGL_destroy();
        jvm_->DetachCurrentThread();
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroySurface(display, surface);
        eglDestroyContext(display, context);
        eglTerminate(display);
    }

    Engine& engine_;
    ANativeWindow* window_;
    JavaVM* jvm_ = nullptr;
    jobject activity_ = nullptr;
    std::atomic<int> width_;
    std::atomic<int> height_;
    std::atomic<bool> running_{true};
    std::thread thread_;
};

class NativeHandle {
public:
    explicit NativeHandle(std::unique_ptr<IGame> game)
        : engine_(std::make_unique<Engine>(std::move(game))), audio_(engine_->audioEvents()) {
        engine_->start();
        audio_.start();
    }

    ~NativeHandle() {
        stopSurface();
        audio_.stop();
    }

    void setSurface(JNIEnv* env, ANativeWindow* window, jobject activity) {
        stopSurface();
        std::lock_guard lock(mutex_);
        if (window) runtime_ = std::make_unique<RenderRuntime>(*engine_, window, env, activity);
    }

    void stopSurface() {
        std::unique_ptr<RenderRuntime> runtime;
        {
            std::lock_guard lock(mutex_);
            runtime = std::move(runtime_);
        }
        runtime.reset();
    }

    void resize(int width, int height) {
        std::lock_guard lock(mutex_);
        if (runtime_) runtime_->resize(width, height);
    }

    Engine& engine() { return *engine_; }

private:
    std::unique_ptr<Engine> engine_;
    OboeAudio audio_;
    std::mutex mutex_;
    std::unique_ptr<RenderRuntime> runtime_;
};

NativeHandle* handle(jlong value) {
    return reinterpret_cast<NativeHandle*>(value);
}

void dispatch(NativeHandle* target, std::uint32_t type) {
    if (target) target->engine().dispatch(Command{type, 0, {}});
}

}  // namespace
}  // namespace onebox::android

using onebox::android::handle;

extern "C" JNIEXPORT jlong JNICALL
Java_com_wanbaohe_engine_android_EngineNative_nativeCreate(JNIEnv* env, jobject, jstring gameId) {
    const char* id = env->GetStringUTFChars(gameId, nullptr);
    auto game = onebox::GameRegistry::create(id ? id : "");
    if (id) env->ReleaseStringUTFChars(gameId, id);
    if (!game) return 0;
    return reinterpret_cast<jlong>(new onebox::android::NativeHandle(std::move(game)));
}

extern "C" JNIEXPORT void JNICALL
Java_com_wanbaohe_engine_android_EngineNative_nativeDestroy(JNIEnv*, jobject, jlong value) {
    delete handle(value);
}

extern "C" JNIEXPORT void JNICALL
Java_com_wanbaohe_engine_android_EngineNative_nativeSurfaceCreated(
    JNIEnv* env,
    jobject,
    jlong value,
    jobject surface,
    jobject activity
) {
    auto* target = handle(value);
    ANativeWindow* window = surface ? ANativeWindow_fromSurface(env, surface) : nullptr;
    if (target) {
        target->setSurface(env, window, activity);
    } else if (window) {
        ANativeWindow_release(window);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_wanbaohe_engine_android_EngineNative_nativeSurfaceChanged(
    JNIEnv*,
    jobject,
    jlong value,
    jint width,
    jint height
) {
    if (auto* target = handle(value)) target->resize(width, height);
}

extern "C" JNIEXPORT void JNICALL
Java_com_wanbaohe_engine_android_EngineNative_nativeSurfaceDestroyed(JNIEnv*, jobject, jlong value) {
    if (auto* target = handle(value)) target->stopSurface();
}

extern "C" JNIEXPORT void JNICALL
Java_com_wanbaohe_engine_android_EngineNative_nativeDispatchCommand(
    JNIEnv* env,
    jobject,
    jlong value,
    jint type,
    jbyteArray payload
) {
    auto* target = handle(value);
    if (!target) return;
    onebox::Command command{};
    command.type = static_cast<std::uint32_t>(type);
    if (payload) {
        const jsize length = std::min<jsize>(env->GetArrayLength(payload), command.payload.size());
        command.payloadSize = static_cast<std::uint32_t>(length);
        if (length > 0) {
            env->GetByteArrayRegion(payload, 0, length, reinterpret_cast<jbyte*>(command.payload.data()));
        }
    }
    target->engine().dispatch(command);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_wanbaohe_engine_android_EngineNative_nativeReadSnapshot(
    JNIEnv* env,
    jobject,
    jlong value,
    jobject buffer
) {
    auto* target = handle(value);
    if (!target || !buffer) return JNI_FALSE;
    void* address = env->GetDirectBufferAddress(buffer);
    const jlong capacity = env->GetDirectBufferCapacity(buffer);
    if (!address || capacity < static_cast<jlong>(sizeof(onebox::StateSnapshot))) return JNI_FALSE;
    const onebox::StateSnapshot snapshot = target->engine().context().snapshots().read();
    std::memcpy(address, &snapshot, sizeof(snapshot));
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_wanbaohe_engine_android_EngineNative_nativeSetPaused(JNIEnv*, jobject, jlong value, jboolean paused) {
    onebox::android::dispatch(
        handle(value),
        static_cast<std::uint32_t>(paused ? onebox::CommonCommand::Pause : onebox::CommonCommand::Resume)
    );
}
