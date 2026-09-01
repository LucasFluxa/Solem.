package com.example.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

typealias HorariosRootDto = Map<String, Map<String, Map<String, Map<String, Map<String, ParaleloDetailDto>>>>>

@JsonClass(generateAdapter = true)
data class ParaleloDetailDto(
    @Json(name = "cupo") val cupo: Int? = null,
    @Json(name = "departamento") val departamento: String? = null,
    @Json(name = "horario") val horario: List<BloqueHorarioDto>? = null,
    @Json(name = "profesor") val profesor: List<String>? = null,
    @Json(name = "nombre") val nombre: String? = null,
    @Json(name = "sigla") val sigla: String? = null,
    @Json(name = "paralelo") val paralelo: String? = null
)

@JsonClass(generateAdapter = true)
data class BloqueHorarioDto(
    @Json(name = "bloque") val bloque: Int? = null,
    @Json(name = "campus") val campus: String? = null,
    @Json(name = "dia") val dia: Int? = null,
    @Json(name = "profesor") val profesor: String? = null,
    @Json(name = "sala") val sala: String? = null,
    @Json(name = "tipo") val tipo: String? = null
)
