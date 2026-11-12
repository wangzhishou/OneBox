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

import androidx.compose.runtime.Immutable

/**
 * All the pointers that can exist for a node in an AST.
 *
 * Links are mutable to make it possible to instantiate a Node which can then reconfigure its
 * children and siblings. Please do not modify the links after an ASTNode is created and the scope
 * is finished.
 */
@Immutable
class AstNodeLinks(
  var parent: AstNode? = null,
  var firstChild: AstNode? = null,
  var lastChild: AstNode? = null,
  var previous: AstNode? = null,
  var next: AstNode? = null
) {

  override fun equals(other: Any?): Boolean {
    if (other !is AstNodeLinks) return false

    return parent === other.parent &&
        firstChild === other.firstChild &&
        lastChild === other.lastChild &&
        previous === other.previous &&
        next === other.next
  }

  /**
   * Stop infinite loop and only calculate towards bottom-right direction
   */
  override fun hashCode(): Int {
    return (firstChild ?: 0).hashCode() * 11 + (next ?: 0).hashCode() * 7
  }
}