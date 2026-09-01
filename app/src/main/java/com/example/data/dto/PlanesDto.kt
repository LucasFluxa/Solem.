package com.example.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CarreraDto(
    @Json(name = "código") val codigoConAcento: String? = null,
    @Json(name = "codigo") val codigoSinAcento: String? = null,
    @Json(name = "nombre") val nombreCarrera: String? = null,
    @Json(name = "sede") val sede: String? = null,
    @Json(name = "jornada") val jornada: String? = null,
    @Json(name = "menciones/especialidades") val mencionesConSlash: Map<String, MencionDto>? = null,
    @Json(name = "menciones") val mencionesSinSlash: Map<String, MencionDto>? = null,
    @Json(name = "especialidades") val especialidades: Map<String, MencionDto>? = null
) {
    val codigo: String?
        get() = codigoConAcento ?: codigoSinAcento

    val menciones: Map<String, MencionDto>?
        get() = mencionesConSlash ?: mencionesSinSlash ?: especialidades
}

@JsonClass(generateAdapter = true)
data class MencionDto(
    @Json(name = "nombre") val nombre: String? = null,
    @Json(name = "planes") val planes: Map<String, PlanEstudioDto>? = null
)

@JsonClass(generateAdapter = true)
data class PlanEstudioDto(
    @Json(name = "plan") val plan: String? = null,
    @Json(name = "malla") val malla: List<Map<String, RamoMallaDto>>? = null
)

@JsonClass(generateAdapter = true)
data class RequisitoItemDto(
    @Json(name = "sigla") val sigla: String? = null,
    @Json(name = "tipo") val tipo: String? = null // "PRE" | "CO"
)

@JsonClass(generateAdapter = true)
data class EquivalenciaDto(
    @Json(name = "sigla") val sigla: String? = null
)

@JsonClass(generateAdapter = true)
data class RamoMallaDto(
    @Json(name = "nombre") val nombre: String? = null,
    @Json(name = "creditos") val creditosRaw: Any? = null,
    @Json(name = "departamento") val departamento: String? = null,
    @Json(name = "requisito_licenciatura") val requisitoLicenciatura: Boolean? = null,
    @Json(name = "requisitos") val requisitos: List<List<RequisitoItemDto>>? = null,
    @Json(name = "horas") val horas: HorasDto? = null,
    @Json(name = "equivalencias") val equivalencias: List<List<EquivalenciaDto>>? = null
) {
    val creditos: Int
        get() = when (creditosRaw) {
            is Number -> creditosRaw.toInt()
            is String -> creditosRaw.toIntOrNull() ?: 0
            else -> 0
        }

    // Devuelve los grupos de requisitos DNF como expresiones legibles (ej: "FIS110 y MAT022")
    val requisitosFormatted: List<String>
        get() {
            val list = requisitos ?: return emptyList()
            return list.mapNotNull { grupo ->
                val siglas = grupo.mapNotNull { it.sigla?.trim() }.filter { it.isNotBlank() }
                if (siglas.isEmpty()) null
                else siglas.joinToString(" y ")
            }
        }

    // Devuelve todas las siglas individuales de requisitos
    val requisitosSiglas: List<String>
        get() {
            val list = requisitos ?: return emptyList()
            return list.flatMap { grupo ->
                grupo.mapNotNull { it.sigla?.trim() }.filter { it.isNotBlank() }
            }.distinct()
        }

    val prerequisitosList: List<String>
        get() {
            val list = requisitos ?: return emptyList()
            return list.flatMap { grupo ->
                grupo.filter { it.tipo == null || it.tipo.equals("PRE", ignoreCase = true) }
                    .mapNotNull { it.sigla?.trim() }
                    .filter { it.isNotBlank() }
            }.distinct()
        }

    val correquisitosList: List<String>
        get() {
            val list = requisitos ?: return emptyList()
            return list.flatMap { grupo ->
                grupo.filter { it.tipo?.equals("CO", ignoreCase = true) == true }
                    .mapNotNull { it.sigla?.trim() }
                    .filter { it.isNotBlank() }
            }.distinct()
        }

    // Devuelve todas las siglas de equivalencias
    val equivalenciasSiglas: List<String>
        get() {
            val list = equivalencias ?: return emptyList()
            return list.flatMap { grupo ->
                grupo.mapNotNull { it.sigla?.trim() }.filter { it.isNotBlank() }
            }.distinct()
        }
}

@JsonClass(generateAdapter = true)
data class HorasDto(
    @Json(name = "teoricas") val teoricasRaw: Any? = null,
    @Json(name = "practicas") val practicasRaw: Any? = null,
    @Json(name = "laboratorios") val laboratoriosRaw: Any? = null,
    @Json(name = "ayudantias") val ayudantiasRaw: Any? = null
) {
    val teoricas: Int get() = (teoricasRaw as? Number)?.toInt() ?: (teoricasRaw as? String)?.toIntOrNull() ?: 0
    val practicas: Int get() = (practicasRaw as? Number)?.toInt() ?: (practicasRaw as? String)?.toIntOrNull() ?: 0
    val laboratorios: Int get() = (laboratoriosRaw as? Number)?.toInt() ?: (laboratoriosRaw as? String)?.toIntOrNull() ?: 0
    val ayudantias: Int get() = (ayudantiasRaw as? Number)?.toInt() ?: (ayudantiasRaw as? String)?.toIntOrNull() ?: 0
}

