package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "paralelos",
    indices = [
        Index(value = ["sigla", "paralelo", "campus", "jornada", "periodo"], unique = true),
        Index(value = ["sigla"]),
        Index(value = ["campus"]),
        Index(value = ["periodo"])
    ]
)
data class ParaleloEntity(
    @PrimaryKey
    val id: String, // e.g. "Campus_Jornada_Periodo_Sigla_Paralelo"
    val sigla: String,
    val paralelo: String,
    val campus: String,
    val jornada: String,
    val periodo: String,
    val cupo: Int?,
    val departamento: String?
)
