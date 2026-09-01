package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.AsignaturaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignaturaDao {

    @Query("SELECT * FROM asignaturas ORDER BY sigla ASC")
    fun getAllAsignaturas(): Flow<List<AsignaturaEntity>>

    @Query("SELECT * FROM asignaturas WHERE sigla IN (:siglas)")
    fun getAsignaturasBySiglas(siglas: List<String>): Flow<List<AsignaturaEntity>>

    @Query("SELECT * FROM asignaturas WHERE sigla = :sigla LIMIT 1")
    suspend fun getAsignaturaBySigla(sigla: String): AsignaturaEntity?

    @Query("""
        SELECT * FROM asignaturas 
        WHERE sigla LIKE '%' || :query || '%' 
           OR nombre LIKE '%' || :query || '%'
           OR departamento LIKE '%' || :query || '%'
        ORDER BY sigla ASC
    """)
    fun searchAsignaturas(query: String): Flow<List<AsignaturaEntity>>

    @Query("SELECT * FROM asignaturas WHERE departamento = :departamento ORDER BY sigla ASC")
    fun getAsignaturasByDepartamento(departamento: String): Flow<List<AsignaturaEntity>>

    @Query("SELECT DISTINCT departamento FROM asignaturas WHERE departamento != '' ORDER BY departamento ASC")
    fun getDepartamentos(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsignaturas(asignaturas: List<AsignaturaEntity>)

    @Query("DELETE FROM asignaturas")
    suspend fun clearAsignaturas()

    @Query("SELECT COUNT(*) FROM asignaturas")
    suspend fun count(): Int
}
