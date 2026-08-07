package com.wanbaohe.dynamicui.state

import com.wanbaohe.dynamicui.action.DynamicUiInternalState
import com.wanbaohe.dynamicui.ir.ActionSpec
import com.wanbaohe.dynamicui.ir.UiNode
import com.wanbaohe.dynamicui.ir.propBool

/**
 * 收集动态 UI 树中的 prompt 信息（仅收集，不负责提交）。
 *
 * 约定：
 * - 每个节点可选 `prompt` 字段
 * - prompt 中可使用 `%s`（兼容 `s%`）占位实际值
 * - 实际值优先来源：binding -> id -> value/text/checked/selected 表达式
 */
object PromptCollector {

    data class PromptItem(
        val nodeType: String,
        val nodeId: String?,
        val template: String,
        val value: String,
        val rendered: String,
    )

    data class ValidationError(
        val nodeId: String,
        val message: String,
    )

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<ValidationError>,
    )

    fun collect(
        root: UiNode,
        scope: UiStateScope,
        includeEmptyValue: Boolean = false,
        itemContext: Map<String, Any?> = emptyMap(),
    ): List<PromptItem> {
        val result = mutableListOf<PromptItem>()
        traverse(root) { node ->
            val template = node.prompt?.trim().orEmpty()
            if (template.isEmpty()) return@traverse
            val value = resolveNodeValue(node, scope, itemContext)
            if (!includeEmptyValue && value.isBlank()) return@traverse
            val rendered = applyTemplate(template, value)
            result += PromptItem(
                nodeType = node.type,
                nodeId = node.id,
                template = template,
                value = value,
                rendered = rendered,
            )
        }
        return result
    }

    fun collectAsText(
        root: UiNode,
        scope: UiStateScope,
        separator: String = "\n",
        includeEmptyValue: Boolean = false,
        itemContext: Map<String, Any?> = emptyMap(),
    ): String = collect(
        root = root,
        scope = scope,
        includeEmptyValue = includeEmptyValue,
        itemContext = itemContext,
    )
        .map { it.rendered }
        .filter { it.isNotBlank() }
        .joinToString(separator)

    /**
     * 生成 submit 动作要发送给 LLM 的文本。
     *
     * 优先使用 `actions.onClick.params.prompt` 作为整次提交的显式提示；它可包含
     * `${state.xxx}` 表达式。未提供时才回退到递归收集节点上的 `prompt` 字段。
     */
    fun collectSubmitText(
        action: ActionSpec,
        root: UiNode,
        scope: UiStateScope,
        separator: String = "\n",
        includeEmptyValue: Boolean = false,
        itemContext: Map<String, Any?> = emptyMap(),
    ): String {
        action.params["prompt"]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { explicitPrompt ->
                return ValueExprResolver.resolveString(explicitPrompt, scope, itemContext)
            }

        return collectAsText(
            root = root,
            scope = scope,
            separator = separator,
            includeEmptyValue = includeEmptyValue,
            itemContext = itemContext,
        )
    }

    /**
     * 收集为 Map，key 使用 node.id（无 id 的节点会被忽略）。
     * 同 id 多次出现时以后者覆盖前者，便于做增量更新场景。
     */
    fun collectAsMap(
        root: UiNode,
        scope: UiStateScope,
        includeEmptyValue: Boolean = false,
        itemContext: Map<String, Any?> = emptyMap(),
    ): Map<String, String> = collect(
        root = root,
        scope = scope,
        includeEmptyValue = includeEmptyValue,
        itemContext = itemContext,
    ).mapNotNull { item ->
        val id = item.nodeId?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        id to item.rendered
    }.toMap()

    /**
     * 校验所有 `required=true` 的节点。
     *
     * - 文本输入类（TextField / OutlinedTextField）：值为空则失败
     * - Switch / Checkbox：checked 不为 true 则失败（如"同意协议"场景）
     * - RowSelector / ColumnSelector / Chip：无选中项则失败
     *
     * 校验结果会写入 [UiStateScope]：
     * - 失败节点：`_dyn.errors.{nodeId}` = requiredMessage
     * - 通过节点：`_dyn.errors.{nodeId}` = null（清除之前的错误）
     * - 聚合结果：`_dyn.validationErrors` = Map<nodeId, message>
     *
     * @return [ValidationResult] — isValid=true 时可安全提交
     */
    fun validate(
        root: UiNode,
        scope: UiStateScope,
        itemContext: Map<String, Any?> = emptyMap(),
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val errorMap = mutableMapOf<String, String>()

        traverse(root) { node ->
            val required = node.propBool("required", false)
            if (!required) return@traverse

            val nodeId = node.id ?: return@traverse
            val requiredMessage = node.props["requiredMessage"]?.toString()?.ifBlank { null }
                ?: "此项为必填"

            val isValuePresent = checkRequiredValue(node, scope, itemContext)

            if (!isValuePresent) {
                errors += ValidationError(nodeId = nodeId, message = requiredMessage)
                errorMap[nodeId] = requiredMessage
                scope.setByPath(DynamicUiInternalState.validationErrorKey(nodeId), requiredMessage)
            } else {
                scope.setByPath(DynamicUiInternalState.validationErrorKey(nodeId), null)
            }
        }

        scope.setByPath(DynamicUiInternalState.VALIDATION_ERRORS, errorMap)

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
        )
    }

    /**
     * 清除所有校验错误状态（可用于重置表单时调用）。
     */
    fun clearValidationErrors(
        root: UiNode,
        scope: UiStateScope,
    ) {
        traverse(root) { node ->
            val nodeId = node.id ?: return@traverse
            scope.setByPath(DynamicUiInternalState.validationErrorKey(nodeId), null)
        }
        scope.setByPath(DynamicUiInternalState.VALIDATION_ERRORS, null)
    }

    private fun checkRequiredValue(
        node: UiNode,
        scope: UiStateScope,
        itemContext: Map<String, Any?>,
    ): Boolean {
        val type = node.type
        return when {
            type in setOf("Switch", "Checkbox") -> {
                val checked = node.props["checked"]
                if (checked != null) {
                    val resolved = rememberResolvedBoolStatic(checked, scope, itemContext)
                    resolved
                } else {
                    val bindingPath = parseBindingPath(node.props["binding"]?.toString())
                    if (bindingPath != null) {
                        scope.getByPath(bindingPath) == true
                    } else {
                        node.id?.let { scope.getByPath(it) == true } ?: false
                    }
                }
            }

            type in setOf("Chip", "FilterChip", "RowSelector", "ColumnSelector") -> {
                val bindingPath = parseBindingPath(node.props["binding"]?.toString())
                if (bindingPath != null) {
                    val value = scope.getByPath(bindingPath)
                    !value.toString().isBlank()
                } else {
                    node.id?.let { id ->
                        val value = scope.getByPath(id)
                        !value.toString().isBlank()
                    } ?: false
                }
            }

            else -> {
                val value = resolveNodeValue(node, scope, itemContext)
                value.isNotBlank()
            }
        }
    }

    private fun rememberResolvedBoolStatic(
        raw: Any?,
        scope: UiStateScope,
        itemContext: Map<String, Any?>,
    ): Boolean {
        return when (raw) {
            is Boolean -> raw
            is String -> {
                val resolved = ValueExprResolver.resolveString(raw, scope, itemContext)
                resolved.toBooleanStrictOrNull() ?: resolved.isNotBlank()
            }
            else -> raw.toString().toBooleanStrictOrNull() ?: false
        }
    }

    private fun traverse(node: UiNode, visitor: (UiNode) -> Unit) {
        visitor(node)
        node.children.forEach { child -> traverse(child, visitor) }
        node.props.values.forEach { value ->
            when (value) {
                is UiNode -> traverse(value, visitor)
                is List<*> -> value.filterIsInstance<UiNode>().forEach { child -> traverse(child, visitor) }
            }
        }
    }

    private fun resolveNodeValue(
        node: UiNode,
        scope: UiStateScope,
        itemContext: Map<String, Any?>,
    ): String {
        parseBindingPath(node.props["binding"]?.toString())?.let { path ->
            return scope.getByPath(path)?.toString().orEmpty()
        }
        node.id?.let { id ->
            val byId = scope.getByPath(id)?.toString().orEmpty()
            if (byId.isNotEmpty()) return byId
        }

        val candidateKeys = listOf("value", "text", "checked", "selected")
        for (key in candidateKeys) {
            val raw = node.props[key] ?: continue
            val resolved = when (raw) {
                is String -> ValueExprResolver.resolveString(raw, scope, itemContext)
                else -> raw.toString()
            }
            if (resolved.isNotEmpty()) return resolved
        }
        return ""
    }

    private fun parseBindingPath(binding: String?): String? {
        val raw = binding?.trim().orEmpty()
        if (raw.isEmpty()) return null
        return when {
            raw.startsWith("\${state.") && raw.endsWith("}") ->
                raw.removePrefix("\${state.").removeSuffix("}")
            raw.startsWith("state.") -> raw.removePrefix("state.")
            else -> null
        }
    }

    private fun applyTemplate(template: String, value: String): String {
        return when {
            template.contains("%s") -> template.replace("%s", value)
            template.contains("s%") -> template.replace("s%", value)
            else -> template
        }
    }
}
