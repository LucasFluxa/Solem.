package com.example.ui.ramos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AsignaturaEntity
import com.example.data.local.entity.BloqueHorarioEntity
import com.example.data.local.entity.CarreraPlanEntity
import com.example.data.local.entity.ParaleloEntity
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.UsmDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RamosUiState(
    val selectedAsignatura: AsignaturaEntity? = null,
    val selectedParalelos: List<ParaleloEntity> = emptyList(),
    val selectedBloques: List<BloqueHorarioEntity> = emptyList(),
    val selectedDepartamento: String? = null,
    val isFilteredByPreference: Boolean = true,
    val isSyncing: Boolean = false,
    val syncError: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class RamosViewModel(
    private val repository: UsmDataRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    private val _selectedDept = MutableStateFlow<String?>(null)
    val selectedDept = _selectedDept.asStateFlow()

    private val _isFilteredByPreference = MutableStateFlow(true)
    val isFilteredByPreference = _isFilteredByPreference.asStateFlow()

    private val _uiState = MutableStateFlow(RamosUiState())
    val uiState: StateFlow<RamosUiState> = _uiState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow

    val departamentos: StateFlow<List<String>> = repository.getDepartamentos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allCarreras: StateFlow<List<CarreraPlanEntity>> = repository.getAllCarreras()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val asignaturas: StateFlow<List<AsignaturaEntity>> = combine(
        searchQuery.flatMapLatest { query ->
            if (query.isBlank()) repository.getAllAsignaturas()
            else repository.searchAsignaturas(query)
        },
        _selectedDept,
        _isFilteredByPreference,
        userPreferences,
        allCarreras
    ) { allAsignaturas, dept, filtered, prefs, carreras ->
        var result = allAsignaturas

        // Filtrado por carrera y tipo de malla (NUEVA o ANTIGUA)
        if (filtered && prefs.selectedCarreraCodigo.isNotBlank()) {
            val userCarrera = carreras.find {
                (it.codigoCarrera == prefs.selectedCarreraCodigo || it.nombre.equals(prefs.selectedCarreraNombre, ignoreCase = true)) &&
                        it.tipoMalla == prefs.selectedPlanTipo
            } ?: carreras.find {
                it.codigoCarrera == prefs.selectedCarreraCodigo || it.nombre.equals(prefs.selectedCarreraNombre, ignoreCase = true)
            }

            if (userCarrera != null && userCarrera.siglas.isNotEmpty()) {
                val siglasSet = userCarrera.siglas.toSet()
                val filteredList = result.filter { asig ->
                    val base = if (asig.sigla.any { it.isDigit() }) {
                        asig.sigla.replace(Regex("[^0-9]+\$"), "")
                    } else asig.sigla
                    siglasSet.contains(asig.sigla) || siglasSet.contains(base)
                }
                if (filteredList.isNotEmpty()) {
                    result = filteredList
                }
            }
        }

        // Filtrado por departamento si está seleccionado
        if (dept != null) {
            result = result.filter { it.departamento.equals(dept, ignoreCase = true) }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkAndSyncData()
    }

    fun checkAndSyncData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncError = null)
            val result = repository.syncAllData(forceRefresh)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isSyncing = false)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncError = err.message ?: "Error al sincronizar datos"
                )
            }
        }
    }

    fun toggleFilterScope(filteredByPreference: Boolean) {
        _isFilteredByPreference.value = filteredByPreference
    }

    fun selectDepartamento(dept: String?) {
        _selectedDept.value = if (_selectedDept.value == dept) null else dept
    }

    fun selectAsignatura(asignatura: AsignaturaEntity?) {
        if (asignatura == null) {
            _uiState.value = _uiState.value.copy(
                selectedAsignatura = null,
                selectedParalelos = emptyList(),
                selectedBloques = emptyList()
            )
            return
        }

        val periodo = userPreferences.value.selectedPeriodo
        val campus = userPreferences.value.selectedCampus
        _uiState.value = _uiState.value.copy(selectedAsignatura = asignatura)

        viewModelScope.launch {
            combine(
                repository.getParalelosBySigla(asignatura.sigla),
                repository.getBloquesBySigla(asignatura.sigla)
            ) { allParalelos, allBloques ->
                val periodFilteredParalelos = allParalelos.filter { it.periodo == periodo || it.periodo.isBlank() }
                val activeParalelos = if (periodFilteredParalelos.isNotEmpty()) periodFilteredParalelos else allParalelos

                // Ordenar para que los paralelos del campus del estudiante aparezcan primero
                val sortedParalelos = activeParalelos.sortedWith(
                    compareByDescending<ParaleloEntity> { it.campus.equals(campus, ignoreCase = true) || it.campus.contains(campus, ignoreCase = true) }
                        .thenBy { it.paralelo.toIntOrNull() ?: 999 }
                )

                sortedParalelos to allBloques
            }.collect { (paralelos, bloques) ->
                _uiState.value = _uiState.value.copy(
                    selectedParalelos = paralelos,
                    selectedBloques = bloques
                )
            }
        }
    }

    fun enrollParalelo(sigla: String, paralelo: String, campus: String = userPreferences.value.selectedCampus, periodo: String = userPreferences.value.selectedPeriodo) {
        viewModelScope.launch {
            userPreferencesRepository.enrollParalelo(sigla, paralelo, campus, periodo)
        }
    }

    fun unenrollRamo(sigla: String) {
        viewModelScope.launch {
            userPreferencesRepository.unenrollRamo(sigla)
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
                    return RamosViewModel(repository, userPreferencesRepository) as T
                }
            }
    }
}
