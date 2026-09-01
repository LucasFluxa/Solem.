package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BloqueHorarioEntity
import com.example.data.local.entity.ParaleloEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HorarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParalelos(paralelos: List<ParaleloEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloques(bloques: List<BloqueHorarioEntity>)

    @Query("SELECT * FROM paralelos WHERE sigla = :sigla ORDER BY paralelo ASC")
    fun getParalelosBySigla(sigla: String): Flow<List<ParaleloEntity>>

    @Query("SELECT * FROM paralelos WHERE sigla = :sigla AND (periodo = :periodo OR periodo = '' OR :periodo = '') ORDER BY paralelo ASC")
    fun getParalelosBySiglaAndPeriodo(sigla: String, periodo: String): Flow<List<ParaleloEntity>>

    @Query("SELECT * FROM paralelos WHERE sigla = :sigla AND (campus = :campus OR campus LIKE '%' || :campus || '%' OR :campus LIKE '%' || campus || '%' OR :campus = '') AND (periodo = :periodo OR periodo = '' OR :periodo = '') ORDER BY CAST(paralelo AS INTEGER) ASC, paralelo ASC")
    fun getParalelosBySiglaCampusAndPeriodo(sigla: String, campus: String, periodo: String): Flow<List<ParaleloEntity>>

    @Query("SELECT * FROM bloques_horario WHERE sigla = :sigla ORDER BY dia ASC, bloque ASC")
    fun getBloquesBySigla(sigla: String): Flow<List<BloqueHorarioEntity>>

    @Query("SELECT * FROM bloques_horario WHERE sigla = :sigla AND (periodo = :periodo OR periodo = '' OR :periodo = '') ORDER BY dia ASC, bloque ASC")
    fun getBloquesBySiglaAndPeriodo(sigla: String, periodo: String): Flow<List<BloqueHorarioEntity>>

    @Query("SELECT * FROM bloques_horario WHERE sigla = :sigla AND (campus = :campus OR campus LIKE '%' || :campus || '%' OR :campus LIKE '%' || campus || '%' OR :campus = '') AND (periodo = :periodo OR periodo = '' OR :periodo = '') ORDER BY dia ASC, bloque ASC")
    fun getBloquesBySiglaCampusAndPeriodo(sigla: String, campus: String, periodo: String): Flow<List<BloqueHorarioEntity>>

    @Query("SELECT * FROM bloques_horario WHERE paraleloId = :paraleloId ORDER BY dia ASC, bloque ASC")
    fun getBloquesByParaleloId(paraleloId: String): Flow<List<BloqueHorarioEntity>>

    @Query("SELECT * FROM bloques_horario WHERE profesor LIKE '%' || :profesorName || '%'")
    fun getBloquesByProfesor(profesorName: String): Flow<List<BloqueHorarioEntity>>

    @Query("SELECT * FROM bloques_horario WHERE campus = :campus ORDER BY dia ASC, bloque ASC")
    fun getBloquesByCampus(campus: String): Flow<List<BloqueHorarioEntity>>

    @Query("SELECT * FROM bloques_horario WHERE campus = :campus AND periodo = :periodo ORDER BY dia ASC, bloque ASC")
    fun getBloquesByCampusAndPeriodo(campus: String, periodo: String): Flow<List<BloqueHorarioEntity>>

    @Query("SELECT * FROM bloques_horario WHERE sigla IN (:siglas) ORDER BY dia ASC, bloque ASC")
    fun getBloquesBySiglas(siglas: List<String>): Flow<List<BloqueHorarioEntity>>

    @Query("SELECT * FROM bloques_horario ORDER BY dia ASC, bloque ASC")
    fun getAllBloques(): Flow<List<BloqueHorarioEntity>>

    @Query("SELECT DISTINCT campus FROM paralelos WHERE campus != '' ORDER BY campus ASC")
    fun getCampusList(): Flow<List<String>>

    @Query("SELECT DISTINCT periodo FROM paralelos WHERE periodo != '' ORDER BY periodo DESC")
    fun getPeriodosList(): Flow<List<String>>

    @Query("SELECT DISTINCT profesor FROM bloques_horario WHERE campus = :campus AND profesor != ''")
    fun getProfesorNamesByCampus(campus: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM paralelos")
    suspend fun countParalelos(): Int

    @Query("SELECT COUNT(*) FROM bloques_horario")
    suspend fun countBloques(): Int

    @Query("DELETE FROM paralelos")
    suspend fun clearParalelos()

    @Query("DELETE FROM bloques_horario")
    suspend fun clearBloques()
}
