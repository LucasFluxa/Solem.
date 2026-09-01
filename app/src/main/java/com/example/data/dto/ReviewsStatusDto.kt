package com.example.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReviewsStatusDto(
    @Json(name = "version") val version: Int? = null,
    @Json(name = "enabled") val enabled: Boolean? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null,
    @Json(name = "source") val source: String? = null
)
