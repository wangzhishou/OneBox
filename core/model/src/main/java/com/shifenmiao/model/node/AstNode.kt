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

package com.shifenmiao.model.node

/**
 * Generic AstNode implementation that can define any node in Abstract Syntax Tree.
 *
 * @param type A sealed class which is categorized into block and inline nodes.
 * @param links Pointers to parent, sibling, child nodes.
 */
class AstNode(
  val type: AstNodeType,
  val links: AstNodeLinks
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AstNode) return false

    if (type != other.type) return false
    if (links != other.links) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + links.hashCode()
    return result
  }
}
