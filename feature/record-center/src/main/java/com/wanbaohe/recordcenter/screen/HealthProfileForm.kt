package com.wanbaohe.recordcenter.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.data.HealthProfile

/**
 * 基础信息表单:性别二选(可再点取消)+ 年龄/身高/体重(全部可选)+ 保存按钮。
 * "我的" tab 与编辑弹层共用;非法输入 toast 拦截,不保存。
 */
@Composable
fun HealthProfileForm(
    initial: HealthProfile,
    onSave: (HealthProfile) -> Unit,
    modifier: Modifier = Modifier,
) {
    var gender by remember(initial) { mutableStateOf(initial.gender) }
    var age by remember(initial) { mutableStateOf(initial.age?.toString().orEmpty()) }
    var height by remember(initial) { mutableStateOf(initial.heightCm?.let(::formatProfileFloat).orEmpty()) }
    var weight by remember(initial) { mutableStateOf(initial.weightKg?.let(::formatProfileFloat).orEmpty()) }
    val invalidText = stringResource(R.string.record_center_profile_invalid)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileFieldLabel(text = stringResource(R.string.record_center_profile_field_gender))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GenderOption(
                selected = gender == HealthProfile.Gender.MALE,
                icon = Icons.Filled.Male,
                label = stringResource(R.string.record_center_profile_gender_male),
                onClick = {
                    gender = if (gender == HealthProfile.Gender.MALE) {
                        HealthProfile.Gender.UNSET
                    } else HealthProfile.Gender.MALE
                },
                modifier = Modifier.weight(1f),
            )
            GenderOption(
                selected = gender == HealthProfile.Gender.FEMALE,
                icon = Icons.Filled.Female,
                label = stringResource(R.string.record_center_profile_gender_female),
                onClick = {
                    gender = if (gender == HealthProfile.Gender.FEMALE) {
                        HealthProfile.Gender.UNSET
                    } else HealthProfile.Gender.FEMALE
                },
                modifier = Modifier.weight(1f),
            )
        }
        ProfileFieldLabel(text = stringResource(R.string.record_center_profile_field_age))
        GlassOutlinedTextField(
            value = age,
            onValueChange = { age = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.record_center_profile_age_placeholder)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        ProfileFieldLabel(text = stringResource(R.string.record_center_profile_field_height))
        GlassOutlinedTextField(
            value = height,
            onValueChange = { height = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.record_center_profile_height_placeholder)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
        ProfileFieldLabel(text = stringResource(R.string.record_center_profile_field_weight))
        GlassOutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.record_center_profile_weight_placeholder)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
        GlassTonalButton(
            onClick = {
                val parsedAge = age.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                val parsedHeight = height.trim().takeIf { it.isNotEmpty() }?.toFloatOrNull()
                val parsedWeight = weight.trim().takeIf { it.isNotEmpty() }?.toFloatOrNull()
                val valid = (age.isBlank() || parsedAge in 1..150) &&
                    (height.isBlank() || (parsedHeight != null && parsedHeight in 50f..300f)) &&
                    (weight.isBlank() || (parsedWeight != null && parsedWeight in 2f..500f))
                if (!valid) {
                    AppToastHost.showToast(invalidText)
                    return@GlassTonalButton
                }
                onSave(
                    HealthProfile(
                        gender = gender,
                        age = parsedAge,
                        heightCm = parsedHeight,
                        weightKg = parsedWeight,
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.record_center_action_save))
        }
    }
}

@Composable
private fun ProfileFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/** 性别大选项块:选中态实心 primaryContainer,未选态玻璃 surface */
@Composable
private fun GenderOption(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .glassBackground(
                style = if (selected) GlassStyle.Dense else GlassStyle.Regular,
                shape = shape,
                color = containerColor,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}

/** 展示用浮点格式化:整数去小数点(175.0 → "175") */
private fun formatProfileFloat(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else value.toString()
