package com.example

import com.example.data.dto.MallaCurricularModel
import com.example.data.dto.MallaRamoModel
import com.example.data.dto.MallaSemestreModel
import com.example.ui.malla.MallaProgressInfo
import org.junit.Assert.*
import org.junit.Test

class MallaCurricularTest {

    @Test
    fun testProgressCalculation() {
        val semestres = listOf(
            MallaSemestreModel(
                numeroSemestre = 1,
                romano = "I",
                totalSct = 30,
                ramos = listOf(
                    MallaRamoModel(sigla = "MAT021", nombre = "Matemática I", creditos = 5, prerequisitos = emptyList()),
                    MallaRamoModel(sigla = "FIS100", nombre = "Física General I", creditos = 6, prerequisitos = emptyList()),
                    MallaRamoModel(sigla = "IWI131", nombre = "Programación", creditos = 4, prerequisitos = emptyList())
                )
            ),
            MallaSemestreModel(
                numeroSemestre = 2,
                romano = "II",
                totalSct = 30,
                ramos = listOf(
                    MallaRamoModel(sigla = "MAT022", nombre = "Matemática II", creditos = 5, prerequisitos = listOf("MAT021")),
                    MallaRamoModel(sigla = "FIS110", nombre = "Física General II", creditos = 6, prerequisitos = listOf("FIS100", "MAT021"))
                )
            )
        )

        val totalRamos = semestres.sumOf { it.ramos.size } // 5
        val totalSct = semestres.sumOf { it.totalSct } // 60

        val aprobados = setOf("MAT021", "FIS100")
        val ramosAprobadosCount = semestres.flatMap { it.ramos }.count { aprobados.contains(it.sigla) } // 2
        val sctAprobados = semestres.flatMap { it.ramos }
            .filter { aprobados.contains(it.sigla) }
            .sumOf { it.creditos } // 11

        val porcentaje = if (totalRamos > 0) ((ramosAprobadosCount.toFloat() / totalRamos) * 100f).toInt() else 0

        val progressInfo = MallaProgressInfo(
            totalRamosCount = totalRamos,
            ramosAprobadosCount = ramosAprobadosCount,
            totalCreditos = totalSct,
            creditosAprobados = sctAprobados,
            porcentajeAvance = porcentaje
        )

        assertEquals(5, progressInfo.totalRamosCount)
        assertEquals(2, progressInfo.ramosAprobadosCount)
        assertEquals(11, progressInfo.creditosAprobados)
        assertEquals(40, progressInfo.porcentajeAvance)
    }

    @Test
    fun testPrerequisitesTreeResolution() {
        val r1 = MallaRamoModel(sigla = "MAT021", nombre = "Matemática I", creditos = 5, prerequisitos = emptyList())
        val r2 = MallaRamoModel(sigla = "MAT022", nombre = "Matemática II", creditos = 5, prerequisitos = listOf("MAT021"))
        val r3 = MallaRamoModel(sigla = "MAT023", nombre = "Matemática III", creditos = 5, prerequisitos = listOf("MAT022"))
        val allRamos = listOf(r1, r2, r3)

        // When MAT022 is selected:
        // Prereqs: MAT021
        val prereqs = r2.prerequisitos.toSet()
        assertTrue(prereqs.contains("MAT021"))

        // Habilita (Courses that require MAT022): MAT023
        val habilita = allRamos.filter { it.prerequisitos.contains("MAT022") }.map { it.sigla }.toSet()
        assertTrue(habilita.contains("MAT023"))
        assertFalse(habilita.contains("MAT021"))
    }
}
