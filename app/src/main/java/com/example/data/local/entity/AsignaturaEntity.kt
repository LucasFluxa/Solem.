package com.example.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "asignaturas",
    indices = [
        Index(value = ["sigla"], unique = true),
        Index(value = ["nombre"]),
        Index(value = ["departamento"])
    ]
)
data class AsignaturaEntity(
    @PrimaryKey
    val sigla: String,
    val nombre: String,
    val creditos: Int,
    val departamento: String,
    val programaUrl: String? = null,
    val horasTeoricas: Int = 0,
    val horasPracticas: Int = 0,
    val horasLaboratorios: Int = 0,
    val horasAyudantias: Int = 0,
    val requisitos: List<String> = emptyList(),
    val equivalencias: List<String> = emptyList(),
    val requisitoLicenciatura: Boolean = false
)
