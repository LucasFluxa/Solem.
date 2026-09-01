package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profesores",
    indices = [
        Index(value = ["name"]),
        Index(value = ["departamento"]),
        Index(value = ["sedes"])
    ]
)
data class ProfesorEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val departamento: String? = null,
    val sedes: String? = null,
    val ramosImpartidos: String? = null,
    val totalBloques: Int = 0,
    val aliases: String? = null,
    val reviewCount: Int = 0,
    val isArchived: Boolean = false,
    val lastUpdated: String? = null,
    // Estadísticas clave precalculadas para búsquedas y ordenamientos rápidos
    val accesibilidadAvg: Double? = null,
    val claridadAvg: Double? = null,
    val coherenciaAvg: Double? = null,
    val dificultadAvg: Double? = null,
    val estabilidadAvg: Double? = null,
    val gestionTiempoAvg: Double? = null,
    val rigorAvg: Double? = null,
    val safeScorePromedio: Double? = null,
    // JSON completo de stats para vista detallada si se requiere
    val statsRawJson: String? = null
)
