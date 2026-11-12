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

package com.wanbaohe.markuplayers.domain.recipe

import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import kotlinx.coroutines.CancellationException

data class BatchProgress(
    val completed: Int,
    val total: Int,
    val currentName: String? = null
)

data class BatchFailure(
    val uri: String,
    val error: String
)

data class BatchReport(
    val successes: List<SaveResult.Success>,
    val failures: List<BatchFailure>,
    val total: Int = successes.size + failures.size,
    val cancelled: Boolean = false
) {
    val completed: Int get() = successes.size + failures.size
    val isComplete: Boolean get() = !cancelled && completed == total
}

/** Cancellation remains cooperative while exposing the results already committed to disk. */
class MarkupBatchCancelledException(
    val report: BatchReport,
    cause: CancellationException
) : CancellationException("Batch cancelled after ${report.completed} of ${report.total} images.") {
    init {
        initCause(cause)
    }
}
