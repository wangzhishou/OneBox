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
