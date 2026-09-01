package com.example.data.dto

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = true)
data class ReviewProcessedDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "activeTags") val activeTags: List<String>? = null,
    @Json(name = "metadata") val metadata: ReviewMetadataDto? = null,
    @Json(name = "stats") val stats: ReviewStatsDto? = null
)

@JsonClass(generateAdapter = true)
data class ReviewStatsDto(
    val accesibilidad: Int? = null,
    val accesibilidadTag: String? = null,
    @Json(name = "claridad_expositiva") val claridadExpositiva: Int? = null,
    @Json(name = "coherencia_evaluativa") val coherenciaEvaluativa: Int? = null,
    @Json(name = "dificultad_percibida") val dificultadPercibida: Int? = null,
    @Json(name = "estabilidad_emocional") val estabilidadEmocional: Int? = null,
    @Json(name = "gestion_tiempo") val gestionTiempo: Int? = null,
    @Json(name = "rigor_calificatorio") val rigorCalificatorio: Int? = null
)

class ReviewStatsAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): ReviewStatsDto {
        var accesibilidad: Int? = null
        var accesibilidadTag: String? = null
        var claridad: Int? = null
        var coherencia: Int? = null
        var dificultad: Int? = null
        var estabilidad: Int? = null
        var gestion: Int? = null
        var rigor: Int? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "accesibilidad" -> {
                    if (reader.peek() == JsonReader.Token.NUMBER) {
                        accesibilidad = reader.nextInt()
                    } else if (reader.peek() == JsonReader.Token.STRING) {
                        val strVal = reader.nextString()
                        val parsed = strVal.toIntOrNull()
                        if (parsed != null) {
                            accesibilidad = parsed
                        } else {
                            // Preserva strings como "mentor"
                            accesibilidadTag = strVal
                        }
                    } else {
                        reader.skipValue()
                    }
                }
                "claridad_expositiva" -> {
                    claridad = if (reader.peek() == JsonReader.Token.NUMBER) reader.nextInt() else {
                        reader.nextString().toIntOrNull()
                    }
                }
                "coherencia_evaluativa" -> {
                    coherencia = if (reader.peek() == JsonReader.Token.NUMBER) reader.nextInt() else {
                        reader.nextString().toIntOrNull()
                    }
                }
                "dificultad_percibida" -> {
                    dificultad = if (reader.peek() == JsonReader.Token.NUMBER) reader.nextInt() else {
                        reader.nextString().toIntOrNull()
                    }
                }
                "estabilidad_emocional" -> {
                    estabilidad = if (reader.peek() == JsonReader.Token.NUMBER) reader.nextInt() else {
                        reader.nextString().toIntOrNull()
                    }
                }
                "gestion_tiempo" -> {
                    gestion = if (reader.peek() == JsonReader.Token.NUMBER) reader.nextInt() else {
                        reader.nextString().toIntOrNull()
                    }
                }
                "rigor_calificatorio" -> {
                    rigor = if (reader.peek() == JsonReader.Token.NUMBER) reader.nextInt() else {
                        reader.nextString().toIntOrNull()
                    }
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return ReviewStatsDto(
            accesibilidad = accesibilidad,
            accesibilidadTag = accesibilidadTag,
            claridadExpositiva = claridad,
            coherenciaEvaluativa = coherencia,
            dificultadPercibida = dificultad,
            estabilidadEmocional = estabilidad,
            gestionTiempo = gestion,
            rigorCalificatorio = rigor
        )
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: ReviewStatsDto?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        if (value.accesibilidad != null) {
            writer.name("accesibilidad").value(value.accesibilidad)
        } else if (value.accesibilidadTag != null) {
            writer.name("accesibilidad").value(value.accesibilidadTag)
        }
        value.claridadExpositiva?.let { writer.name("claridad_expositiva").value(it) }
        value.coherenciaEvaluativa?.let { writer.name("coherencia_evaluativa").value(it) }
        value.dificultadPercibida?.let { writer.name("dificultad_percibida").value(it) }
        value.estabilidadEmocional?.let { writer.name("estabilidad_emocional").value(it) }
        value.gestionTiempo?.let { writer.name("gestion_tiempo").value(it) }
        value.rigorCalificatorio?.let { writer.name("rigor_calificatorio").value(it) }
        writer.endObject()
    }
}

@JsonClass(generateAdapter = true)
data class ReviewMetadataDto(
    @Json(name = "score") val score: Int? = null,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "serverTime") val serverTime: String? = null,
    @Json(name = "addedAt") val addedAt: String? = null,
    @Json(name = "fingerprint") val fingerprint: String? = null
)
