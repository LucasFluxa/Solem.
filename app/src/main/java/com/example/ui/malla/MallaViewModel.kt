package com.example.ui.malla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.dto.MallaCurricularModel
import com.example.data.dto.MallaRamoModel
import com.example.data.dto.RamoEstado
import com.example.data.local.entity.CarreraPlanEntity
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.UsmDataRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class MallaProgressInfo(
    val creditosAprobados: Int = 0,
    val totalCreditos: Int = 0,
    val porcentajeAvance: Int = 0,
    val ramosAprobadosCount: Int = 0,
    val totalRamosCount: Int = 0
)

class MallaViewModel(
    private val repository: UsmDataRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val moshi: Moshi
) : ViewModel() {

    private val mallaJsonAdapter = moshi.adapter(MallaCurricularModel::class.java)

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow

    val allCarreras: StateFlow<List<CarreraPlanEntity>> = repository.getAllCarreras()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ramo seleccionado para inspeccionar prerrequisitos, habilitaciones y correquisitos
    private val _selectedRamoSigla = MutableStateFlow<String?>(null)
    val selectedRamoSigla: StateFlow<String?> = _selectedRamoSigla.asStateFlow()

    // Buscador rápido de mallas o asignaturas
    val searchQuery = MutableStateFlow("")

    data class CarreraSelection(val codigo: String, val nombre: String, val plan: String)

    private val carreraSelectionFlow = userPreferences.map { 
        CarreraSelection(it.selectedCarreraCodigo, it.selectedCarreraNombre, it.selectedPlanTipo) 
    }.distinctUntilChanged()

    // Malla Curricular estructurada del usuario activo
    val activeMalla: StateFlow<MallaCurricularModel?> = combine(
        carreraSelectionFlow,
        allCarreras
    ) { selection, carreras ->
        if (carreras.isEmpty()) return@combine null

        val userCarrera = carreras.find {
            (it.codigoCarrera == selection.codigo || it.nombre.equals(selection.nombre, ignoreCase = true)) &&
                    it.tipoMalla == selection.plan
        } ?: carreras.find {
            it.codigoCarrera == selection.codigo || it.nombre.equals(selection.nombre, ignoreCase = true)
        } ?: carreras.firstOrNull()

        val json = userCarrera?.dataJson
        if (!json.isNullOrBlank()) {
            try {
                mallaJsonAdapter.fromJson(json)
            } catch (e: Exception) {
                null
            }
        } else null
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Mapa rápido de todos los ramos de la malla por sigla
    val allMallaRamosMap: StateFlow<Map<String, MallaRamoModel>> = activeMalla.combine(MutableStateFlow(Unit)) { malla, _ ->
        val map = mutableMapOf<String, MallaRamoModel>()
        malla?.semestres?.forEach { s ->
            s.ramos.forEach { r ->
                map[r.sigla] = r
            }
        }
        map
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Prerrequisitos de la asignatura actualmente seleccionada
    val selectedPrerequisitos: StateFlow<Set<String>> = combine(
        _selectedRamoSigla,
        allMallaRamosMap
    ) { selSigla, map ->
        if (selSigla == null) emptySet()
        else map[selSigla]?.prerequisitos?.toSet() ?: emptySet()
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Asignaturas que HABILITA la asignatura seleccionada
    val selectedHabilita: StateFlow<Set<String>> = combine(
        _selectedRamoSigla,
        allMallaRamosMap
    ) { selSigla, map ->
        if (selSigla == null) emptySet()
        else map.values.filter { it.prerequisitos.contains(selSigla) }.map { it.sigla }.toSet()
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Correquisitos de la asignatura seleccionada
    val selectedCorrequisitos: StateFlow<Set<String>> = combine(
        _selectedRamoSigla,
        allMallaRamosMap
    ) { selSigla, map ->
        if (selSigla == null) emptySet()
        else map[selSigla]?.correquisitos?.toSet() ?: emptySet()
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Métricas y porcentaje de avance de la carrera
    val progressInfo: StateFlow<MallaProgressInfo> = combine(
        activeMalla,
        userPreferences
    ) { malla, prefs ->
        if (malla == null || malla.semestres.isEmpty()) {
            return@combine MallaProgressInfo()
        }

        val allRamos = malla.semestres.flatMap { it.ramos }
        val totalCreditos = if (malla.totalCreditos > 0) malla.totalCreditos else allRamos.sumOf { it.creditos }
        val totalRamosCount = allRamos.size

        val aprobadosRamos = allRamos.filter { prefs.ramosAprobados.contains(it.sigla) }
        val creditosAprobados = aprobadosRamos.sumOf { it.creditos }
        val ramosAprobadosCount = aprobadosRamos.size

        val porcentaje = if (totalCreditos > 0) {
            ((creditosAprobados.toFloat() / totalCreditos) * 100).toInt().coerceIn(0, 100)
        } else 0

        MallaProgressInfo(
            creditosAprobados = creditosAprobados,
            totalCreditos = totalCreditos,
            porcentajeAvance = porcentaje,
            ramosAprobadosCount = ramosAprobadosCount,
            totalRamosCount = totalRamosCount
        )
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MallaProgressInfo())

    private var clearSelectionJob: kotlinx.coroutines.Job? = null

    fun onRamoTapped(sigla: String) {
        selectRamo(sigla)
        viewModelScope.launch {
            userPreferencesRepository.toggleRamoAprobado(sigla)
        }
    }

    fun selectRamo(sigla: String?) {
        clearSelectionJob?.cancel()
        val newSelection = if (_selectedRamoSigla.value == sigla) null else sigla
        _selectedRamoSigla.value = newSelection

        if (newSelection != null) {
            clearSelectionJob = viewModelScope.launch {
                kotlinx.coroutines.delay(3000L)
                if (_selectedRamoSigla.value == newSelection) {
                    _selectedRamoSigla.value = null
                }
            }
        }
    }

    fun toggleRamoAprobado(sigla: String) {
        viewModelScope.launch {
            userPreferencesRepository.toggleRamoAprobado(sigla)
        }
    }

    fun switchCarrera(carrera: CarreraPlanEntity) {
        viewModelScope.launch {
            userPreferencesRepository.updateCarrera(carrera.codigoCarrera, carrera.nombre)
            userPreferencesRepository.updatePlanTipo(carrera.tipoMalla)
        }
    }

    fun resetAvance() {
        viewModelScope.launch {
            userPreferencesRepository.resetAvance()
        }
    }

    companion object {
        fun provideFactory(
            repository: UsmDataRepository,
            userPreferencesRepository: UserPreferencesRepository,
            moshi: Moshi
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MallaViewModel(repository, userPreferencesRepository, moshi) as T
                }
            }
    }
}
