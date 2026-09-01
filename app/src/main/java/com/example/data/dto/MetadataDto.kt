package com.example.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MetadataDto(
    @Json(name = "status") val status: String? = null,
    @Json(name = "version") val version: Int? = null,
    @Json(name = "generatedAt") val generatedAt: GeneratedAtDto? = null,
    @Json(name = "stats") val stats: MetadataStatsDto? = null,
    @Json(name = "system") val system: SystemInfoDto? = null,
    @Json(name = "files") val files: Map<String, FileMetadataDto>? = null
)

@JsonClass(generateAdapter = true)
data class GeneratedAtDto(
    @Json(name = "iso") val iso: String? = null,
    @Json(name = "unix") val unix: Long? = null
)

@JsonClass(generateAdapter = true)
data class MetadataStatsDto(
    @Json(name = "totalAsignaturas") val totalAsignaturas: Int? = null,
    @Json(name = "totalParalelos") val totalParalelos: Int? = null
)

@JsonClass(generateAdapter = true)
data class SystemInfoDto(
    @Json(name = "environment") val environment: String? = null,
    @Json(name = "executionTimeSeconds") val executionTimeSeconds: Double? = null,
    @Json(name = "scraperVersion") val scraperVersion: String? = null
)

@JsonClass(generateAdapter = true)
data class FileMetadataDto(
    @Json(name = "cambiosUltimaEjecucion") val cambiosUltimaEjecucion: Int? = null,
    @Json(name = "hash") val hash: String? = null,
    @Json(name = "updatedAt") val updatedAt: Double? = null
)
