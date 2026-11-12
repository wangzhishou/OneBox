/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.sudoku.logic

import kotlin.random.Random

/**
 * 数独难度。给定数越多越简单: 简单 40 / 中等 34 / 困难 30。
 * 棋盘上的 "N 题" 展示的就是这个给定数。
 */
enum class SudokuDifficulty(val givens: Int) {
    EASY(40),
    MEDIUM(34),
    HARD(30)
}

/**
 * 数独引擎, 全部纯函数无 IO。
 *
 * 棋盘统一用长度 81 的 IntArray 表示, 0 = 空格, 1..9 = 数字;
 * index = row * 9 + col。持久化时转成 81 字符的字符串('0'..'9')。
 */
object SudokuEngine {

    const val BOARD_CELLS = 81

    /** 生成一盘终盘(回溯 + 每格候选数字随机洗牌)。 */
    fun generateSolvedBoard(random: Random = Random.Default): IntArray {
        val board = IntArray(BOARD_CELLS)
        fillCell(board, 0, random)
        return board
    }

    private fun fillCell(board: IntArray, index: Int, random: Random): Boolean {
        if (index == BOARD_CELLS) return true
        val digits = (1..9).shuffled(random)
        for (digit in digits) {
            if (canPlace(board, index, digit)) {
                board[index] = digit
                if (fillCell(board, index + 1, random)) return true
                board[index] = 0
            }
        }
        return false
    }

