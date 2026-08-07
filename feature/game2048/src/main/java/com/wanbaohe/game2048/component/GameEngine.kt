package com.wanbaohe.game2048.component

/**
 * 2048 核心游戏引擎 —— 纯逻辑，不依赖 Android/Compose
 *
 * 所有棋盘操作均返回新的不可变数据，方便 StateFlow 驱动 Compose 重组。
 */
object GameEngine {

    private const val SIZE = Game2048UiState.GRID_SIZE

    /** 创建初始棋盘：空棋盘上随机放置 2 个数字 */
    fun newBoard(): List<List<Int>> {
        val grid = MutableList(SIZE) { MutableList(SIZE) { 0 } }
        addRandomTile(grid)
        addRandomTile(grid)
        return grid.map { it.toList() }
    }

    /**
     * 向指定方向移动棋盘
     *
     * @return Triple(新棋盘, 本次移动得分, 是否发生了有效移动)
     */
    fun move(grid: List<List<Int>>, direction: Direction): Triple<List<List<Int>>, Int, Boolean> {
        val mutable = grid.map { it.toMutableList() }.toMutableList()
        var score = 0
        var moved = false

        when (direction) {
            Direction.Left -> {
                for (r in 0 until SIZE) {
                    val (newRow, rowScore, rowMoved) = mergeRow(mutable[r])
                    mutable[r] = newRow.toMutableList()
                    score += rowScore
                    if (rowMoved) moved = true
                }
            }

            Direction.Right -> {
                for (r in 0 until SIZE) {
                    val reversed = mutable[r].reversed()
                    val (newRow, rowScore, rowMoved) = mergeRow(reversed)
                    mutable[r] = newRow.reversed().toMutableList()
                    score += rowScore
                    if (rowMoved) moved = true
                }
            }

            Direction.Up -> {
                for (c in 0 until SIZE) {
                    val col = (0 until SIZE).map { mutable[it][c] }
                    val (newCol, colScore, colMoved) = mergeRow(col)
                    for (r in 0 until SIZE) mutable[r][c] = newCol[r]
                    score += colScore
                    if (colMoved) moved = true
                }
            }

            Direction.Down -> {
                for (c in 0 until SIZE) {
                    val col = (0 until SIZE).map { mutable[it][c] }.reversed()
                    val (newCol, colScore, colMoved) = mergeRow(col)
                    val restored = newCol.reversed()
                    for (r in 0 until SIZE) mutable[r][c] = restored[r]
                    score += colScore
                    if (colMoved) moved = true
                }
            }
        }

        // 有效移动后在随机空位添加新数字
        if (moved) {
            addRandomTile(mutable)
        }

        return Triple(mutable.map { it.toList() }, score, moved)
    }

    /** 检测棋盘是否还有可合并/可移动的空间 */
    fun isGameOver(grid: List<List<Int>>): Boolean {
        // 有空格就不算结束
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                if (grid[r][c] == 0) return false
            }
        }
        // 检查相邻是否可合并
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                val v = grid[r][c]
                if (r + 1 < SIZE && grid[r + 1][c] == v) return false
                if (c + 1 < SIZE && grid[r][c + 1] == v) return false
            }
        }
        return true
    }

    /** 检测棋盘上是否出现 2048 */
    fun hasWon(grid: List<List<Int>>): Boolean {
        return grid.any { row -> row.any { it >= 2048 } }
    }

    // ─── 私有辅助方法 ──────────────────────────────────────────────────────────

    /**
     * 对一行执行左向合并
     *
     * 算法：
     * 1. 过滤掉 0（空格）
     * 2. 相邻相同的合并（每个格子每次移动只能参与一次合并）
     * 3. 右侧补 0 到 SIZE 长度
     *
     * @return Triple(合并后的行, 本行得分, 是否发生了变化)
     */
    private fun mergeRow(row: List<Int>): Triple<List<Int>, Int, Boolean> {
        val filtered = row.filter { it != 0 }.toMutableList()
        var score = 0
        val merged = mutableListOf<Int>()
        var i = 0
        while (i < filtered.size) {
            if (i + 1 < filtered.size && filtered[i] == filtered[i + 1]) {
                val sum = filtered[i] * 2
                merged.add(sum)
                score += sum
                i += 2 // 跳过已合并的一对
            } else {
                merged.add(filtered[i])
                i++
            }
        }
        // 补齐空位
        while (merged.size < SIZE) merged.add(0)
        val changed = merged != row
        return Triple(merged, score, changed)
    }

    /** 在棋盘空位上随机放置一个 2 (90%) 或 4 (10%) */
    private fun addRandomTile(grid: MutableList<MutableList<Int>>) {
        val empty = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                if (grid[r][c] == 0) empty.add(r to c)
            }
        }
        if (empty.isEmpty()) return
        val (r, c) = empty.random()
        grid[r][c] = if (Math.random() < 0.9) 2 else 4
    }
}

