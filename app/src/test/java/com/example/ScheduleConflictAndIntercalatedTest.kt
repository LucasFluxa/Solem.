package com.example

import com.example.ui.horario.EnrolledClassBlock
import com.example.ui.horario.HorarioTope
import org.junit.Assert.*
import org.junit.Test

class ScheduleConflictAndIntercalatedTest {

    @Test
    fun testClashDetectionForSameDayAndBlock() {
        val enrolled = listOf(
            EnrolledClassBlock(
                sigla = "FIS100",
                asignaturaNombre = "Física General I",
                paralelo = "1",
                dia = 0,
                bloque = 1,
                sala = "C-201",
                profesor = "Profesor A",
                campus = "Casa Central Valparaíso",
                tipo = "Cátedra"
            ),
            EnrolledClassBlock(
                sigla = "MAT021",
                asignaturaNombre = "Matemática I",
                paralelo = "2",
                dia = 0,
                bloque = 1,
                sala = "C-202",
                profesor = "Profesor B",
                campus = "Casa Central Valparaíso",
                tipo = "Cátedra"
            )
        )

        val grouped = enrolled.groupBy { "${it.dia}_${it.bloque}" }
        val topes = grouped.filter { entry ->
            val distinctSiglas = entry.value.map { it.sigla.trim().uppercase() }.distinct()
            distinctSiglas.size > 1
        }.map { entry ->
            val first = entry.value.first()
            HorarioTope(
                dia = first.dia,
                bloque = first.bloque,
                clases = entry.value
            )
        }

        assertEquals(1, topes.size)
        assertEquals(0, topes[0].dia)
        assertEquals(1, topes[0].bloque)
        assertEquals(2, topes[0].clases.size)
    }

    @Test
    fun testIntercalatedClassesAreNotFlaggedAsErrors() {
        val enrolled = listOf(
            EnrolledClassBlock(
                sigla = "FIS100",
                asignaturaNombre = "Lab Física I",
                paralelo = "1",
                dia = 1,
                bloque = 3,
                sala = "Lab-1",
                profesor = "Docente A",
                campus = "Casa Central Valparaíso",
                tipo = "Lab",
                semanaIntercalada = "A"
            ),
            EnrolledClassBlock(
                sigla = "QUI100",
                asignaturaNombre = "Lab Química General",
                paralelo = "1",
                dia = 1,
                bloque = 3,
                sala = "Lab-Química",
                profesor = "Docente B",
                campus = "Casa Central Valparaíso",
                tipo = "Lab",
                semanaIntercalada = "B"
            )
        )

        val grouped = enrolled.groupBy { "${it.dia}_${it.bloque}" }
        val topes = grouped.filter { entry ->
            val distinctSiglas = entry.value.map { it.sigla.trim().uppercase() }.distinct()
            if (distinctSiglas.size <= 1) return@filter false

            val classesInBlock = entry.value
            val isIntercalatedPair = classesInBlock.all { it.semanaIntercalada != null } &&
                    classesInBlock.map { it.semanaIntercalada }.distinct().size > 1

            !isIntercalatedPair
        }

        assertTrue("Intercalated classes (Semana A and Semana B) must not be reported as schedule clashes", topes.isEmpty())
    }

    @Test
    fun testIgnoredClashesAreFilteredOut() {
        val enrolled = listOf(
            EnrolledClassBlock(
                sigla = "INF130",
                asignaturaNombre = "Estructuras de Datos",
                paralelo = "1",
                dia = 2,
                bloque = 5,
                sala = "F-101",
                profesor = "Docente",
                campus = "Casa Central Valparaíso",
                tipo = "Cátedra"
            ),
            EnrolledClassBlock(
                sigla = "MAT022",
                asignaturaNombre = "Matemática II",
                paralelo = "1",
                dia = 2,
                bloque = 5,
                sala = "F-102",
                profesor = "Docente",
                campus = "Casa Central Valparaíso",
                tipo = "Cátedra"
            )
        )

        val topesIgnorados = setOf("INF130:MAT022")

        val grouped = enrolled.groupBy { "${it.dia}_${it.bloque}" }
        val topes = grouped.filter { entry ->
            val distinctSiglas = entry.value.map { it.sigla.trim().uppercase() }.distinct()
            if (distinctSiglas.size <= 1) return@filter false

            val isIgnored = distinctSiglas.size == 2 && run {
                val s1 = distinctSiglas[0]
                val s2 = distinctSiglas[1]
                topesIgnorados.contains("${s1}:${s2}") || topesIgnorados.contains("${s2}:${s1}")
            }

            !isIgnored
        }

        assertTrue("Explicitly ignored clashes must be filtered out from schedule warnings", topes.isEmpty())
    }
}
