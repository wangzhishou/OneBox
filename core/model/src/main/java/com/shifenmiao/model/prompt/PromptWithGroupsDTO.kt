package com.shifenmiao.model.prompt

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.Date
import kotlinx.serialization.Contextual

/**
 * DTO stands for Data Transfer Object.
 */
@Parcelize
@Serializable
data class PromptWithGroupsDTO(
    val prompt: PromptDTO,
    val groups: List<GroupDTO>
) : Parcelable

@Parcelize
@Serializable
data class PromptDTO(
    val id: Int,
    val name: String,
    val emoji: String,
    val prompt: String,
    val description: String,
    val templates: String = "",
    val placeholder: String = "",
    val updateTime: @Contextual Date,
    val canEdit: Boolean
) : Parcelable

@Parcelize
@Serializable
data class GroupDTO(
    val groupId: Int,
    val name: String
) : Parcelable