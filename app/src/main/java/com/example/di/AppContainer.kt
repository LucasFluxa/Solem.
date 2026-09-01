package com.example.di

import android.content.Context
import com.example.data.api.NetworkModule
import com.example.data.api.UsmApiService
import com.example.data.local.AppDatabase
import com.example.data.repository.UsmDataRepository
import com.example.data.repository.UsmDataRepositoryImpl

import com.example.data.preferences.UserPreferencesRepository

interface AppContainer {
    val apiService: UsmApiService
    val database: AppDatabase
    val repository: UsmDataRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val apiService: UsmApiService by lazy {
        NetworkModule.apiService
    }

    override val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    override val repository: UsmDataRepository by lazy {
        UsmDataRepositoryImpl(
            apiService = apiService,
            database = database,
            moshi = NetworkModule.moshi,
            userPreferencesRepository = UserPreferencesRepository(context),
            context = context
        )
    }
}
