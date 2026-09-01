package com.example.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "bloques_horario",
    indices = [
        Index(value = ["paraleloId"]),
        Index(value = ["profesor"]),
        Index(value = ["dia", "bloque"]),
        Index(value = ["campus", "periodo"]),
        Index(value = ["sigla", "periodo"])
    ]
)
data class BloqueHorarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val paraleloId: String,
    val sigla: String,
    val paralelo: String,
    val dia: Int, // 0: Lunes, 1: Martes, 2: Miércoles, 3: Jueves, 4: Viernes, 5: Sábado
    val bloque: Int,
    val profesor: String?,
    val sala: String?,
    val tipo: String?, // Cátedra, Lab, Ayudantía, etc.
    val campus: String?,
    val periodo: String = "2026-2"
)

