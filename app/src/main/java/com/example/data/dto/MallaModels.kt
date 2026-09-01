package com.example.data.dto

import androidx.compose.runtime.Immutable
import com.squareup.moshi.JsonClass

enum class RamoEstado {
    APROBADO,   // Aprobado por el estudiante (Verde / Mint)
    CURSANDO,   // Cursando actualmente (Cyan / Azul)
    HABILITADO, // Prerrequisitos cumplidos, listo para inscribir (Borde brillante)
    BLOQUEADO,  // Prerrequisitos pendientes (Oscuro / Bloqueado)
    PENDIENTE   // Sin prerrequisitos pendientes, no cursado
}

@Immutable
@JsonClass(generateAdapter = true)
data class MallaRamoModel(
    val sigla: String,
    val nombre: String,
    val creditos: Int,
    val departamento: String? = null,
    val prerequisitos: List<String> = emptyList(),
    val correquisitos: List<String> = emptyList(),
    val semestre: Int = 1
)

@Immutable
@JsonClass(generateAdapter = true)
data class MallaSemestreModel(
    val numeroSemestre: Int,
    val romano: String, // "I", "II", "III", ...
    val totalSct: Int,
    val ramos: List<MallaRamoModel>
)

@Immutable
@JsonClass(generateAdapter = true)
data class MallaCurricularModel(
    val carreraCodigo: String,
    val carreraNombre: String,
    val planLabel: String,
    val tipoMalla: String,
    val totalCreditos: Int,
    val semestres: List<MallaSemestreModel>
)
