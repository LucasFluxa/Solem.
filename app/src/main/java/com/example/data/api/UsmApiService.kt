package com.example.data.api

import com.example.data.dto.CarreraDto
import com.example.data.dto.HistorialCambiosDto
import com.example.data.dto.HorariosRootDto
import com.example.data.dto.MetadataDto
import com.example.data.dto.ProfessorsRootDto
import com.example.data.dto.ProgramasAcademicosRootDto
import com.example.data.dto.ReviewProcessedDto
import com.example.data.dto.ReviewsStatusDto
import retrofit2.http.GET

interface UsmApiService {

    @GET("metadata.json")
    suspend fun getMetadata(): MetadataDto

    @GET("horario_asignaturas.json")
    suspend fun getHorarios(): HorariosRootDto

    @GET("professors_view.json")
    suspend fun getProfessors(): ProfessorsRootDto

    @GET("planes_carreras.json")
    suspend fun getPlanesCarreras(): List<CarreraDto>

    @GET("programas_academicos.json")
    suspend fun getProgramasAcademicos(): ProgramasAcademicosRootDto

    @GET("reviews_processed.json")
    suspend fun getReviews(): List<ReviewProcessedDto>
}
