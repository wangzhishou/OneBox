package com.shifenmiao.base.ui.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SingleColumnPicker(
    visible: Boolean,
    title: String? = null,
    range: List<String>,
    value: Int,
    onChange: (Int) -> Unit,
    onCancel: () -> Unit
) {
    Picker(
        visible,
        arrayOf(range),
        arrayOf(value),
        title,
        onCancel
    ) {
        onChange(it.first())
    }
}

@Stable
interface SingleColumnPickerState {
    val visible: Boolean

    fun show(
        title: String? = null,
        range: List<String>,
        value: Int,
        onChange: (Int) -> Unit
    )

    fun hide()
}

@Composable
fun rememberSingleColumnPickerState(): SingleColumnPickerState {
    val state = remember { SingleColumnPickerStateImpl() }

    state.props?.let { props ->
        SingleColumnPicker(
            visible = state.visible,
            title = props.title,
            range = props.range,
            value = props.value,
            onChange = props.onChange,
            onCancel = { state.hide() }
        )
    }

    return state
}

private class SingleColumnPickerStateImpl : SingleColumnPickerState {
    override var visible by mutableStateOf(false)
    var props by mutableStateOf<SingleColumnPickerProps?>(null)
        private set

    override fun show(
        title: String?,
        range: List<String>,
        value: Int,
        onChange: (Int) -> Unit
    ) {
        props = SingleColumnPickerProps(title, range, value, onChange)
        visible = true
    }

    override fun hide() {
        visible = false
    }
}

private data class SingleColumnPickerProps(
    val title: String? = null,
    val range: List<String>,
    val value: Int,
    val onChange: (Int) -> Unit
)