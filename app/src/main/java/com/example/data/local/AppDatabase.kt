package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.AsignaturaDao
import com.example.data.local.dao.CarreraPlanDao
import com.example.data.local.dao.HorarioDao
import com.example.data.local.dao.ProfesorDao
import com.example.data.local.dao.ReviewDao
import com.example.data.local.entity.AsignaturaEntity
import com.example.data.local.entity.BloqueHorarioEntity
import com.example.data.local.entity.CarreraPlanEntity
import com.example.data.local.entity.ParaleloEntity
import com.example.data.local.entity.ProfesorEntity
import com.example.data.local.entity.ReviewEntity

@Database(
    entities = [
        AsignaturaEntity::class,
        ParaleloEntity::class,
        BloqueHorarioEntity::class,
        ProfesorEntity::class,
        ReviewEntity::class,
        CarreraPlanEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun asignaturaDao(): AsignaturaDao
    abstract fun horarioDao(): HorarioDao
    abstract fun profesorDao(): ProfesorDao
    abstract fun reviewDao(): ReviewDao
    abstract fun carreraPlanDao(): CarreraPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solem_usm_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
