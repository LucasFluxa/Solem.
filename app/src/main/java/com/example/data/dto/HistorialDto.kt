package com.example.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HistorialCambiosDto(
    @Json(name = "metadata") val metadata: HistorialMetadataDto? = null,
    @Json(name = "eventos") val eventos: List<EventoHistorialDto>? = null
)

@JsonClass(generateAdapter = true)
data class HistorialMetadataDto(
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "fecha") val fecha: String? = null,
    @Json(name = "hora") val hora: String? = null,
    @Json(name = "total_eventos") val totalEventos: Int? = null
)

@JsonClass(generateAdapter = true)
data class EventoHistorialDto(
    @Json(name = "tipo") val tipo: String? = null,
    @Json(name = "entidad") val entidad: String? = null,
    @Json(name = "asignatura") val asignatura: String? = null,
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "ruta") val ruta: RutaEventoDto? = null,
    @Json(name = "detalle") val detalle: Map<String, Any?>? = null
)

@JsonClass(generateAdapter = true)
data class RutaEventoDto(
    @Json(name = "sede") val sede: String? = null,
    @Json(name = "jornada") val jornada: String? = null,
    @Json(name = "periodo") val periodo: String? = null,
    @Json(name = "sigla") val sigla: String? = null,
    @Json(name = "paralelo") val paralelo: String? = null
)
