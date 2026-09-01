package com.example.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

typealias ProgramasAcademicosRootDto = Map<String, Map<String, Map<String, Map<String, AsignaturaProgramaDto>>>>

@JsonClass(generateAdapter = true)
data class AsignaturaProgramaDto(
    @Json(name = "nombre") val nombre: String? = null,
    @Json(name = "creditos") val creditosRaw: Any? = null,
    @Json(name = "programa") val programaUrl: String? = null
) {
    val creditos: Int
        get() = when (creditosRaw) {
            is Number -> creditosRaw.toInt()
            is String -> creditosRaw.toIntOrNull() ?: 0
            else -> 0
        }
}