    /** (index, digit) 是否与同行/同列/同宫已有数字冲突。 */
    fun canPlace(board: IntArray, index: Int, digit: Int): Boolean {
        val row = index / 9
        val col = index % 9
        for (i in 0 until 9) {
            if (board[row * 9 + i] == digit) return false
            if (board[i * 9 + col] == digit) return false
        }
        val boxRow = row / 3 * 3
        val boxCol = col / 3 * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (board[r * 9 + c] == digit) return false
            }
        }
        return true
    }

    /** 数解个数, 数到 [limit] 就停(只关心"是否唯一解", 不需要精确总数)。 */
    fun countSolutions(board: IntArray, limit: Int = 2): Int {
        val work = board.copyOf()
        return countSolutionsInner(work, 0, limit)
    }

    private fun countSolutionsInner(board: IntArray, index: Int, limit: Int): Int {
        if (index == BOARD_CELLS) return 1
        if (board[index] != 0) return countSolutionsInner(board, index + 1, limit)
        var count = 0
        for (digit in 1..9) {
            if (canPlace(board, index, digit)) {
                board[index] = digit
                count += countSolutionsInner(board, index + 1, limit - count)
                board[index] = 0
                if (count >= limit) return count
            }
        }
        return count
    }

    /**
     * 出题: 先生成终盘, 再按 180° 旋转对称成对挖洞, 每次挖完用解计数保证唯一解。
     * 挖到目标给定数为止; 对称 + 唯一解约束下单盘偶尔挖不到位, 就整盘重抽,
     * 多次尝试取给定数最少的一盘(通常几次内就能精确命中目标)。
     *
     * @return first = 题目(含空格), second = 终盘解
     */
    fun generatePuzzle(
        difficulty: SudokuDifficulty,
        random: Random = Random.Default
    ): Pair<IntArray, IntArray> {
        var best: Pair<IntArray, IntArray>? = null
        var bestGivens = BOARD_CELLS + 1
        repeat(MAX_GENERATE_ATTEMPTS) {
            val candidate = generatePuzzleOnce(difficulty, random)
            val givens = candidate.first.count { it != 0 }
            if (givens < bestGivens) {
                best = candidate
                bestGivens = givens
            }
            if (givens == difficulty.givens) return candidate
        }
        return best!!
    }

    private const val MAX_GENERATE_ATTEMPTS = 30

    /** 单盘出题尝试: 对称挖洞; 挖不动时回填几对换条路径再挖, 跳出局部最优。 */
    private fun generatePuzzleOnce(
        difficulty: SudokuDifficulty,
        random: Random
    ): Pair<IntArray, IntArray> {
        val solution = generateSolvedBoard(random)
        val puzzle = solution.copyOf()

        // 81 格按 180° 旋转对称配成 41 组(40 对 + 正中心自成一组)
        val pairs = (0 until BOARD_CELLS / 2).map { it to BOARD_CELLS - 1 - it }
            .plusElement(BOARD_CELLS / 2 to BOARD_CELLS / 2)

        var givens = BOARD_CELLS
        val removedPairs = mutableListOf<Pair<Int, Int>>()
        var best = puzzle.copyOf()
        var bestGivens = givens
        var round = 0

        while (givens > difficulty.givens && round < MAX_DIG_ROUNDS) {
            round++
            var progress = false
            for ((a, b) in pairs.shuffled(random)) {
                if (givens <= difficulty.givens) break
                // a == b 是中心格, 只算 1 个; 其余对两个格子同挖同填
                val filledA = puzzle[a] != 0
                val filledB = a != b && puzzle[b] != 0
                if (!filledA && !filledB) continue
                val removed = (if (filledA) 1 else 0) + (if (filledB) 1 else 0)
                if (givens - removed < difficulty.givens) continue
                val backupA = puzzle[a]
                val backupB = puzzle[b]
                puzzle[a] = 0
                puzzle[b] = 0
                if (countSolutions(puzzle) == 1) {
                    givens -= removed
                    removedPairs += a to b
                    progress = true
                    if (givens < bestGivens) {
                        best = puzzle.copyOf()
                        bestGivens = givens
                    }
                } else {
                    puzzle[a] = backupA
                    puzzle[b] = backupB
                }
            }
            if (!progress) {
                // 挖到局部最优也到不了目标: 回填几对已挖的洞, 洗牌后换条路径再挖
                if (removedPairs.isEmpty()) break
                repeat(minOf(removedPairs.size, RESTORE_ON_STALL)) {
                    val index = random.nextInt(removedPairs.size)
                    val (a, b) = removedPairs.removeAt(index)
                    puzzle[a] = solution[a]
                    puzzle[b] = solution[b]
                    givens += if (a == b) 1 else 2
                }
            }
        }
        return best to solution
    }

    private const val MAX_DIG_ROUNDS = 40
    private const val RESTORE_ON_STALL = 3

    /** 完成判定: 81 格全部与解一致。 */
    fun isBoardComplete(entries: IntArray, solution: IntArray): Boolean {
        for (i in 0 until BOARD_CELLS) {
            if (entries[i] != solution[i]) return false
        }
        return true
    }

    /**
     * 提示选格: 选中格空着或填错就填它; 否则随机挑一个未填对的格子。
     * 全部已填对返回 -1。
     */
    fun findHintCell(
        puzzle: IntArray,
        entries: IntArray,
        solution: IntArray,
        selectedIndex: Int?,
        random: Random = Random.Default
    ): Int {
        val selected = selectedIndex
        if (selected != null && selected in 0 until BOARD_CELLS &&
            puzzle[selected] == 0 && entries[selected] != solution[selected]
        ) {
            return selected
        }
        val candidates = (0 until BOARD_CELLS).filter { i ->
            puzzle[i] == 0 && entries[i] != solution[i]
        }
        if (candidates.isEmpty()) return -1
        return candidates[random.nextInt(candidates.size)]
    }

    /** 盘上某个数字已出现的次数(含给定), 满 9 个时键盘置灰。 */
    fun countDigit(entries: IntArray, digit: Int): Int = entries.count { it == digit }

    /** 用户已填格数(不算给定), 继续游戏卡片的进度分子。 */
    fun countUserFilled(puzzle: IntArray, entries: IntArray): Int {
        var count = 0
        for (i in 0 until BOARD_CELLS) {
            if (puzzle[i] == 0 && entries[i] != 0) count++
        }
        return count
    }

    // ─── 81 字符快照编解码 ──────────────────────────────────────────────────

    fun encodeBoard(board: IntArray): String {
        val chars = CharArray(BOARD_CELLS)
        for (i in 0 until BOARD_CELLS) chars[i] = '0' + board[i]
        return String(chars)
    }

    /** 解码失败(长度不对/含非数字)返回 null, 脏快照不致命。 */
    fun decodeBoard(encoded: String?): IntArray? {
        if (encoded == null || encoded.length != BOARD_CELLS) return null
        val board = IntArray(BOARD_CELLS)
        for (i in 0 until BOARD_CELLS) {
            val digit = encoded[i] - '0'
            if (digit !in 0..9) return null
            board[i] = digit
        }
        return board
    }
}
