#pragma once

#include <condition_variable>
#include <functional>
#include <mutex>
#include <queue>
#include <thread>

namespace onebox {

class WorkerThread {
public:
    WorkerThread();
    ~WorkerThread();

    void post(std::function<void()> task);

private:
    void run();

    std::mutex mutex_;
    std::condition_variable condition_;
    std::queue<std::function<void()>> tasks_;
    bool running_ = true;
    std::thread thread_;
};

}  // namespace onebox
