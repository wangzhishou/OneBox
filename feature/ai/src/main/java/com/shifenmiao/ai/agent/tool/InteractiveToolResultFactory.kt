package com.shifenmiao.ai.agent.tool

import com.google.gson.Gson

object InteractiveToolResultFactory {

    fun buildQuestionSubmittedResult(
        answersJson: String,
        gson: Gson
    ): AgentToolResult {
        return AgentToolResult(
            content = gson.toJson(
                mapOf(
                    "status" to "submitted",
                    "answers" to gson.fromJson(answersJson, Map::class.java).orEmpty()
                )
            ),
            isError = false
        )
    }

    fun buildQuestionCancelledResult(gson: Gson): AgentToolResult {
        return AgentToolResult(
            content = gson.toJson(
                mapOf(
                    "status" to "cancelled"
                )
            ),
            isError = false
        )
    }

    fun buildConfirmationRejectedResult(
        toolName: String,
        reason: String,
        gson: Gson
    ): AgentToolResult {
        return AgentToolResult(
            content = gson.toJson(
                mapOf(
                    "toolName" to toolName,
                    "decision" to "rejected",
                    "executed" to false,
                    "message" to "用户拒绝了本次工具执行",
                    "reason" to reason
                )
            ),
            isError = false
        )
    }

    fun isConfirmationApproved(payload: String?): Boolean {
        return payload?.contains("\"approved\"") == true
    }
}
