package com.example.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "reviews",
    indices = [
        Index(value = ["profesorName"]),
        Index(value = ["addedAt"])
    ]
)
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profesorName: String,
    val summary: String? = null,
    val activeTags: List<String> = emptyList(),
    // Métricas numéricas
    val accesibilidad: Int? = null,
    // Campo preservado para valores especiales ("mentor", etc.)
    val accesibilidadTag: String? = null,
    val claridadExpositiva: Int? = null,
    val coherenciaEvaluativa: Int? = null,
    val dificultadPercibida: Int? = null,
    val estabilidadEmocional: Int? = null,
    val gestionTiempo: Int? = null,
    val rigorCalificatorio: Int? = null,
    // Metadata
    val score: Int? = null,
    val reason: String? = null,
    val serverTime: String? = null,
    val addedAt: String? = null,
    val fingerprint: String? = null
)
