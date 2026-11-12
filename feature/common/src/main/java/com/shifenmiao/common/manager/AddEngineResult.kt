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

package com.shifenmiao.common.manager

/**
 * 新增自定义引擎的结果。
 *
 * 之所以不直接用 Boolean: 失败原因决定用户能不能自己解决 ——
 * "名称重复"要提示换名字, "写库异常"才该说稍后重试。混成一句
 * "添加失败, 请稍后重试"会让用户在一个永远重试不成功的操作上循环。
 */
sealed interface AddEngineResult {

    data object Success : AddEngineResult

    /** 服务标识已被内置预设或另一条自定义引擎占用, 需要换个名字 */
    data object NameTaken : AddEngineResult

    /** 写库成功但回读不到"用户自有"的行, 已回滚 —— 理论上不该出现, 出现即是 bug */
    data object NotVisible : AddEngineResult

    /** 其他异常(数据库、参数等) */
    data object Failed : AddEngineResult
}
