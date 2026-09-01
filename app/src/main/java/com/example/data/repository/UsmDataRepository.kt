package com.example.data.repository

import com.example.data.local.entity.AsignaturaEntity
import com.example.data.local.entity.BloqueHorarioEntity
import com.example.data.local.entity.CarreraPlanEntity
import com.example.data.local.entity.ParaleloEntity
import com.example.data.local.entity.ProfesorEntity
import com.example.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

interface UsmDataRepository {

    // Asignaturas
    fun getAllAsignaturas(): Flow<List<AsignaturaEntity>>
    fun getAsignaturasBySiglas(siglas: List<String>): Flow<List<AsignaturaEntity>>
    fun searchAsignaturas(query: String): Flow<List<AsignaturaEntity>>
    suspend fun getAsignaturaBySigla(sigla: String): AsignaturaEntity?
    fun getDepartamentos(): Flow<List<String>>

    // Carreras y Mallas (Nuevas y Antiguas)
    fun getAllCarreras(): Flow<List<CarreraPlanEntity>>
    fun getPlanesForCarrera(codigo: String): Flow<List<CarreraPlanEntity>>
    fun getCarreraByCodigo(codigo: String): Flow<CarreraPlanEntity?>
    fun getCarreraByCodigoAndTipo(codigo: String, tipo: String): Flow<CarreraPlanEntity?>

    // Profesores
    fun getAllProfesores(): Flow<List<ProfesorEntity>>
    fun getProfesoresByCampus(campus: String): Flow<List<ProfesorEntity>>
    fun searchProfesores(query: String): Flow<List<ProfesorEntity>>
    fun searchProfesoresByCampus(query: String, campus: String): Flow<List<ProfesorEntity>>
    suspend fun getProfesorById(id: String): ProfesorEntity?
    suspend fun getProfesorByName(name: String): ProfesorEntity?
    fun getProfesorNamesByCampus(campus: String): Flow<List<String>>

    // Reviews (con accesibilidadTag preservado)
    fun getReviewsForProfesor(profesorName: String): Flow<List<ReviewEntity>>
    fun getRecentReviews(limit: Int = 50): Flow<List<ReviewEntity>>

    // Horarios y Paralelos
    fun getParalelosBySigla(sigla: String): Flow<List<ParaleloEntity>>
    fun getParalelosBySiglaAndPeriodo(sigla: String, periodo: String): Flow<List<ParaleloEntity>>
    fun getParalelosBySiglaCampusAndPeriodo(sigla: String, campus: String, periodo: String): Flow<List<ParaleloEntity>>
    fun getBloquesBySigla(sigla: String): Flow<List<BloqueHorarioEntity>>
    fun getBloquesBySiglaAndPeriodo(sigla: String, periodo: String): Flow<List<BloqueHorarioEntity>>
    fun getBloquesBySiglaCampusAndPeriodo(sigla: String, campus: String, periodo: String): Flow<List<BloqueHorarioEntity>>
    fun getBloquesByParaleloId(paraleloId: String): Flow<List<BloqueHorarioEntity>>
    fun getBloquesByParaleloIdOrSiglaParalelo(paraleloId: String, sigla: String, paralelo: String): Flow<List<BloqueHorarioEntity>>
    fun getBloquesByProfesor(profesorName: String): Flow<List<BloqueHorarioEntity>>
    fun getBloquesForProfesor(profesor: ProfesorEntity): Flow<List<BloqueHorarioEntity>>
    fun getBloquesByCampus(campus: String): Flow<List<BloqueHorarioEntity>>
    fun getBloquesByCampusAndPeriodo(campus: String, periodo: String): Flow<List<BloqueHorarioEntity>>
    fun getBloquesBySiglas(siglas: List<String>): Flow<List<BloqueHorarioEntity>>
    fun getAllBloques(): Flow<List<BloqueHorarioEntity>>
    fun getCampusList(): Flow<List<String>>
    fun getPeriodosList(): Flow<List<String>>

    // Sincronización atómica de red a Room
    suspend fun syncAllData(forceRefresh: Boolean = false): Result<Unit>
    suspend fun isDataAvailable(): Boolean
}
