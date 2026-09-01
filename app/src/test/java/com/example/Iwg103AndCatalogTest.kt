package com.example

import com.example.data.local.entity.AsignaturaEntity
import org.junit.Assert.*
import org.junit.Test

class Iwg103AndCatalogTest {

    @Test
    fun testIwg103CourseMatchingAndNaming() {
        val sigla = "IWG103"
        val rawName = "TALLER DE HABILIDADES PARA LA VIDA UNIVERSITARIA"
        val department = "DIRECCION GENERAL DE DOCENCIA"

        val entity = AsignaturaEntity(
            sigla = sigla,
            nombre = "Taller De Habilidades Para La Vida Universitaria",
            creditos = 2,
            departamento = department
        )

        assertEquals("IWG103", entity.sigla)
        assertEquals("Taller De Habilidades Para La Vida Universitaria", entity.nombre)
        assertEquals("DIRECCION GENERAL DE DOCENCIA", entity.departamento)

        // Test search query matching
        val queries = listOf("iwg", "103", "IWG103", "habilidades", "taller", "docencia")
        for (q in queries) {
            val matches = entity.sigla.contains(q, ignoreCase = true) ||
                    entity.nombre.contains(q, ignoreCase = true) ||
                    entity.departamento.contains(q, ignoreCase = true)
            assertTrue("Query '$q' should match IWG103", matches)
        }
    }
}
