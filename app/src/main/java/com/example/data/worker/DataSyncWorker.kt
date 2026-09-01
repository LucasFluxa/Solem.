package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.api.NetworkModule
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.UsmDataRepositoryImpl
import java.util.concurrent.TimeUnit

class DataSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando sincronización periódica de datos SIGA en segundo plano...")
        return try {
            val database = AppDatabase.getInstance(applicationContext)
            val apiService = NetworkModule.apiService
            val moshi = NetworkModule.moshi
            val userPrefsRepo = UserPreferencesRepository(applicationContext)
            val repository = UsmDataRepositoryImpl(
                apiService = apiService,
                database = database,
                moshi = moshi,
                userPreferencesRepository = userPrefsRepo,
                context = applicationContext
            )

            val syncResult = repository.syncAllData(forceRefresh = false)
            if (syncResult.isSuccess) {
                Log.d(TAG, "Sincronización periódica completada con éxito.")
                Result.success()
            } else {
                Log.w(TAG, "Sincronización periódica falló: ${syncResult.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en DataSyncWorker: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DataSyncWorker"
        const val WORK_NAME = "usm_data_sync_periodic_12h"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
                12, TimeUnit.HOURS,
                30, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            Log.d(TAG, "Programada sincronización periódica cada 12 horas con WorkManager.")
        }
    }
}
