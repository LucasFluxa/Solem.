package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CarreraPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarreraPlanDao {

    @Query("SELECT * FROM carreras_planes ORDER BY nombre ASC")
    fun getAllCarreras(): Flow<List<CarreraPlanEntity>>

    @Query("SELECT * FROM carreras_planes WHERE codigoCarrera = :codigo")
    fun getPlanesForCarrera(codigo: String): Flow<List<CarreraPlanEntity>>

    @Query("SELECT * FROM carreras_planes WHERE codigoCarrera = :codigo AND tipoMalla = :tipo LIMIT 1")
    suspend fun getCarreraByCodigoAndTipo(codigo: String, tipo: String): CarreraPlanEntity?

    @Query("SELECT * FROM carreras_planes WHERE codigoCarrera = :codigo LIMIT 1")
    suspend fun getCarreraByCodigo(codigo: String): CarreraPlanEntity?

    @Query("SELECT * FROM carreras_planes WHERE codigoCarrera = :codigo AND tipoMalla = :tipo LIMIT 1")
    fun observeCarreraByCodigoAndTipo(codigo: String, tipo: String): Flow<CarreraPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarreras(carreras: List<CarreraPlanEntity>)

    @Query("DELETE FROM carreras_planes")
    suspend fun clearCarreras()

    @Query("SELECT COUNT(*) FROM carreras_planes")
    suspend fun count(): Int
}
