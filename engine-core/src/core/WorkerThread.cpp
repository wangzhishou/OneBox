#include "onebox/core/WorkerThread.hpp"

namespace onebox {

WorkerThread::WorkerThread() : thread_(&WorkerThread::run, this) {}

WorkerThread::~WorkerThread() {
    {
        std::lock_guard lock(mutex_);
        running_ = false;
    }
    condition_.notify_one();
    if (thread_.joinable()) thread_.join();
}

void WorkerThread::post(std::function<void()> task) {
    {
        std::lock_guard lock(mutex_);
        if (running_) tasks_.push(std::move(task));
    }
    condition_.notify_one();
}

void WorkerThread::run() {
    for (;;) {
        std::function<void()> task;
        {
            std::unique_lock lock(mutex_);
            condition_.wait(lock, [this] { return !running_ || !tasks_.empty(); });
            if (!running_ && tasks_.empty()) return;
            task = std::move(tasks_.front());
            tasks_.pop();
        }
        task();
    }
}

}  // namespace onebox
