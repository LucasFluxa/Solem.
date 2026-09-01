package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import java.io.IOException

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val selectedCampus: String = "Casa Central Valparaíso",
    val selectedCarreraCodigo: String = "",
    val selectedCarreraNombre: String = "",
    val selectedPlanTipo: String = "NUEVA", // "NUEVA" o "ANTIGUA"
    val selectedPeriodo: String = "2026-2",
    val misRamosInscritos: Set<String> = emptySet(), // Siglas inscritas en mi horario
    val misParalelosInscritos: Set<String> = emptySet(), // Formato "SIGLA:PARALELO:CAMPUS:PERIODO"
    val ramosAprobados: Set<String> = emptySet(), // Siglas de ramos aprobados en la malla
    val ramosCursando: Set<String> = emptySet(), // Siglas de ramos que está cursando
    val notificacionesClaseActivas: Boolean = true,
    val minutosAnticipacionNotificacion: Int = 15,
    val isOnboardingCompleted: Boolean = false,
    val lastDataVersion: Int = 0,
    val lastSyncUnix: Long = 0L
)

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_SELECTED_CAMPUS = stringPreferencesKey("selected_campus")
        val KEY_SELECTED_CARRERA_CODIGO = stringPreferencesKey("selected_carrera_codigo")
        val KEY_SELECTED_CARRERA_NOMBRE = stringPreferencesKey("selected_carrera_nombre")
        val KEY_SELECTED_PLAN_TIPO = stringPreferencesKey("selected_plan_tipo")
        val KEY_SELECTED_PERIODO = stringPreferencesKey("selected_periodo")
        val KEY_MIS_RAMOS_INSCRITOS = stringSetPreferencesKey("mis_ramos_inscritos")
        val KEY_MIS_PARALELOS_INSCRITOS = stringSetPreferencesKey("mis_paralelos_inscritos")
        val KEY_RAMOS_APROBADOS = stringSetPreferencesKey("ramos_aprobados")
        val KEY_RAMOS_CURSANDO = stringSetPreferencesKey("ramos_cursando")
        val KEY_NOTIFICACIONES_ACTIVAS = booleanPreferencesKey("notificaciones_clase_activas")
        val KEY_MINUTOS_ANTICIPACION = intPreferencesKey("minutos_anticipacion_notificacion")
        val KEY_IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val KEY_LAST_DATA_VERSION = intPreferencesKey("last_data_version")
        val KEY_LAST_SYNC_UNIX = longPreferencesKey("last_sync_unix")

        @Volatile
        var cachedPreferences: UserPreferences? = null
    }

    private val repoScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    val userPreferencesFlow: StateFlow<UserPreferences> = context.userDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val mapped = UserPreferences(
                selectedCampus = preferences[KEY_SELECTED_CAMPUS] ?: "Casa Central Valparaíso",
                selectedCarreraCodigo = preferences[KEY_SELECTED_CARRERA_CODIGO] ?: "",
                selectedCarreraNombre = preferences[KEY_SELECTED_CARRERA_NOMBRE] ?: "",
                selectedPlanTipo = preferences[KEY_SELECTED_PLAN_TIPO] ?: "NUEVA",
                selectedPeriodo = preferences[KEY_SELECTED_PERIODO] ?: "2026-2",
                misRamosInscritos = preferences[KEY_MIS_RAMOS_INSCRITOS] ?: emptySet(),
                misParalelosInscritos = preferences[KEY_MIS_PARALELOS_INSCRITOS] ?: emptySet(),
                ramosAprobados = preferences[KEY_RAMOS_APROBADOS] ?: emptySet(),
                ramosCursando = preferences[KEY_RAMOS_CURSANDO] ?: emptySet(),
                notificacionesClaseActivas = preferences[KEY_NOTIFICACIONES_ACTIVAS] ?: true,
                minutosAnticipacionNotificacion = preferences[KEY_MINUTOS_ANTICIPACION] ?: 15,
                isOnboardingCompleted = preferences[KEY_IS_ONBOARDING_COMPLETED] ?: false,
                lastDataVersion = preferences[KEY_LAST_DATA_VERSION] ?: 0,
                lastSyncUnix = preferences[KEY_LAST_SYNC_UNIX] ?: 0L
            )
            cachedPreferences = mapped
            mapped
        }
        .stateIn(repoScope, SharingStarted.Eagerly, cachedPreferences ?: UserPreferences())

    suspend fun savePreferences(
        campus: String,
        carreraCodigo: String,
        carreraNombre: String,
        planTipo: String = "NUEVA",
        periodo: String? = null,
        completed: Boolean = true
    ) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_SELECTED_CAMPUS] = campus
            preferences[KEY_SELECTED_CARRERA_CODIGO] = carreraCodigo
            preferences[KEY_SELECTED_CARRERA_NOMBRE] = carreraNombre
            preferences[KEY_SELECTED_PLAN_TIPO] = planTipo
            preferences[KEY_SELECTED_PERIODO] = periodo ?: preferences[KEY_SELECTED_PERIODO] ?: "2026-2"
            preferences[KEY_IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun updateSyncMetadata(unix: Long, version: Int) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_UNIX] = unix
            preferences[KEY_LAST_DATA_VERSION] = version
        }
    }

    suspend fun updateCampus(campus: String) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_SELECTED_CAMPUS] = campus
        }
    }

    suspend fun updateCarrera(codigo: String, nombre: String) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_SELECTED_CARRERA_CODIGO] = codigo
            preferences[KEY_SELECTED_CARRERA_NOMBRE] = nombre
        }
    }

    suspend fun updatePlanTipo(planTipo: String) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_SELECTED_PLAN_TIPO] = planTipo
        }
    }

    suspend fun updatePeriodo(periodo: String) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_SELECTED_PERIODO] = periodo
        }
    }

    suspend fun updateNotificationSettings(enabled: Boolean, minutesBefore: Int) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_NOTIFICACIONES_ACTIVAS] = enabled
            preferences[KEY_MINUTOS_ANTICIPACION] = minutesBefore
        }
    }

    suspend fun enrollParalelo(sigla: String, paralelo: String, campus: String, periodo: String = "2026-2") {
        val cleanSigla = sigla.trim().uppercase()
        context.userDataStore.edit { preferences ->
            val currentRamos = preferences[KEY_MIS_RAMOS_INSCRITOS]?.toMutableSet() ?: mutableSetOf()
            val currentParalelos = preferences[KEY_MIS_PARALELOS_INSCRITOS]?.toMutableSet() ?: mutableSetOf()

            // Eliminar inscripciones anteriores de la MISMA sigla exacta (ej. MAT070 no borra MAT070T)
            currentParalelos.removeAll { entry ->
                entry.split(":").firstOrNull()?.equals(cleanSigla, ignoreCase = true) == true
            }

            currentRamos.add(cleanSigla)
            currentParalelos.add("$cleanSigla:$paralelo:$campus:$periodo")

            preferences[KEY_MIS_RAMOS_INSCRITOS] = currentRamos
            preferences[KEY_MIS_PARALELOS_INSCRITOS] = currentParalelos
        }
    }

    suspend fun unenrollRamo(sigla: String) {
        val cleanSigla = sigla.trim().uppercase()
        context.userDataStore.edit { preferences ->
            val currentRamos = preferences[KEY_MIS_RAMOS_INSCRITOS]?.toMutableSet() ?: mutableSetOf()
            val currentParalelos = preferences[KEY_MIS_PARALELOS_INSCRITOS]?.toMutableSet() ?: mutableSetOf()

            currentRamos.remove(cleanSigla)
            // Elimina estrictamente la sigla exacta (ej. borrar MAT070T no borra MAT070)
            currentParalelos.removeAll { entry ->
                entry.split(":").firstOrNull()?.equals(cleanSigla, ignoreCase = true) == true
            }

            preferences[KEY_MIS_RAMOS_INSCRITOS] = currentRamos
            preferences[KEY_MIS_PARALELOS_INSCRITOS] = currentParalelos
        }
    }

    suspend fun unenrollParalelo(sigla: String, paralelo: String) {
        val cleanSigla = sigla.trim().uppercase()
        context.userDataStore.edit { preferences ->
            val currentRamos = preferences[KEY_MIS_RAMOS_INSCRITOS]?.toMutableSet() ?: mutableSetOf()
            val currentParalelos = preferences[KEY_MIS_PARALELOS_INSCRITOS]?.toMutableSet() ?: mutableSetOf()

            currentParalelos.removeAll { entry ->
                val parts = entry.split(":")
                val s = parts.getOrNull(0) ?: ""
                val p = parts.getOrNull(1) ?: ""
                s.equals(cleanSigla, ignoreCase = true) && p == paralelo
            }

            // Si ya no quedan paralelos de esa sigla, quitar de misRamosInscritos
            val stillHasParalelos = currentParalelos.any { entry ->
                entry.split(":").firstOrNull()?.equals(cleanSigla, ignoreCase = true) == true
            }
            if (!stillHasParalelos) {
                currentRamos.remove(cleanSigla)
            }

            preferences[KEY_MIS_RAMOS_INSCRITOS] = currentRamos
            preferences[KEY_MIS_PARALELOS_INSCRITOS] = currentParalelos
        }
    }

    suspend fun clearHorario() {
        context.userDataStore.edit { preferences ->
            preferences[KEY_MIS_RAMOS_INSCRITOS] = emptySet()
            preferences[KEY_MIS_PARALELOS_INSCRITOS] = emptySet()
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun toggleRamoAprobado(sigla: String) {
        context.userDataStore.edit { preferences ->
            val aprobados = preferences[KEY_RAMOS_APROBADOS]?.toMutableSet() ?: mutableSetOf()

            if (aprobados.contains(sigla)) {
                aprobados.remove(sigla)
            } else {
                aprobados.add(sigla)
            }

            preferences[KEY_RAMOS_APROBADOS] = aprobados
        }
    }

    suspend fun setRamoAprobado(sigla: String, aprobado: Boolean) {
        context.userDataStore.edit { preferences ->
            val aprobados = preferences[KEY_RAMOS_APROBADOS]?.toMutableSet() ?: mutableSetOf()
            if (aprobado) {
                aprobados.add(sigla)
            } else {
                aprobados.remove(sigla)
            }
            preferences[KEY_RAMOS_APROBADOS] = aprobados
        }
    }

    suspend fun resetAvance() {
        context.userDataStore.edit { preferences ->
            preferences[KEY_RAMOS_APROBADOS] = emptySet()
            preferences[KEY_RAMOS_CURSANDO] = emptySet()
        }
    }
}
