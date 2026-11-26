#pragma once

#include <cmath>

namespace onebox {

struct Vec2 {
    float x = 0.0F;
    float y = 0.0F;

    constexpr Vec2 operator+(const Vec2& other) const { return {x + other.x, y + other.y}; }
    constexpr Vec2 operator-(const Vec2& other) const { return {x - other.x, y - other.y}; }
    constexpr Vec2 operator*(float scalar) const { return {x * scalar, y * scalar}; }
    constexpr Vec2 operator/(float scalar) const { return {x / scalar, y / scalar}; }
    Vec2& operator+=(const Vec2& other) {
        x += other.x;
        y += other.y;
        return *this;
    }
    Vec2& operator-=(const Vec2& other) {
        x -= other.x;
        y -= other.y;
        return *this;
    }
};

constexpr float dot(const Vec2& a, const Vec2& b) { return a.x * b.x + a.y * b.y; }
inline float length(const Vec2& value) { return std::sqrt(dot(value, value)); }
inline Vec2 normalized(const Vec2& value) {
    const float magnitude = length(value);
    return magnitude > 0.00001F ? value / magnitude : Vec2{};
}

}  // namespace onebox
