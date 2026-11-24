package com.clghks86.dailybriefing.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Supabase `briefing_images` 한 행. sort_order 1=커버, 2=코인·금, 3=섹션. */
@Serializable
data class BriefingImageDto(
    val id: Long? = null,
    @SerialName("briefing_date") val briefingDate: String? = null,
    val section: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val caption: String? = null,
    @SerialName("sort_order") val sortOrder: Int? = null,
)
