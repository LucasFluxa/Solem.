package com.example.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

typealias ProfessorsRootDto = Map<String, ProfesorDto>

@JsonClass(generateAdapter = true)
data class ProfesorDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "comments") val comments: List<Any>? = null,
    @Json(name = "meta") val meta: ProfesorMetaDto? = null,
    @Json(name = "stats") val stats: Map<String, ProfesorMetricStatDto?>? = null
)

@JsonClass(generateAdapter = true)
data class ProfesorMetaDto(
    @Json(name = "effectiveCount") val effectiveCount: Double? = null,
    @Json(name = "isArchived") val isArchived: Boolean? = null,
    @Json(name = "lastUpdated") val lastUpdated: String? = null,
    @Json(name = "reviewCount") val reviewCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class ProfesorMetricStatDto(
    @Json(name = "avg") val avg: Double? = null,
    @Json(name = "distribution") val distribution: Map<String, Int>? = null,
    @Json(name = "is_bimodal") val isBimodal: Boolean? = null,
    @Json(name = "safe_score") val safeScore: Double? = null,
    @Json(name = "stdev") val stdev: Double? = null
)
