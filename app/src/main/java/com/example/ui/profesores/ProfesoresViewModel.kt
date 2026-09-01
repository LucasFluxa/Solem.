package com.example.ui.profesores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BloqueHorarioEntity
import com.example.data.local.entity.ProfesorEntity
import com.example.data.local.entity.ReviewEntity
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.UsmDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfesoresUiState(
    val selectedProfesor: ProfesorEntity? = null,
    val profesorReviews: List<ReviewEntity> = emptyList(),
    val profesorBloques: List<BloqueHorarioEntity> = emptyList(),
    val isFilteredByPreference: Boolean = true,
    val isSyncing: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfesoresViewModel(
    private val repository: UsmDataRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    private val _isFilteredByPreference = MutableStateFlow(true)
    val isFilteredByPreference = _isFilteredByPreference.asStateFlow()

    private val _uiState = MutableStateFlow(ProfesoresUiState())
    val uiState: StateFlow<ProfesoresUiState> = _uiState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow



    val profesores: StateFlow<List<ProfesorEntity>> = combine(
        searchQuery,
        _isFilteredByPreference,
        userPreferences
    ) { query, filtered, prefs ->
        Triple(query, filtered, prefs.selectedCampus)
    }.flatMapLatest { (query, filtered, campus) ->
        val effectiveCampus = if (filtered) campus else ""
        if (query.isBlank()) {
            repository.getProfesoresByCampus(effectiveCampus)
        } else {
            repository.searchProfesoresByCampus(query, effectiveCampus)
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFilterScope(filteredByPreference: Boolean) {
        _isFilteredByPreference.value = filteredByPreference
    }

    fun selectProfesor(profesor: ProfesorEntity?) {
        if (profesor == null) {
            _uiState.value = _uiState.value.copy(
                selectedProfesor = null,
                profesorReviews = emptyList(),
                profesorBloques = emptyList()
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedProfesor = profesor)
            repository.getReviewsForProfesor(profesor.name).collect { reviews ->
                _uiState.value = _uiState.value.copy(profesorReviews = reviews)
            }
        }
        viewModelScope.launch {
            repository.getBloquesForProfesor(profesor).collect { bloques ->
                _uiState.value = _uiState.value.copy(profesorBloques = bloques)
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: UsmDataRepository,
            userPreferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfesoresViewModel(repository, userPreferencesRepository) as T
                }
            }
    }
}
