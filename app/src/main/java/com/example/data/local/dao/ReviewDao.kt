package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {

    @Query("SELECT * FROM reviews WHERE profesorName = :profesorName ORDER BY addedAt DESC")
    fun getReviewsForProfesor(profesorName: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews ORDER BY addedAt DESC LIMIT :limit")
    fun getRecentReviews(limit: Int = 50): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Query("DELETE FROM reviews")
    suspend fun clearReviews()

    @Query("SELECT COUNT(*) FROM reviews")
    suspend fun count(): Int
}
