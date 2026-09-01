package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "carreras_planes",
    indices = [
        Index(value = ["codigoCarrera", "jornada"]),
        Index(value = ["codigoCarrera", "tipoMalla"])
    ]
)
data class CarreraPlanEntity(
    @PrimaryKey
    val id: String, // e.g. "13-1_Diurna_NUEVA" o "13-1_Diurna_ANTIGUA"
    val codigoCarrera: String,
    val nombre: String,
    val jornada: String = "Diurna",
    val tipoMalla: String = "NUEVA", // "NUEVA" o "ANTIGUA"
    val nombreMalla: String = "Malla Nueva",
    val totalMenciones: Int = 1,
    val siglas: List<String> = emptyList(),
    val dataJson: String = ""
)
