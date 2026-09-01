package com.example.ui.horario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AsignaturaEntity
import com.example.data.local.entity.BloqueHorarioEntity
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class EnrolledClassBlock(
    val sigla: String,
    val asignaturaNombre: String,
    val paralelo: String,
    val dia: Int,
    val bloque: Int,
    val sala: String?,
    val profesor: String?,
    val campus: String?,
    val tipo: String?,
    val periodo: String = "2026-2",
    val semanaIntercalada: String? = null // "A" (Semana 1 / Impar) o "B" (Semana 2 / Par)
)

data class HorarioTope(
    val dia: Int,
    val bloque: Int,
    val clases: List<EnrolledClassBlock>
)

enum class HorarioViewMode {
    SEMANAL_MATRIZ,
    DIARIA_DETALLADA
}

@OptIn(ExperimentalCoroutinesApi::class)
class HorarioViewModel(
    private val repository: UsmDataRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow

    // Día de la semana actual por defecto (0: Lunes, 1: Martes, etc., Sábado/Domingo -> 0)
    val todayWeekdayIndex: Int = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        else -> 0
    }

    private val _selectedDay = MutableStateFlow(todayWeekdayIndex)
    val selectedDay = _selectedDay.asStateFlow()

    private val _viewMode = MutableStateFlow(HorarioViewMode.SEMANAL_MATRIZ)
    val viewMode = _viewMode.asStateFlow()

    fun setViewMode(mode: HorarioViewMode) {
        _viewMode.value = mode
    }

    val searchQueryAsignatura = MutableStateFlow("")

    val allAsignaturas: StateFlow<List<AsignaturaEntity>> = repository.getAllAsignaturas()
        .flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableAsignaturas: StateFlow<List<AsignaturaEntity>> = combine(
        allAsignaturas,
        userPreferences.flatMapLatest { prefs -> repository.getCarreraByCodigoAndTipo(prefs.selectedCarreraCodigo, prefs.selectedPlanTipo) },
        searchQueryAsignatura
    ) { all, plan, query ->
        val careerSiglas = plan?.siglas?.toSet() ?: emptySet()
        val baseList = if (careerSiglas.isNotEmpty()) {
            all.filter { asig ->
                val base = if (asig.sigla.any { it.isDigit() }) {
                    asig.sigla.replace(Regex("[^0-9]+\$"), "")
                } else asig.sigla
                careerSiglas.contains(asig.sigla) || careerSiglas.contains(base)
            }
        } else {
            all
        }

        if (query.isBlank()) {
            baseList
        } else {
            val q = query.trim().lowercase()
            all.filter {
                it.sigla.lowercase().contains(q) ||
                        it.nombre.lowercase().contains(q) ||
                        it.departamento.lowercase().contains(q)
            }
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bloques del campus y periodo activo del usuario
    val campusBloques: StateFlow<List<BloqueHorarioEntity>> = userPreferences.flatMapLatest { prefs ->
        repository.getBloquesByCampusAndPeriodo(prefs.selectedCampus, prefs.selectedPeriodo)
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bloques correspondientes únicamente al Horario Propio del estudiante en el periodo activo
    val miHorarioBloques: StateFlow<List<EnrolledClassBlock>> = userPreferences.flatMapLatest { prefs ->
        val enrolledParalelos = prefs.misParalelosInscritos // "SIGLA:PARALELO:CAMPUS:PERIODO"
        val enrolledRamos = prefs.misRamosInscritos // Set<String>
        val intercaladas = prefs.clasesIntercaladas

        val enrolledSiglas = (enrolledParalelos.mapNotNull { it.split(":").firstOrNull()?.trim() } + enrolledRamos)
            .filter { it.isNotBlank() }
            .distinct()

        if (enrolledSiglas.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(
                repository.getBloquesBySiglas(enrolledSiglas),
                repository.getAsignaturasBySiglas(enrolledSiglas)
            ) { bloquesForSiglas, asignaturas ->
                val asignaturasMap = asignaturas.associateBy { it.sigla }

                data class Inscription(val sigla: String, val paralelo: String, val campus: String, val periodo: String)

                val parsedInscriptions = enrolledParalelos.mapNotNull { entry ->
                    val parts = entry.split(":")
                    if (parts.size >= 2) {
                        val sigla = parts[0].trim()
                        val paralelo = parts[1].trim()
                        val campus = parts.getOrNull(2)?.trim()?.takeIf { it.isNotBlank() } ?: prefs.selectedCampus
                        val per = parts.getOrNull(3)?.trim()?.takeIf { it.isNotBlank() } ?: prefs.selectedPeriodo
                        Inscription(sigla, paralelo, campus, per)
                    } else null
                }.toMutableList()

                // Fallback para ramos inscritos que no tengan paralelo explícito
                enrolledRamos.forEach { sigla ->
                    if (parsedInscriptions.none { it.sigla.equals(sigla, ignoreCase = true) }) {
                        parsedInscriptions.add(Inscription(sigla.trim(), "1", prefs.selectedCampus, prefs.selectedPeriodo))
                    }
                }

                bloquesForSiglas.filter { b: BloqueHorarioEntity ->
                    parsedInscriptions.any { insc ->
                        val siglaMatch = b.sigla.equals(insc.sigla, ignoreCase = true)
                        val parClean = insc.paralelo.replace("P", "").trim()
                        val bParClean = b.paralelo.replace("P", "").trim()
                        val paraleloMatch = (b.paralelo.equals(insc.paralelo, ignoreCase = true) || parClean == bParClean)
                        val campusMatch = b.campus.isNullOrBlank() || b.campus.equals(insc.campus, ignoreCase = true) || b.campus.contains(insc.campus, ignoreCase = true) || insc.campus.contains(b.campus, ignoreCase = true)
                        val periodoMatch = (b.periodo.isBlank() || b.periodo.equals(insc.periodo, ignoreCase = true) || b.periodo.equals(prefs.selectedPeriodo, ignoreCase = true))
                        siglaMatch && paraleloMatch && campusMatch && periodoMatch
                    }
                }.map { b: BloqueHorarioEntity ->
                    // Determinar si es una clase intercalada (Semana A o B)
                    var semTag: String? = null
                    val bSigla = b.sigla.trim().uppercase()
                    val bPar = b.paralelo.trim().replace("P", "")

                    for (entry in intercaladas) {
                        val parts = entry.split(":")
                        if (parts.size >= 5) {
                            val s1 = parts[0]
                            val p1 = parts[1]
                            val s2 = parts[2]
                            val p2 = parts[3]
                            val first = parts[4]

                            if (bSigla == s1 && (p1.isBlank() || bPar == p1)) {
                                semTag = if (first == s1) "A" else "B"
                                break
                            } else if (bSigla == s2 && (p2.isBlank() || bPar == p2)) {
                                semTag = if (first == s2) "A" else "B"
                                break
                            }
                        }
                    }

                    EnrolledClassBlock(
                        sigla = b.sigla,
                        asignaturaNombre = asignaturasMap[b.sigla]?.nombre ?: b.sigla,
                        paralelo = b.paralelo,
                        dia = b.dia,
                        bloque = b.bloque,
                        sala = b.sala,
                        profesor = b.profesor,
                        campus = b.campus,
                        tipo = b.tipo,
                        periodo = b.periodo,
                        semanaIntercalada = semTag
                    )
                }
            }
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Detección de Topes de Horario (Schedule conflicts)
    val topesDeHorario: StateFlow<List<HorarioTope>> = combine(
        miHorarioBloques,
        userPreferences
    ) { bloques, prefs ->
        val intercaladas = prefs.clasesIntercaladas
        val ignorados = prefs.topesIgnorados
        val grouped = bloques.groupBy { "${it.dia}_${it.bloque}" }

        grouped.filter { entry ->
            val distinctSiglas = entry.value.map { it.sigla.trim().uppercase() }.distinct()
            if (distinctSiglas.size <= 1) return@filter false

            // Verificar si el choque está resuelto como intercalado o ignorado
            val classesInBlock = entry.value
            val isIntercalatedPair = classesInBlock.all { it.semanaIntercalada != null } &&
                    classesInBlock.map { it.semanaIntercalada }.distinct().size > 1

            if (isIntercalatedPair) return@filter false

            // Verificar si el choque fue ignorado explícitamente
            val isIgnored = distinctSiglas.size == 2 && run {
                val s1 = distinctSiglas[0]
                val s2 = distinctSiglas[1]
                val pairKey1 = "${s1}:${s2}"
                val pairKey2 = "${s2}:${s1}"
                ignorados.contains(pairKey1) || ignorados.contains(pairKey2)
            }

            !isIgnored
        }.map { entry ->
            val first = entry.value.first()
            HorarioTope(
                dia = first.dia,
                bloque = first.bloque,
                clases = entry.value.distinctBy { "${it.sigla}_${it.paralelo}" }
            )
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bloques del día seleccionado para Mi Horario
    val bloquesForDay: StateFlow<List<EnrolledClassBlock>> = combine(
        miHorarioBloques,
        _selectedDay
    ) { bloques, day ->
        bloques.filter { it.dia == day }
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Asignaturas de la carrera del usuario para fácil inscripción
    val activeCarreraPlanes: StateFlow<com.example.data.local.entity.CarreraPlanEntity?> = userPreferences.flatMapLatest { prefs ->
        repository.getCarreraByCodigoAndTipo(prefs.selectedCarreraCodigo, prefs.selectedPlanTipo)
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    /**
     * Revisa si inscribir este paralelo causará un choque de horario con ramos ya inscritos.
     */
    suspend fun getConflictingEnrolledClasses(
        sigla: String,
        paralelo: String,
        campus: String = userPreferences.value.selectedCampus,
        periodo: String = userPreferences.value.selectedPeriodo
    ): List<EnrolledClassBlock> {
        val cleanSigla = sigla.trim().uppercase()
        val cleanPar = paralelo.trim().replace("P", "")
        val allParalelos = repository.getParalelosBySiglaAndPeriodo(sigla, periodo).firstOrNull() ?: emptyList()
        val matchingParalelo = allParalelos.find {
            it.paralelo.replace("P", "").trim() == cleanPar &&
            (campus.isBlank() || it.campus.isBlank() || it.campus.contains(campus, ignoreCase = true) || campus.contains(it.campus, ignoreCase = true))
        } ?: allParalelos.find { it.paralelo.replace("P", "").trim() == cleanPar }

        val newBlocks = if (matchingParalelo != null) {
            repository.getBloquesByParaleloId(matchingParalelo.id).firstOrNull() ?: emptyList()
        } else emptyList()

        val currentEnrolled = miHorarioBloques.value.filter { !it.sigla.equals(cleanSigla, ignoreCase = true) }

        return currentEnrolled.filter { enrolled ->
            newBlocks.any { nb -> nb.dia == enrolled.dia && nb.bloque == enrolled.bloque }
        }.distinctBy { "${it.sigla}_${it.paralelo}" }
    }

    fun enrollParalelo(
        sigla: String,
        paralelo: String,
        campus: String = userPreferences.value.selectedCampus,
        periodo: String = userPreferences.value.selectedPeriodo
    ) {
        viewModelScope.launch {
            userPreferencesRepository.enrollParalelo(sigla, paralelo, campus, periodo)
        }
    }

    fun enrollIntercalado(
        sigla1: String,
        par1: String,
        sigla2: String,
        par2: String,
        firstSigla: String,
        campus: String = userPreferences.value.selectedCampus,
        periodo: String = userPreferences.value.selectedPeriodo
    ) {
        viewModelScope.launch {
            userPreferencesRepository.enrollParalelo(sigla1, par1, campus, periodo)
            userPreferencesRepository.setClasesIntercaladas(sigla1, par1, sigla2, par2, firstSigla)
        }
    }

    fun ignoreTopeAndEnroll(
        sigla1: String,
        par1: String,
        sigla2: String,
        par2: String,
        campus: String = userPreferences.value.selectedCampus,
        periodo: String = userPreferences.value.selectedPeriodo
    ) {
        viewModelScope.launch {
            userPreferencesRepository.enrollParalelo(sigla1, par1, campus, periodo)
            userPreferencesRepository.ignoreTope(sigla1, sigla2)
        }
    }

    fun unenrollRamo(sigla: String) {
        viewModelScope.launch {
            userPreferencesRepository.unenrollRamo(sigla)
            userPreferencesRepository.removeClasesIntercaladas(sigla)
        }
    }

    fun unenrollParalelo(sigla: String, paralelo: String) {
        viewModelScope.launch {
            userPreferencesRepository.unenrollParalelo(sigla, paralelo)
            userPreferencesRepository.removeClasesIntercaladas(sigla)
        }
    }

    fun getParalelosForSigla(sigla: String) = userPreferences.flatMapLatest { prefs ->
        repository.getParalelosBySiglaCampusAndPeriodo(sigla, prefs.selectedCampus, prefs.selectedPeriodo)
    }

    fun getAllParalelosForSigla(sigla: String) = userPreferences.flatMapLatest { prefs ->
        repository.getParalelosBySiglaAndPeriodo(sigla, prefs.selectedPeriodo)
    }

    fun getBloquesForParalelo(paraleloId: String, sigla: String = "", paralelo: String = "") =
        repository.getBloquesByParaleloId(paraleloId)

    fun getBloquesForParaleloId(paraleloId: String) = repository.getBloquesByParaleloId(paraleloId)

    fun getBloquesForSigla(sigla: String) = userPreferences.flatMapLatest { prefs ->
        repository.getBloquesBySiglaAndPeriodo(sigla, prefs.selectedPeriodo)
    }

    companion object {
        fun provideFactory(
            repository: UsmDataRepository,
            userPreferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HorarioViewModel(repository, userPreferencesRepository) as T
                }
            }
    }
}
