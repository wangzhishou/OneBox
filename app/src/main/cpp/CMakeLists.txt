cmake_minimum_required(VERSION 3.22.1)

project("dummy")

add_library(
        dummy
        SHARED
        dummy.cpp)

# 关闭 ELF build-id: 该 note 段是链接产物的哈希, 不同构建目录下值不同,
# 会破坏 F-Droid 可复现构建的逐字节比对; 本库只是占位 so, 无需 crash 符号化
target_link_options(dummy PRIVATE -Wl,--build-id=none)

