package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ProfesorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfesorDao {

    @Query("SELECT * FROM profesores ORDER BY name ASC")
    fun getAllProfesores(): Flow<List<ProfesorEntity>>

    @Query("SELECT * FROM profesores WHERE sedes LIKE '%' || :campus || '%' OR :campus = '' ORDER BY name ASC")
    fun getProfesoresByCampus(campus: String): Flow<List<ProfesorEntity>>

    @Query("SELECT * FROM profesores WHERE id = :id LIMIT 1")
    suspend fun getProfesorById(id: String): ProfesorEntity?

    @Query("SELECT * FROM profesores WHERE name = :name LIMIT 1")
    suspend fun getProfesorByName(name: String): ProfesorEntity?

    @Query("""
        SELECT * FROM profesores 
        WHERE name LIKE '%' || :query || '%'
           OR departamento LIKE '%' || :query || '%'
           OR ramosImpartidos LIKE '%' || :query || '%'
           OR aliases LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchProfesores(query: String): Flow<List<ProfesorEntity>>

    @Query("""
        SELECT * FROM profesores 
        WHERE (sedes LIKE '%' || :campus || '%' OR :campus = '')
          AND (name LIKE '%' || :query || '%'
               OR departamento LIKE '%' || :query || '%'
               OR ramosImpartidos LIKE '%' || :query || '%'
               OR aliases LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchProfesoresByCampus(query: String, campus: String): Flow<List<ProfesorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfesores(profesores: List<ProfesorEntity>)

    @Query("DELETE FROM profesores")
    suspend fun clearProfesores()

    @Query("SELECT COUNT(*) FROM profesores")
    suspend fun count(): Int
}
