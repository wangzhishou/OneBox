#pragma once

#include <algorithm>
#include <cstdint>
#include <memory>
#include <typeindex>
#include <unordered_map>
#include <utility>
#include <vector>

namespace onebox {

using Entity = std::uint32_t;
inline constexpr Entity kInvalidEntity = 0;

class ComponentPoolBase {
public:
    virtual ~ComponentPoolBase() = default;
    virtual void remove(Entity entity) = 0;
    virtual void clear() = 0;
};

template<typename T>
class ComponentPool final : public ComponentPoolBase {
public:
    template<typename... Args>
    T& add(Entity entity, Args&&... args) {
        if (contains(entity)) {
            *get(entity) = T{std::forward<Args>(args)...};
            return *get(entity);
        }
        if (sparse_.size() <= entity) sparse_.resize(entity + 1, 0);
        sparse_[entity] = static_cast<std::uint32_t>(entities_.size() + 1);
        entities_.push_back(entity);
        values_.push_back(T{std::forward<Args>(args)...});
        return values_.back();
    }

    bool contains(Entity entity) const {
        return entity < sparse_.size() && sparse_[entity] != 0;
    }

    T* get(Entity entity) {
        return contains(entity) ? &values_[sparse_[entity] - 1] : nullptr;
    }

    const T* get(Entity entity) const {
        return contains(entity) ? &values_[sparse_[entity] - 1] : nullptr;
    }

    void remove(Entity entity) override {
        if (!contains(entity)) return;
        const std::uint32_t denseIndex = sparse_[entity] - 1;
        const std::uint32_t lastIndex = static_cast<std::uint32_t>(entities_.size() - 1);
        if (denseIndex != lastIndex) {
            entities_[denseIndex] = entities_[lastIndex];
            values_[denseIndex] = std::move(values_[lastIndex]);
            sparse_[entities_[denseIndex]] = denseIndex + 1;
        }
        entities_.pop_back();
        values_.pop_back();
        sparse_[entity] = 0;
    }

    void clear() override {
        entities_.clear();
        values_.clear();
        std::fill(sparse_.begin(), sparse_.end(), 0);
    }

    const std::vector<Entity>& entities() const { return entities_; }
    std::vector<T>& values() { return values_; }
    const std::vector<T>& values() const { return values_; }
    bool empty() const { return entities_.empty(); }
    T& front() { return values_.front(); }
    const T& front() const { return values_.front(); }

private:
    std::vector<std::uint32_t> sparse_;
    std::vector<Entity> entities_;
    std::vector<T> values_;
};

class Registry {
public:
    Entity create() { return ++nextEntity_; }

    void destroy(Entity entity) {
        for (auto& [type, pool] : pools_) pool->remove(entity);
    }

    void clear() {
        for (auto& [type, pool] : pools_) pool->clear();
        nextEntity_ = 0;
    }

    template<typename T>
    ComponentPool<T>& pool() {
        const auto type = std::type_index(typeid(T));
        auto [it, inserted] = pools_.try_emplace(type, nullptr);
        if (inserted) it->second = std::make_unique<ComponentPool<T>>();
        return *static_cast<ComponentPool<T>*>(it->second.get());
    }

    template<typename T>
    const ComponentPool<T>& pool() const {
        const auto it = pools_.find(std::type_index(typeid(T)));
        static const ComponentPool<T> empty;
        return it == pools_.end() ? empty : *static_cast<const ComponentPool<T>*>(it->second.get());
    }

    template<typename T, typename... Args>
    T& add(Entity entity, Args&&... args) {
        return pool<T>().add(entity, std::forward<Args>(args)...);
    }

    template<typename T>
    T* get(Entity entity) { return pool<T>().get(entity); }

    template<typename T>
    const T* get(Entity entity) const { return pool<T>().get(entity); }

    template<typename T>
    bool has(Entity entity) const { return pool<T>().contains(entity); }

private:
    Entity nextEntity_ = 0;
    std::unordered_map<std::type_index, std::unique_ptr<ComponentPoolBase>> pools_;
};

}  // namespace onebox
