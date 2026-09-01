package com.example.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.data.api.UsmApiService
import com.example.data.dto.ProfesorMetricStatDto
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AsignaturaEntity
import com.example.data.local.entity.BloqueHorarioEntity
import com.example.data.local.entity.CarreraPlanEntity
import com.example.data.local.entity.ParaleloEntity
import com.example.data.local.entity.ProfesorEntity
import com.example.data.local.entity.ReviewEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.utils.toTitleCase
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import android.content.Context
import kotlinx.coroutines.withContext

class UsmDataRepositoryImpl(
    private val apiService: UsmApiService,
    private val database: AppDatabase,
    private val moshi: Moshi,
    private val userPreferencesRepository: UserPreferencesRepository? = null,
    private val context: Context? = null
) : UsmDataRepository {

    private val asignaturaDao = database.asignaturaDao()
    private val horarioDao = database.horarioDao()
    private val profesorDao = database.profesorDao()
    private val reviewDao = database.reviewDao()
    private val carreraPlanDao = database.carreraPlanDao()

    private fun <T> loadAssetParameterized(fileName: String, type: java.lang.reflect.Type): T? {
        return runCatching {
            context?.assets?.open(fileName)?.bufferedReader()?.use { reader ->
                val adapter: com.squareup.moshi.JsonAdapter<T> = moshi.adapter(type)
                adapter.fromJson(reader.readText())
            }
        }.onFailure {
            Log.w("UsmDataRepository", "Error leyendo asset $fileName: ${it.message}")
        }.getOrNull()
    }

    override fun getAllAsignaturas(): Flow<List<AsignaturaEntity>> = asignaturaDao.getAllAsignaturas()

    override fun getAsignaturasBySiglas(siglas: List<String>): Flow<List<AsignaturaEntity>> {
        if (siglas.isEmpty()) return kotlinx.coroutines.flow.flowOf(emptyList())
        return asignaturaDao.getAsignaturasBySiglas(siglas)
    }

    override fun searchAsignaturas(query: String): Flow<List<AsignaturaEntity>> =
        asignaturaDao.searchAsignaturas(query)

    override suspend fun getAsignaturaBySigla(sigla: String): AsignaturaEntity? =
        asignaturaDao.getAsignaturaBySigla(sigla)

    override fun getDepartamentos(): Flow<List<String>> = asignaturaDao.getDepartamentos()

    override fun getAllCarreras(): Flow<List<CarreraPlanEntity>> = carreraPlanDao.getAllCarreras()

    override fun getPlanesForCarrera(codigo: String): Flow<List<CarreraPlanEntity>> =
        carreraPlanDao.getPlanesForCarrera(codigo)

    override fun getCarreraByCodigo(codigo: String): Flow<CarreraPlanEntity?> =
        carreraPlanDao.observeCarreraByCodigoAndTipo(codigo, "NUEVA")

    override fun getCarreraByCodigoAndTipo(codigo: String, tipo: String): Flow<CarreraPlanEntity?> =
        carreraPlanDao.observeCarreraByCodigoAndTipo(codigo, tipo)

    override fun getAllProfesores(): Flow<List<ProfesorEntity>> =
        profesorDao.getAllProfesores()

    override fun getProfesoresByCampus(campus: String): Flow<List<ProfesorEntity>> =
        if (campus.isBlank()) profesorDao.getAllProfesores() else profesorDao.getProfesoresByCampus(campus)

    override fun searchProfesores(query: String): Flow<List<ProfesorEntity>> =
        profesorDao.searchProfesores(query)

    override fun searchProfesoresByCampus(query: String, campus: String): Flow<List<ProfesorEntity>> =
        if (campus.isBlank()) profesorDao.searchProfesores(query) else profesorDao.searchProfesoresByCampus(query, campus)

    override suspend fun getProfesorById(id: String): ProfesorEntity? = profesorDao.getProfesorById(id)

    override suspend fun getProfesorByName(name: String): ProfesorEntity? =
        profesorDao.getProfesorByName(name)

    override fun getProfesorNamesByCampus(campus: String): Flow<List<String>> =
        horarioDao.getProfesorNamesByCampus(campus)

    override fun getReviewsForProfesor(profesorName: String): Flow<List<ReviewEntity>> =
        reviewDao.getReviewsForProfesor(profesorName)

    override fun getRecentReviews(limit: Int): Flow<List<ReviewEntity>> =
        reviewDao.getRecentReviews(limit)

    override fun getParalelosBySigla(sigla: String): Flow<List<ParaleloEntity>> =
        horarioDao.getParalelosBySigla(sigla)

    override fun getParalelosBySiglaAndPeriodo(sigla: String, periodo: String): Flow<List<ParaleloEntity>> =
        horarioDao.getParalelosBySiglaAndPeriodo(sigla, periodo)

    override fun getParalelosBySiglaCampusAndPeriodo(sigla: String, campus: String, periodo: String): Flow<List<ParaleloEntity>> =
        horarioDao.getParalelosBySiglaCampusAndPeriodo(sigla, campus, periodo)

    override fun getBloquesBySigla(sigla: String): Flow<List<BloqueHorarioEntity>> =
        horarioDao.getBloquesBySigla(sigla)

    override fun getBloquesBySiglaAndPeriodo(sigla: String, periodo: String): Flow<List<BloqueHorarioEntity>> =
        horarioDao.getBloquesBySiglaAndPeriodo(sigla, periodo)

    override fun getBloquesBySiglaCampusAndPeriodo(sigla: String, campus: String, periodo: String): Flow<List<BloqueHorarioEntity>> =
        horarioDao.getBloquesBySiglaCampusAndPeriodo(sigla, campus, periodo)

    override fun getBloquesByParaleloId(paraleloId: String): Flow<List<BloqueHorarioEntity>> =
        horarioDao.getBloquesByParaleloId(paraleloId)

    override fun getBloquesByParaleloIdOrSiglaParalelo(paraleloId: String, sigla: String, paralelo: String): Flow<List<BloqueHorarioEntity>> =
        horarioDao.getBloquesByParaleloId(paraleloId)

    override fun getBloquesByProfesor(profesorName: String): Flow<List<BloqueHorarioEntity>> =
        horarioDao.getBloquesByProfesor(profesorName)

    override fun getBloquesForProfesor(profesor: ProfesorEntity): Flow<List<BloqueHorarioEntity>> {
        val aliasList = (profesor.aliases?.split("|") ?: emptyList()) + listOf(profesor.name)
        val cleanAliases = aliasList.map { it.trim().uppercase() }.filter { it.length >= 3 }
        val siglas = (profesor.ramosImpartidos?.split(",") ?: emptyList()).map { it.trim().uppercase() }.filter { it.isNotBlank() }

        return if (siglas.isNotEmpty()) {
            horarioDao.getBloquesBySiglas(siglas).map { list ->
                val matched = list.filter { b ->
                    val bProf = b.profesor?.uppercase()?.trim() ?: ""
                    cleanAliases.any { alias -> bProf.contains(alias) || alias.contains(bProf) } ||
                            (bProf.isNotBlank() && cleanAliases.any { alias ->
                                val words = alias.split(" ").filter { it.length >= 4 }
                                words.any { w -> bProf.contains(w) }
                            })
                }
                if (matched.isNotEmpty()) matched else list
            }
        } else {
            horarioDao.getBloquesByProfesor(profesor.name)
        }
    }

    override fun getBloquesByCampus(campus: String): Flow<List<BloqueHorarioEntity>> =
        horarioDao.getBloquesByCampus(campus)

    override fun getBloquesByCampusAndPeriodo(campus: String, periodo: String): Flow<List<BloqueHorarioEntity>> =
        horarioDao.getBloquesByCampusAndPeriodo(campus, periodo)

    override fun getBloquesBySiglas(siglas: List<String>): Flow<List<BloqueHorarioEntity>> {
        if (siglas.isEmpty()) return kotlinx.coroutines.flow.flowOf(emptyList())
        return horarioDao.getBloquesBySiglas(siglas)
    }

    override fun getAllBloques(): Flow<List<BloqueHorarioEntity>> =
        horarioDao.getAllBloques()

    override fun getCampusList(): Flow<List<String>> = horarioDao.getCampusList()

    override fun getPeriodosList(): Flow<List<String>> = horarioDao.getPeriodosList()

    override suspend fun isDataAvailable(): Boolean = withContext(Dispatchers.IO) {
        asignaturaDao.count() > 0 && carreraPlanDao.count() > 0 && horarioDao.countBloques() > 500
    }

    override suspend fun syncAllData(forceRefresh: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val totalBloquesLocal = horarioDao.countBloques()
            if (!forceRefresh && isDataAvailable()) {
                Log.d("UsmDataRepository", "Datos locales ya disponibles en Room ($totalBloquesLocal bloques). Carga instantánea lista.")
                return@withContext Result.success(Unit)
            }

            // Inicializar catálogo base de carreras/asignaturas mínimas solo si la base de datos está completamente vacía
            val defaultSiglas = DEFAULT_USM_CARRERAS.flatMap { it.siglas }.toSet()
            if (carreraPlanDao.count() == 0) {
                carreraPlanDao.insertCarreras(DEFAULT_USM_CARRERAS)
            }
            if (asignaturaDao.count() == 0) {
                asignaturaDao.insertAsignaturas(DEFAULT_ASIGNATURAS_BASE.filter { it.sigla in defaultSiglas })
            }
            if (profesorDao.count() == 0) {
                profesorDao.insertProfesores(DEFAULT_PROFESORES_BASE)
                reviewDao.insertReviews(DEFAULT_REVIEWS_BASE)
            }

            // 1. Comprobar metadata de assets locales y metadata remota
            val assetMetadata = loadAssetParameterized<com.example.data.dto.MetadataDto>("metadata.json", com.example.data.dto.MetadataDto::class.java)
            val assetUnix = assetMetadata?.generatedAt?.unix ?: 0L
            val assetVersion = assetMetadata?.version ?: 3

            val remoteMetadata = runCatching { apiService.getMetadata() }
                .onFailure { Log.w("UsmDataRepository", "Metadata fallback: ${it.message}") }
                .getOrNull()

            val remoteUnix = remoteMetadata?.generatedAt?.unix ?: 0L
            val remoteVersion = remoteMetadata?.version ?: 1

            val isAssetFresher = assetUnix > remoteUnix
            val targetUnix = if (isAssetFresher) assetUnix else remoteUnix
            val targetVersion = if (isAssetFresher) assetVersion else remoteVersion

            val currentPrefs = userPreferencesRepository?.userPreferencesFlow?.firstOrNull()
            val localVersion = currentPrefs?.lastDataVersion ?: 0
            val localUnix = currentPrefs?.lastSyncUnix ?: 0L

            Log.d("UsmDataRepository", "Sincronizando datos oficiales desde SIGA (Target: v$targetVersion @ $targetUnix, Local: v$localVersion @ $localUnix, Fuente: ${if (isAssetFresher) "Asset Local 2026-08-29" else "Remoto"})...")

            val planesType = Types.newParameterizedType(List::class.java, com.example.data.dto.CarreraDto::class.java)
            val programasType = Types.newParameterizedType(Map::class.java, String::class.java, Types.newParameterizedType(Map::class.java, String::class.java, Types.newParameterizedType(Map::class.java, String::class.java, Types.newParameterizedType(Map::class.java, String::class.java, com.example.data.dto.AsignaturaProgramaDto::class.java))))
            val horarioType = Types.newParameterizedType(Map::class.java, String::class.java, Types.newParameterizedType(Map::class.java, String::class.java, Types.newParameterizedType(Map::class.java, String::class.java, Types.newParameterizedType(Map::class.java, String::class.java, Types.newParameterizedType(Map::class.java, String::class.java, com.example.data.dto.ParaleloDetailDto::class.java)))))
            val profesoresType = Types.newParameterizedType(Map::class.java, String::class.java, com.example.data.dto.ProfesorDto::class.java)

            val planesDto: List<com.example.data.dto.CarreraDto>? = if (isAssetFresher) {
                loadAssetParameterized("planes_carreras.json", planesType)
            } else {
                runCatching { apiService.getPlanesCarreras() }.getOrNull() ?: loadAssetParameterized("planes_carreras.json", planesType)
            }

            val programasDto: Map<String, Map<String, Map<String, Map<String, com.example.data.dto.AsignaturaProgramaDto>>>>? = if (isAssetFresher) {
                loadAssetParameterized("programas_academicos.json", programasType)
            } else {
                runCatching { apiService.getProgramasAcademicos() }.getOrNull() ?: loadAssetParameterized("programas_academicos.json", programasType)
            }

            val horarioDto: Map<String, Map<String, Map<String, Map<String, Map<String, com.example.data.dto.ParaleloDetailDto>>>>>? = if (isAssetFresher) {
                loadAssetParameterized("horario_asignaturas.json", horarioType)
            } else {
                runCatching { apiService.getHorarios() }.getOrNull() ?: loadAssetParameterized("horario_asignaturas.json", horarioType)
            }

            val profesoresDto: Map<String, com.example.data.dto.ProfesorDto>? = if (isAssetFresher) {
                loadAssetParameterized("professors_view.json", profesoresType)
            } else {
                runCatching { apiService.getProfessors() }.getOrNull() ?: loadAssetParameterized("professors_view.json", profesoresType)
            }

            val reviewsType = Types.newParameterizedType(List::class.java, com.example.data.dto.ReviewProcessedDto::class.java)
            val reviewsDto: List<com.example.data.dto.ReviewProcessedDto>? = if (isAssetFresher) {
                loadAssetParameterized("reviews_processed.json", reviewsType)
            } else {
                runCatching { apiService.getReviews() }.getOrNull() ?: loadAssetParameterized("reviews_processed.json", reviewsType)
            }

            // 1. Programas y Nombres de Asignaturas
            val programasMap = mutableMapOf<String, String>()
            val programasNombresMap = mutableMapOf<String, String>()
            val programasCreditosMap = mutableMapOf<String, Int>()

            programasDto?.forEach { (_, carreras) ->
                carreras.forEach { (_, periodos) ->
                    periodos.forEach { (_, asignaturas) ->
                        asignaturas.forEach { (sigla, info) ->
                            if (!info.programaUrl.isNullOrBlank()) {
                                programasMap[sigla] = info.programaUrl
                            }
                            if (!info.nombre.isNullOrBlank()) {
                                programasNombresMap[sigla] = info.nombre
                            }
                            if (info.creditos > 0) {
                                programasCreditosMap[sigla] = info.creditos
                            }
                        }
                    }
                }
            }

            // 2. Clasificación de carreras y recopilación de asignaturas de mallas válidas
            data class PlanAccumulator(
                val planLabel: String,
                val carreraCodigo: String,
                val siglas: MutableSet<String> = mutableSetOf(),
                val semestres: MutableList<MutableList<com.example.data.dto.MallaRamoModel>> = mutableListOf()
            )

            // Agrupación global: Pair(nombreCarrera, jornada) -> Map(planId -> PlanAccumulator)
            val globalCarrerasMap = LinkedHashMap<Pair<String, String>, LinkedHashMap<String, PlanAccumulator>>()
            val validMallaAsignaturasMap = mutableMapOf<String, AsignaturaEntity>()

            // Clasificación y agrupación global de planes por carrera
            planesDto?.forEach { carrera ->
                val codigo = carrera.codigo ?: return@forEach
                val jornada = carrera.jornada ?: "Diurna"
                val menciones = carrera.menciones ?: emptyMap()

                val primerNombreMencion = menciones.values.firstOrNull()?.nombre
                val nombreCarrera = when {
                    !carrera.nombreCarrera.isNullOrBlank() -> carrera.nombreCarrera.trim()
                    !primerNombreMencion.isNullOrBlank() -> primerNombreMencion.trim()
                    else -> formatCarreraNameFromCode(codigo)
                }

                // Filtrar según clasificador oficial: SOLO carreras profesionales, técnicas, licenciaturas, doctorados, magísteres, piloto comercial e inf en plan común
                val categoria = clasificarCarrera(nombreCarrera)
                if (categoria == null) return@forEach

                val carreraPlansMap = globalCarrerasMap.getOrPut(nombreCarrera to jornada) { LinkedHashMap() }

                menciones.values.forEach { mencion ->
                    mencion.planes?.forEach { (planId, planDto) ->
                        val planLabel = planDto.plan?.takeIf { it.isNotBlank() } ?: planId
                        val planAcc = carreraPlansMap.getOrPut(planId) {
                            PlanAccumulator(planLabel = planLabel, carreraCodigo = codigo)
                        }

                        planDto.malla?.forEachIndexed { sIdx, mallaMap ->
                            while (planAcc.semestres.size <= sIdx) {
                                planAcc.semestres.add(mutableListOf())
                            }

                            mallaMap.forEach { (sigla, ramo) ->
                                planAcc.siglas.add(sigla)
                                val existing = validMallaAsignaturasMap[sigla]
                                val ramoNombre = (ramo.nombre?.takeIf { it.isNotBlank() } ?: programasNombresMap[sigla] ?: existing?.nombre ?: sigla).toTitleCase()
                                val ramoCreditos = if (ramo.creditos > 0) ramo.creditos else (programasCreditosMap[sigla] ?: existing?.creditos ?: 0)
                                val ramoDepto = if (!ramo.departamento.isNullOrBlank()) ramo.departamento else (existing?.departamento ?: inferDepartamentoFromSigla(sigla))
                                val ramoReqs = if (ramo.requisitosFormatted.isNotEmpty()) ramo.requisitosFormatted else (existing?.requisitos ?: emptyList())
                                val ramoEqs = if (ramo.equivalenciasSiglas.isNotEmpty()) ramo.equivalenciasSiglas else (existing?.equivalencias ?: emptyList())

                                validMallaAsignaturasMap[sigla] = AsignaturaEntity(
                                    sigla = sigla,
                                    nombre = ramoNombre,
                                    creditos = ramoCreditos,
                                    departamento = ramoDepto,
                                    programaUrl = programasMap[sigla] ?: existing?.programaUrl,
                                    horasTeoricas = ramo.horas?.teoricas ?: existing?.horasTeoricas ?: 0,
                                    horasPracticas = ramo.horas?.practicas ?: existing?.horasPracticas ?: 0,
                                    horasLaboratorios = ramo.horas?.laboratorios ?: existing?.horasLaboratorios ?: 0,
                                    horasAyudantias = ramo.horas?.ayudantias ?: existing?.horasAyudantias ?: 0,
                                    requisitos = ramoReqs,
                                    equivalencias = ramoEqs,
                                    requisitoLicenciatura = ramo.requisitoLicenciatura ?: existing?.requisitoLicenciatura ?: false
                                )

                                if (planAcc.semestres[sIdx].none { it.sigla == sigla }) {
                                    planAcc.semestres[sIdx].add(
                                        com.example.data.dto.MallaRamoModel(
                                            sigla = sigla,
                                            nombre = ramoNombre,
                                            creditos = ramoCreditos,
                                            departamento = ramoDepto,
                                            prerequisitos = ramo.prerequisitosList,
                                            correquisitos = ramo.correquisitosList,
                                            semestre = sIdx + 1
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val carreraEntities = mutableListOf<CarreraPlanEntity>()
            val mallaJsonAdapter = moshi.adapter(com.example.data.dto.MallaCurricularModel::class.java)

            globalCarrerasMap.forEach { (carreraJornada, plansMap) ->
                val (nombreCarrera, jornada) = carreraJornada
                if (plansMap.isNotEmpty()) {
                    // Orden numérico ascendente de plan_id: el ID mayor es Malla Nueva, el resto Malla Antigua
                    val sortedPlanIds = plansMap.keys.sortedBy { it.toLongOrNull() ?: 0L }
                    val idNuevo = sortedPlanIds.last()

                    sortedPlanIds.forEach { planId ->
                        val info = plansMap[planId]!!
                        val isNueva = (planId == idNuevo)
                        val tipoMalla = if (isNueva) "NUEVA" else "ANTIGUA"
                        val nombreMalla = if (isNueva) "Malla Nueva (Plan ${info.planLabel})" else "Malla Antigua (Plan ${info.planLabel})"

                        val semestreModels = info.semestres.mapIndexed { idx, ramosList ->
                            val roman = when (idx + 1) {
                                1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"; 5 -> "V"
                                6 -> "VI"; 7 -> "VII"; 8 -> "VIII"; 9 -> "IX"; 10 -> "X"
                                11 -> "XI"; 12 -> "XII"; 13 -> "XIII"; 14 -> "XIV"
                                else -> "${idx + 1}"
                            }
                            com.example.data.dto.MallaSemestreModel(
                                numeroSemestre = idx + 1,
                                romano = roman,
                                totalSct = ramosList.sumOf { it.creditos },
                                ramos = ramosList
                            )
                        }

                        val mallaModel = com.example.data.dto.MallaCurricularModel(
                            carreraCodigo = info.planLabel,
                            carreraNombre = nombreCarrera,
                            planLabel = info.planLabel,
                            tipoMalla = tipoMalla,
                            totalCreditos = semestreModels.sumOf { it.totalSct },
                            semestres = semestreModels
                        )

                        val dataJsonString = try {
                            mallaJsonAdapter.toJson(mallaModel)
                        } catch (e: Exception) {
                            ""
                        }

                        carreraEntities.add(
                            CarreraPlanEntity(
                                id = "${nombreCarrera.replace(" ", "_")}_${jornada}_${tipoMalla}_${info.planLabel.replace(" ", "_").replace("/", "_")}",
                                codigoCarrera = info.planLabel,
                                nombre = nombreCarrera,
                                jornada = jornada,
                                tipoMalla = tipoMalla,
                                nombreMalla = nombreMalla,
                                totalMenciones = 1,
                                siglas = info.siglas.toList(),
                                dataJson = dataJsonString
                            )
                        )
                    }
                }
            }

            // Usar carreras extraídas de la API oficial (o fallback si no hay datos)
            val finalCarreras = carreraEntities.ifEmpty { DEFAULT_USM_CARRERAS }

            // Recolectar estrictamente el conjunto de todas las siglas de las carreras permitidas
            val validSiglas: Set<String> = finalCarreras.flatMap { it.siglas }.toSet()

            // 3. Mapeo estricto de Asignaturas: SOLO ramos de carreras permitidas (omitiendo todos los cursos y sus ramos)
            val asignaturasMap = mutableMapOf<String, AsignaturaEntity>()

            // Primero agregar los ramos extraídos de las mallas de carreras válidas
            validMallaAsignaturasMap.forEach { (sigla, asig) ->
                if (sigla in validSiglas) {
                    asignaturasMap[sigla] = asig
                }
            }

            // Complementar con catálogo base oficial para siglas válidas
            DEFAULT_ASIGNATURAS_BASE.forEach { baseAsig ->
                if (baseAsig.sigla in validSiglas && !asignaturasMap.containsKey(baseAsig.sigla)) {
                    asignaturasMap[baseAsig.sigla] = baseAsig
                }
            }

            // Enriquecer con metadatos de programas_academicos SOLO para las siglas válidas
            validSiglas.forEach { sigla ->
                val existing = asignaturasMap[sigla]
                val progNombre = programasNombresMap[sigla]
                val progUrl = programasMap[sigla]
                val progCred = programasCreditosMap[sigla]

                if (existing != null) {
                    val fallbackName = if (existing.nombre.isBlank() || existing.nombre == sigla) (progNombre ?: existing.nombre) else existing.nombre
                    asignaturasMap[sigla] = existing.copy(
                        nombre = fallbackName.toTitleCase(),
                        creditos = if (existing.creditos <= 0 && progCred != null) progCred else existing.creditos,
                        programaUrl = progUrl ?: existing.programaUrl
                    )
                } else {
                    asignaturasMap[sigla] = AsignaturaEntity(
                        sigla = sigla,
                        nombre = (progNombre ?: sigla).toTitleCase(),
                        creditos = progCred ?: 3,
                        departamento = inferDepartamentoFromSigla(sigla),
                        programaUrl = progUrl
                    )
                }
            }

            // 4. Procesar Horarios y Paralelos para asignaturas, talleres (T), ayudantías (Y) y laboratorios (L)
            val paralelosList = mutableListOf<ParaleloEntity>()
            val bloquesList = mutableListOf<BloqueHorarioEntity>()
            val profAccumulatorMap = mutableMapOf<String, SigaProfAccumulator>()

            horarioDto?.forEach { (campus, jornadas) ->
                jornadas.forEach { (jornada, periodos) ->
                    periodos.forEach { (periodo, asignaturas) ->
                        asignaturas.forEach { (siglaRaw, paralelos) ->
                            val sigla = siglaRaw.trim().uppercase()
                            val baseSigla = if (sigla.any { it.isDigit() }) {
                                sigla.replace(Regex("[^0-9]+\$"), "")
                            } else sigla

                            // Permitir la asignatura si ella o su sigla base pertenecen a las carreras válidas
                            val isValida = sigla in validSiglas || baseSigla in validSiglas || validSiglas.isEmpty()
                            if (!isValida) return@forEach

                            // Registrar la asignatura/taller/ayudantía en asignaturasMap para búsqueda y visualización
                            if (!asignaturasMap.containsKey(sigla)) {
                                val baseAsig = asignaturasMap[baseSigla] 
                                    ?: validMallaAsignaturasMap[baseSigla] 
                                    ?: DEFAULT_ASIGNATURAS_BASE.find { it.sigla == baseSigla }
                                
                                val baseName = baseAsig?.nombre ?: programasNombresMap[baseSigla] ?: baseSigla
                                val suffixDesc = when {
                                    sigla.endsWith("T") -> " (Taller)"
                                    sigla.endsWith("Y") -> " (Ayudantía)"
                                    sigla.endsWith("L") -> " (Laboratorio)"
                                    sigla.endsWith("C") -> " (Cátedra)"
                                    else -> ""
                                }
                                val fullName = (programasNombresMap[sigla] ?: "$baseName$suffixDesc").toTitleCase()
                                val depto = paralelos.values.firstOrNull()?.departamento 
                                    ?: baseAsig?.departamento 
                                    ?: inferDepartamentoFromSigla(sigla)

                                asignaturasMap[sigla] = AsignaturaEntity(
                                    sigla = sigla,
                                    nombre = fullName,
                                    creditos = baseAsig?.creditos ?: 0,
                                    departamento = depto,
                                    programaUrl = baseAsig?.programaUrl
                                )
                            }

                            val canonicalCampus = when {
                                campus.contains("Valparaíso", ignoreCase = true) || campus.contains("Valp", ignoreCase = true) -> "Casa Central Valparaíso"
                                campus.contains("San Joaquín", ignoreCase = true) || campus.contains("Joaquín", ignoreCase = true) -> "Campus Santiago San Joaquín"
                                campus.contains("Vitacura", ignoreCase = true) -> "Campus Santiago Vitacura"
                                campus.contains("Viña", ignoreCase = true) -> "Sede Viña del Mar"
                                campus.contains("Concepción", ignoreCase = true) -> "Sede Concepción"
                                else -> campus.trim()
                            }

                            paralelos.forEach { (numParalelo, det) ->
                                val idParalelo = "${canonicalCampus}_${jornada}_${periodo}_${sigla}_${numParalelo}"
                                val depto = det.departamento ?: inferDepartamentoFromSigla(sigla)

                                paralelosList.add(
                                    ParaleloEntity(
                                        id = idParalelo,
                                        sigla = sigla,
                                        paralelo = numParalelo,
                                        campus = canonicalCampus,
                                        jornada = jornada,
                                        periodo = periodo,
                                        cupo = det.cupo,
                                        departamento = depto
                                    )
                                )

                                val blockProfs = mutableSetOf<String>()

                                det.horario?.forEach { b ->
                                    val rawSala = b.sala?.trim()?.replace("\n", " ")?.replace(Regex("\\s+"), " ")
                                    val cleanSala = if (rawSala.isNullOrBlank() || rawSala.equals("null", ignoreCase = true) || rawSala.equals("por definir", ignoreCase = true) || rawSala == "-" || rawSala == ".") null else rawSala

                                    val rawProf = b.profesor?.trim()?.replace("\n", ", ")?.replace(Regex("\\s+"), " ")
                                    val cleanProf = if (rawProf.isNullOrBlank() || rawProf.equals("null", ignoreCase = true) || rawProf == "." || rawProf.equals("NN", ignoreCase = true) || rawProf.equals(". NN", ignoreCase = true)) null else rawProf

                                    if (!rawProf.isNullOrBlank() && !rawProf.equals("null", ignoreCase = true) && rawProf != "." && !rawProf.equals("NN", ignoreCase = true) && !rawProf.equals("Sin profesor", ignoreCase = true)) {
                                        rawProf.split(",", "/", ";").map { it.trim() }.filter { it.isNotBlank() && !it.equals("NN", ignoreCase = true) && !it.equals("Sin profesor", ignoreCase = true) }.forEach {
                                            blockProfs.add(it.uppercase())
                                        }
                                    }

                                    val cleanTipo = when (b.tipo?.trim()?.lowercase()) {
                                        "cát", "cat", "cátedra", "catedra" -> "Cátedra"
                                        "prá", "pra", "práctica", "practica", "taller" -> "Taller"
                                        "lab", "laboratorio" -> "Laboratorio"
                                        "ayu", "ayudantía", "ayudantia" -> "Ayudantía"
                                        else -> b.tipo?.trim() ?: "Cátedra"
                                    }

                                    bloquesList.add(
                                        BloqueHorarioEntity(
                                            paraleloId = idParalelo,
                                            sigla = sigla,
                                            paralelo = numParalelo,
                                            campus = canonicalCampus,
                                            dia = b.dia ?: 0,
                                            bloque = b.bloque ?: 0,
                                            profesor = cleanProf,
                                            sala = cleanSala,
                                            tipo = cleanTipo,
                                            periodo = periodo
                                        )
                                    )
                                }

                                // Extraer todos los profesores del paralelo y acumular sus asignaturas y sedes
                                val rawProfList = det.profesor ?: emptyList()
                                val candidateNames = if (rawProfList.isNotEmpty()) rawProfList else blockProfs.toList()

                                for (rawName in candidateNames) {
                                    val trimmed = rawName.trim()
                                    if (trimmed.isBlank() || trimmed.equals("NN", ignoreCase = true) || trimmed.equals("Sin profesor", ignoreCase = true) || trimmed.equals("Por Designar", ignoreCase = true) || trimmed.equals("Docente", ignoreCase = true) || trimmed.equals("A Designar", ignoreCase = true) || trimmed.equals("TBA", ignoreCase = true)) continue

                                    val cleanTitleName = trimmed.split(" ").filter { it.isNotBlank() }.joinToString(" ") { word ->
                                        word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                    }
                                    if (cleanTitleName.uppercase() in listOf("SIN PROFESOR", "NN", "DOCENTE", "POR DESIGNAR", "A DESIGNAR", "TBA", ".", "-")) continue

                                    val acc = profAccumulatorMap.getOrPut(cleanTitleName) {
                                        SigaProfAccumulator(
                                            id = "p_${cleanTitleName.lowercase().replace(Regex("[^a-z0-9]"), "_")}",
                                            name = cleanTitleName,
                                            departamento = depto
                                        )
                                    }
                                    acc.sedes.add(canonicalCampus)
                                    acc.siglas.add(sigla)
                                    acc.aliases.add(trimmed.uppercase())
                                    acc.aliases.addAll(blockProfs)
                                    acc.totalBloques += (det.horario?.size ?: 0)
                                    if (acc.departamento.isNullOrBlank() && !depto.isNullOrBlank()) {
                                        acc.departamento = depto
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Profesores: Construir lista completa de todos los docentes USM (1700+)
            val profesoresList = mutableListOf<ProfesorEntity>()
            profAccumulatorMap.values.forEach { acc ->
                val matchingReviewProf = profesoresDto?.values?.firstOrNull { rProf ->
                    val rName = rProf.name?.trim()?.lowercase() ?: ""
                    rName.isNotBlank() && (acc.name.lowercase().contains(rName) || rName.contains(acc.name.lowercase()))
                }

                profesoresList.add(
                    ProfesorEntity(
                        id = acc.id,
                        name = acc.name,
                        departamento = acc.departamento,
                        sedes = acc.sedes.joinToString(", "),
                        ramosImpartidos = acc.siglas.joinToString(", "),
                        totalBloques = acc.totalBloques,
                        aliases = acc.aliases.joinToString("|"),
                        reviewCount = matchingReviewProf?.meta?.reviewCount ?: 0,
                        isArchived = matchingReviewProf?.meta?.isArchived ?: false,
                        lastUpdated = matchingReviewProf?.meta?.lastUpdated
                    )
                )
            }

            // Agregar también cualquier profesor con reviews que no esté activo en este semestre
            profesoresDto?.forEach { (profId, profDetail) ->
                val pName = profDetail.name ?: profId
                val cleanTitle = pName.split(" ").filter { it.isNotBlank() }.joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
                if (profesoresList.none { it.name.equals(cleanTitle, ignoreCase = true) }) {
                    profesoresList.add(
                        ProfesorEntity(
                            id = profId,
                            name = cleanTitle,
                            reviewCount = profDetail.meta?.reviewCount ?: 0,
                            isArchived = profDetail.meta?.isArchived ?: false,
                            lastUpdated = profDetail.meta?.lastUpdated
                        )
                    )
                }
            }

            // 5. Reviews
            val reviewsList = mutableListOf<ReviewEntity>()
            reviewsDto?.forEach { r ->
                val stats = r.stats
                reviewsList.add(
                    ReviewEntity(
                        profesorName = r.name ?: "Sin Nombre",
                        summary = r.summary,
                        activeTags = r.activeTags ?: emptyList(),
                        accesibilidad = stats?.accesibilidad,
                        accesibilidadTag = stats?.accesibilidadTag,
                        claridadExpositiva = stats?.claridadExpositiva,
                        coherenciaEvaluativa = stats?.coherenciaEvaluativa,
                        dificultadPercibida = stats?.dificultadPercibida,
                        estabilidadEmocional = stats?.estabilidadEmocional,
                        gestionTiempo = stats?.gestionTiempo,
                        rigorCalificatorio = stats?.rigorCalificatorio,
                        score = r.metadata?.score,
                        reason = r.metadata?.reason,
                        serverTime = r.metadata?.serverTime,
                        addedAt = r.metadata?.addedAt,
                        fingerprint = r.metadata?.fingerprint
                    )
                )
            }

            // Guardar en base de datos local
            database.withTransaction {
                if (asignaturasMap.isNotEmpty()) {
                    asignaturaDao.clearAsignaturas()
                    asignaturaDao.insertAsignaturas(asignaturasMap.values.toList())
                }

                if (finalCarreras.isNotEmpty()) {
                    carreraPlanDao.clearCarreras()
                    carreraPlanDao.insertCarreras(finalCarreras)
                }
                
                System.gc()

                if (paralelosList.isNotEmpty()) {
                    val finalParalelos = paralelosList.distinctBy { it.id }
                    val finalBloques = bloquesList.distinctBy { "${it.paraleloId}_${it.dia}_${it.bloque}_${it.tipo}" }
                    Log.d("UsmDataRepository", "Guardando datos oficiales SIGA: ${finalParalelos.size} paralelos, ${finalBloques.size} bloques")
                    horarioDao.clearParalelos()
                    horarioDao.clearBloques()
                    horarioDao.insertParalelos(finalParalelos)
                    horarioDao.insertBloques(finalBloques)
                }
                
                System.gc()

                if (profesoresList.isNotEmpty()) {
                    profesorDao.clearProfesores()
                    profesorDao.insertProfesores(profesoresList)
                }

                if (reviewsList.isNotEmpty()) {
                    reviewDao.clearReviews()
                    reviewDao.insertReviews(reviewsList)
                }
                
                System.gc()
            }

            if (paralelosList.isEmpty() && planesDto == null) {
                Log.w("UsmDataRepository", "No se descargaron datos remotos completos.")
                return@withContext Result.failure(Exception("No se pudo descargar el catálogo completo de SIGA"))
            }

            // Registrar metadatos de sincronización exitosa
            userPreferencesRepository?.updateSyncMetadata(targetUnix, targetVersion)

            Log.d("UsmDataRepository", "Sincronización finalizada exitosamente. Versión SIGA: $targetVersion @ $targetUnix.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UsmDataRepository", "Error durante sincronización: ${e.message}", e)
            Result.failure(e)
        }
    }

    private val EXCLUIDAS_EXPLICITAS = setOf(
        "Formación de Piloto Comercial"
    )

    private val PROFESIONALES_EXACTAS = setOf(
        "Arquitectura",
        "Construcción Civil",
        "Químico",
        "Piloto Comercial"
    )

    private val RE_DOCTORADO = Regex("^(Doc\\.|Doctorado)", RegexOption.IGNORE_CASE)
    private val RE_MAGISTER = Regex("^(Mag\\.|Magíster|Magister|Master)", RegexOption.IGNORE_CASE)
    private val RE_LICENCIATURA = Regex("^(Lic\\.|Licenciatura)", RegexOption.IGNORE_CASE)
    private val RE_TECNICA = Regex("^(Téc\\.|Tec\\.|Técnico)", RegexOption.IGNORE_CASE)
    private val RE_PROFESIONAL = Regex("^(Ing\\.|Ingeniería|Ingenieria|I\\.Civil|I\\.Ejec)", RegexOption.IGNORE_CASE)

    private fun clasificarCarrera(nombre: String?): String? {
        val n = (nombre ?: "").trim()
        if (n in EXCLUIDAS_EXPLICITAS) return null
        if (RE_DOCTORADO.containsMatchIn(n)) return "Doctorado"
        if (RE_MAGISTER.containsMatchIn(n)) return "Magíster"
        if (RE_LICENCIATURA.containsMatchIn(n)) return "Licenciatura"
        if (RE_TECNICA.containsMatchIn(n)) return "Técnica"
        if (RE_PROFESIONAL.containsMatchIn(n)) return "Profesional"
        if (n in PROFESIONALES_EXACTAS) return "Profesional"
        return null
    }

    private fun inferDepartamentoFromSigla(sigla: String): String {
        val prefix = sigla.take(3).uppercase()
        return when (prefix) {
            "MAT" -> "Departamento de Matemática"
            "FIS" -> "Departamento de Física"
            "AST" -> "Departamento de Física"
            "QUI" -> "Departamento de Química"
            "INF", "IWI" -> "Departamento de Informática"
            "ILN", "IND" -> "Departamento de Industrias"
            "ELO" -> "Departamento de Electrónica"
            "ELI" -> "Departamento de Electricidad"
            "TEL" -> "Departamento de Telemática"
            "MEC" -> "Departamento de Ingeniería Mecánica"
            "CIV" -> "Departamento de Obras Civiles"
            "MIN", "MET" -> "Departamento de Ingeniería Metalúrgica y Materiales"
            "IBT", "IIC" -> "Departamento de Ingeniería Química y Ambiental"
            "ICN" -> "Departamento de Ingeniería Comercial"
            "ARQ" -> "Departamento de Arquitectura"
            "DIS" -> "Departamento de Diseño de Productos"
            "HRW" -> "Departamento de Educación Física"
            "DEW", "CSJ", "HUM" -> "Departamento de Estudios Humanísticos"
            "IIN" -> "Facultad de Ingeniería"
            else -> "Universidad Técnica Federico Santa María"
        }
    }

    private fun formatCarreraNameFromCode(codigo: String): String {
        val trimmed = codigo.trim().uppercase()
        val baseCode = trimmed.substringBefore("-")
        return when {
            trimmed in listOf("3004", "3016", "30", "30-1", "TEL") || baseCode in listOf("3004", "3016", "30") -> "Ingeniería Civil Telemática"
            trimmed in listOf("7310", "7313", "73", "73-1", "3015", "2728", "2729", "INF") || baseCode in listOf("7310", "7313", "73", "3015", "2728") -> "Ingeniería Civil Informática"
            trimmed in listOf("6050", "6006", "6008", "6806", "6705", "6706", "6805", "60", "60-1", "IND") || baseCode in listOf("6050", "6006", "6008", "6806", "60") -> "Ingeniería Civil Industrial"
            trimmed in listOf("0406", "0410", "406", "410", "4", "4-1", "PC") || baseCode in listOf("0406", "0410", "406", "410", "4") -> "Ingeniería Civil Plan Común"
            trimmed in listOf("2106", "2111", "3103", "21", "21-1", "ELO") || baseCode in listOf("2106", "2111", "3103", "21") -> "Ingeniería Civil Electrónica"
            trimmed in listOf("2308", "2311", "23", "23-1", "ELI") || baseCode in listOf("2308", "2311", "23") -> "Ingeniería Civil Eléctrica"
            trimmed in listOf("4605", "4505", "4116", "4125", "41", "41-1", "MEC") || baseCode in listOf("4605", "4505", "4116", "4125", "41") -> "Ingeniería Civil Mecánica"
            trimmed in listOf("5119", "5117", "51", "51-1", "QUI") || baseCode in listOf("5119", "5117", "51") -> "Ingeniería Civil Química"
            trimmed in listOf("1009", "1014", "10", "10-1", "MAT") || baseCode in listOf("1009", "1014", "10") -> "Ingeniería Civil Matemática"
            trimmed in listOf("12525", "125", "125-1", "IBT") || baseCode in listOf("12525", "125") -> "Ingeniería Civil en Biotecnología"
            trimmed in listOf("11224", "11223", "112", "112-1", "FIS") || baseCode in listOf("11224", "11223", "112") -> "Ingeniería Civil Física"
            trimmed in listOf("1125", "1115", "11", "11-1", "CIV") || baseCode in listOf("1125", "1115", "11") -> "Ingeniería Civil"
            trimmed in listOf("5408", "5405", "5406", "54", "54-1", "MET") || baseCode in listOf("5408", "5405", "5406", "54") -> "Ingeniería Civil Metalúrgica"
            trimmed in listOf("5614", "5613", "56", "56-1", "MIN") || baseCode in listOf("5614", "5613", "56") -> "Ingeniería Civil de Minas"
            trimmed in listOf("8419", "8418", "84", "84-1", "AMB") || baseCode in listOf("8419", "8418", "84") -> "Ingeniería Civil Ambiental"
            trimmed in listOf("6607", "6606", "5801", "5810", "5419", "66", "66-1", "58", "58-1", "54-4", "ICN") || baseCode in listOf("6607", "6606", "5801", "5810", "66", "58") -> "Ingeniería Comercial"
            trimmed in listOf("1325", "1311", "13", "13-1", "ARQ") || baseCode in listOf("1325", "1311", "13") -> "Arquitectura"
            trimmed in listOf("4425", "4420", "44", "44-1", "DIS") || baseCode in listOf("4425", "4420", "44") -> "Ingeniería en Diseño de Productos"
            trimmed in listOf("1215", "12", "12-1") || baseCode in listOf("1215", "12") -> "Construcción Civil"
            trimmed in listOf("7920", "8518", "79", "79-1", "85", "85-1") || baseCode in listOf("7920", "8518", "79", "85") -> "Licenciatura en Física"
            trimmed in listOf("8720", "87", "87-1", "AST") || baseCode in listOf("8720", "87") -> "Licenciatura en Astrofísica"
            trimmed in listOf("8814", "88", "88-1") || baseCode in listOf("8814", "88") -> "Licenciatura en Matemática"
            trimmed in listOf("8900", "89", "89-1", "8000", "80", "80-1") || baseCode in listOf("8900", "89", "8000", "80") -> "Licenciatura en Ciencias mención Química"
            trimmed in listOf("2920", "29", "29-3", "29-4", "7200", "7211", "72", "72-1", "7405", "7400", "74", "74-1") || baseCode in listOf("2920", "29", "7200", "7211", "7405") -> "Ingeniería en Informática"
            trimmed in listOf("1720", "1703", "1717", "17", "17-4", "1696", "16-4", "92-3", "TINF") || baseCode in listOf("1720", "1703", "1717", "17", "1696") -> "Técnico Universitario en Informática"
            trimmed in listOf("2820", "2817", "28", "28-4", "22-3") || baseCode in listOf("2820", "2817", "28") -> "Técnico Universitario en Telecomunicaciones y Redes"
            trimmed in listOf("2020", "2003", "2016", "20", "20-4", "11-3") || baseCode in listOf("2020", "2003", "2016", "20") -> "Técnico Universitario en Electricidad"
            trimmed in listOf("10020", "1003", "1016", "10-4", "21-3") || baseCode in listOf("10020", "1003", "1016") -> "Técnico Universitario en Electrónica"
            trimmed in listOf("4320", "4303", "4316", "43-4", "4203", "42-4", "51-3") || baseCode in listOf("4320", "4303", "4316", "4203") -> "Técnico Universitario en Mecánica Automotriz"
            trimmed in listOf("4920", "49", "49-1") || baseCode in listOf("4920", "49") -> "Técnico Universitario en Mantenimiento Aeronáutico"
            trimmed in listOf("60020", "6003", "6017", "60-4", "65-3") || baseCode in listOf("60020", "6003", "6017") -> "Técnico Universitario en Prevención de Riesgos"
            trimmed in listOf("4400", "44-4", "4120", "4103", "4116", "41-3", "3920", "39-4") || baseCode in listOf("4400", "4120", "4103", "4116", "3920") -> "Técnico Universitario en Mecánica Industrial"
            trimmed in listOf("2220", "2216", "22-4") || baseCode in listOf("2220", "2216") -> "Técnico Universitario en Automatización y Control"
            trimmed in listOf("2320", "2314", "23-4", "5715", "57-4") || baseCode in listOf("2320", "2314", "5715") -> "Técnico Universitario en Robótica y Mecatrónica"
            trimmed in listOf("3420", "3403", "3417", "34-4", "63-3") || baseCode in listOf("3420", "3403", "3417") -> "Técnico Universitario en Construcción"
            trimmed in listOf("12625", "12725", "126-3", "126-4") || baseCode in listOf("12625", "12725") -> "Técnico Universitario en Ciencia de Datos"
            trimmed in listOf("5320", "5317", "53-4", "5103", "51-4", "84-3") || baseCode in listOf("5320", "5317", "5103") -> "Técnico Universitario en Química"
            else -> "Plan de Estudio $codigo"
        }
    }
}

// -------------------------------------------------------------
// CATÁLOGO OFICIAL DE PLANES DE ESTUDIO USM (SIGA / DOCENCIA)
// -------------------------------------------------------------
val DEFAULT_USM_CARRERAS = listOf(
    // Plan 3004 (Malla Nueva) / 3016 (Malla Antigua) Ingeniería Civil Telemática
    CarreraPlanEntity(
        id = "3004_Diurna_NUEVA",
        codigoCarrera = "3004",
        nombre = "Ingeniería Civil Telemática",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 3004 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "TEL100", "INF130", "CSJ100",
            "MAT023", "FIS120", "TEL101", "ELO211", "HRW101",
            "MAT024", "FIS130", "TEL211", "INF246", "INF320",
            "MAT060", "TEL221", "TEL311", "TEL321", "TEL331",
            "ILN250", "INF352", "TEL390", "INF399"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "3016_Diurna_ANTIGUA",
        codigoCarrera = "3016",
        nombre = "Ingeniería Civil Telemática",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 3016 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "TEL100", "HRW101",
            "MAT023", "FIS120", "TEL101", "ELO211", "CSJ100",
            "MAT024", "FIS130", "TEL211", "INF246", "INF320",
            "MAT060", "TEL221", "TEL311", "TEL321", "TEL331",
            "ILN250", "TEL390", "INF399"
        ),
        dataJson = ""
    ),

    // Plan 7310 (Malla Nueva) / 7313 (Malla Antigua) Ingeniería Civil Informática
    CarreraPlanEntity(
        id = "7310_Diurna_NUEVA",
        codigoCarrera = "7310",
        nombre = "Ingeniería Civil Informática",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 7310 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "INF130", "IIN100", "CSJ100",
            "MAT023", "FIS120", "INF140", "INF225", "HRW101",
            "MAT024", "FIS130", "INF221", "INF236", "INF280",
            "MAT060", "INF239", "INF246", "INF253", "INF285",
            "ILN250", "INF320", "INF330", "INF343", "INF350",
            "INF323", "INF331", "INF352", "INF360", "INF370",
            "INF376", "INF390", "INF395", "INF399", "INF380", "INF381", "INF382"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "7313_Diurna_ANTIGUA",
        codigoCarrera = "7313",
        nombre = "Ingeniería Civil Informática",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 7313 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "INF134", "QUI021", "HRW101", "DEW101",
            "MAT023", "FIS120", "INF115", "INF221", "CSJ100",
            "MAT024", "FIS130", "INF140", "INF225", "INF280",
            "MAT060", "INF236", "INF239", "INF246", "INF253",
            "ILN250", "INF285", "INF320", "INF330", "INF343",
            "INF323", "INF331", "INF350", "INF352", "INF360",
            "INF370", "INF376", "INF390", "INF395", "INF399"
        ),
        dataJson = ""
    ),

    // Plan 1325 (Malla Nueva) / 1311 (Malla Antigua) Arquitectura
    CarreraPlanEntity(
        id = "1325_Diurna_NUEVA",
        codigoCarrera = "1325",
        nombre = "Arquitectura",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 1325 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "ARQ100", "ARQ110", "HRW100", "DEW100",
            "ARQ210", "ARQ220", "ARQ230", "CSJ100",
            "ARQ310", "ARQ320", "ARQ330", "ARQ390"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "1311_Diurna_ANTIGUA",
        codigoCarrera = "1311",
        nombre = "Arquitectura",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 1311 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "ARQ100", "ARQ110", "HRW100", "DEW100",
            "ARQ210", "ARQ220", "ARQ230", "CSJ100",
            "ARQ310", "ARQ320", "ARQ330", "ARQ390"
        ),
        dataJson = ""
    ),

    // Plan 0406 (Malla Nueva) / 0410 (Malla Antigua) Ingeniería Civil Plan Común
    CarreraPlanEntity(
        id = "0406_Diurna_NUEVA",
        codigoCarrera = "0406",
        nombre = "Ingeniería Civil Plan Común",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 0406 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI010", "IIN100", "CSJ100",
            "MAT023", "FIS120", "QUI021", "HRW101",
            "MAT024", "FIS130", "ILN250"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "0410_Diurna_ANTIGUA",
        codigoCarrera = "0410",
        nombre = "Ingeniería Civil Plan Común",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 0410 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "HRW101", "CSJ100",
            "MAT023", "FIS120", "MAT024", "FIS130"
        ),
        dataJson = ""
    ),

    // Plan 6050 (Malla Nueva) / 6008 (Malla Antigua) Ingeniería Civil Industrial
    CarreraPlanEntity(
        id = "6050_Diurna_NUEVA",
        codigoCarrera = "6050",
        nombre = "Ingeniería Civil Industrial",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 6050 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "ILN100", "IIN100", "CSJ100",
            "MAT023", "FIS120", "ILN210", "ILN220", "HRW101",
            "MAT024", "FIS130", "ILN230", "ILN250", "ILN260",
            "MAT060", "ILN251", "ILN310", "ILN320", "INF239",
            "ILN252", "ILN330", "ILN340", "ILN350", "ILN370",
            "ILN360", "ILN380", "ILN390", "INF399"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "6008_Diurna_ANTIGUA",
        codigoCarrera = "6008",
        nombre = "Ingeniería Civil Industrial",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 6008 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "ILN100", "HRW101",
            "MAT023", "FIS120", "ILN210", "ILN220", "CSJ100",
            "MAT024", "FIS130", "ILN230", "ILN250", "ILN260"
        ),
        dataJson = ""
    ),

    // Plan 2106 (Malla Nueva) / 2111 (Malla Antigua) Ingeniería Civil Electrónica
    CarreraPlanEntity(
        id = "2106_Diurna_NUEVA",
        codigoCarrera = "2106",
        nombre = "Ingeniería Civil Electrónica",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 2106 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "ELO100", "ELO102", "CSJ100",
            "MAT023", "FIS120", "ELO211", "ELO212", "HRW101",
            "MAT024", "FIS130", "ELO241", "ELO270", "ELO328",
            "MAT060", "ELO310", "ELO320", "ELO330", "ELO340",
            "ILN250", "ELO350", "ELO390", "INF399"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "2111_Diurna_ANTIGUA",
        codigoCarrera = "2111",
        nombre = "Ingeniería Civil Electrónica",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 2111 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "ELO100", "HRW101",
            "MAT023", "FIS120", "ELO211", "ELO212", "CSJ100",
            "MAT024", "FIS130", "ELO241", "ELO270", "ELO328"
        ),
        dataJson = ""
    ),

    // Plan 2308 (Malla Nueva) / 2311 (Malla Antigua) Ingeniería Civil Eléctrica
    CarreraPlanEntity(
        id = "2308_Diurna_NUEVA",
        codigoCarrera = "2308",
        nombre = "Ingeniería Civil Eléctrica",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 2308 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "ELI100", "IIN100", "CSJ100",
            "MAT023", "FIS120", "ELO211", "ELI110", "HRW101",
            "MAT024", "FIS130", "ELO212", "ELI210", "ELI220",
            "MAT060", "ELI230", "ELI310", "ELI320", "ELI330",
            "ILN250", "ELO270", "ELO340", "ELI390", "INF399"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "2311_Diurna_ANTIGUA",
        codigoCarrera = "2311",
        nombre = "Ingeniería Civil Eléctrica",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 2311 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "ELI100", "HRW101",
            "MAT023", "FIS120", "ELO211", "ELI110", "CSJ100",
            "MAT024", "FIS130", "ELO212", "ELI210", "ELI220"
        ),
        dataJson = ""
    ),

    // Plan 4605 (Malla Nueva) / 4116 (Malla Antigua) Ingeniería Civil Mecánica
    CarreraPlanEntity(
        id = "4605_Diurna_NUEVA",
        codigoCarrera = "4605",
        nombre = "Ingeniería Civil Mecánica",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 4605 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "MEC101", "MEC102", "CSJ100",
            "MAT023", "FIS120", "MEC201", "MEC211", "HRW101",
            "MAT024", "FIS130", "MEC221", "MEC231", "MEC241",
            "MAT060", "MEC311", "MEC321", "MEC331", "MEC341",
            "ILN250", "MEC351", "MEC390", "INF399"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "4116_Diurna_ANTIGUA",
        codigoCarrera = "4116",
        nombre = "Ingeniería Civil Mecánica",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 4116 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "MEC101", "HRW101",
            "MAT023", "FIS120", "MEC201", "MEC211", "CSJ100",
            "MAT024", "FIS130", "MEC221", "MEC231", "MEC241"
        ),
        dataJson = ""
    ),

    // Plan 5119 (Malla Nueva) / 5117 (Malla Antigua) Ingeniería Civil Química
    CarreraPlanEntity(
        id = "5119_Diurna_NUEVA",
        codigoCarrera = "5119",
        nombre = "Ingeniería Civil Química",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 5119 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "QUI110", "CSJ100",
            "MAT023", "FIS120", "QUI120", "QUI210", "HRW101",
            "MAT024", "FIS130", "QUI220", "IBT210", "ILN250",
            "MAT060", "IBT310", "IBT390", "INF399"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "5117_Diurna_ANTIGUA",
        codigoCarrera = "5117",
        nombre = "Ingeniería Civil Química",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 5117 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "QUI110", "HRW101",
            "MAT023", "FIS120", "QUI120", "QUI210", "CSJ100",
            "MAT024", "FIS130", "QUI220", "IBT210", "ILN250"
        ),
        dataJson = ""
    ),

    // Plan 1009 (Malla Nueva) / 1014 (Malla Antigua) Ingeniería Civil Matemática
    CarreraPlanEntity(
        id = "1009_Diurna_NUEVA",
        codigoCarrera = "1009",
        nombre = "Ingeniería Civil Matemática",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 1009 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "MAT210", "IIN100", "CSJ100",
            "MAT023", "FIS120", "MAT220", "MAT230", "HRW101",
            "MAT024", "FIS130", "MAT260", "MAT270", "INF130",
            "MAT060", "MAT310", "MAT320", "MAT350", "INF285",
            "ILN250", "MAT390", "INF399"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "1014_Diurna_ANTIGUA",
        codigoCarrera = "1014",
        nombre = "Ingeniería Civil Matemática",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 1014 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "MAT210", "QUI021", "HRW101",
            "MAT023", "FIS120", "MAT220", "MAT230", "CSJ100",
            "MAT024", "FIS130", "MAT260", "MAT270", "INF130"
        ),
        dataJson = ""
    ),

    // Plan 12525 (Malla Nueva) Ingeniería Civil en Biotecnología
    CarreraPlanEntity(
        id = "12525_Diurna_NUEVA",
        codigoCarrera = "12525",
        nombre = "Ingeniería Civil en Biotecnología",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 12525 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "IBT100", "CSJ100",
            "MAT023", "FIS120", "IBT210", "IBT220", "HRW101",
            "MAT024", "QUI110", "IBT310", "IBT320", "ILN250",
            "MAT060", "IBT390", "INF399"
        ),
        dataJson = ""
    ),

    // Plan 11224 (Malla Nueva) / 11223 (Malla Antigua) Ingeniería Civil Física
    CarreraPlanEntity(
        id = "11224_Diurna_NUEVA",
        codigoCarrera = "11224",
        nombre = "Ingeniería Civil Física",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 11224 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "FIS120", "IIN100", "CSJ100",
            "MAT023", "FIS130", "FIS140", "FIS210", "HRW101",
            "MAT024", "FIS220", "FIS230", "FIS240", "ELO211",
            "MAT060", "FIS310", "FIS320", "FIS390", "INF399"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "11223_Diurna_ANTIGUA",
        codigoCarrera = "11223",
        nombre = "Ingeniería Civil Física",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 11223 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "FIS120", "QUI021", "HRW101",
            "MAT023", "FIS130", "FIS140", "FIS210", "CSJ100",
            "MAT024", "FIS220", "FIS230", "FIS240", "ELO211"
        ),
        dataJson = ""
    ),

    // Plan 1125 (Malla Nueva) / 1115 (Malla Antigua) Ingeniería Civil (Obras Civiles)
    CarreraPlanEntity(
        id = "1125_Diurna_NUEVA",
        codigoCarrera = "1125",
        nombre = "Ingeniería Civil",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 1125 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "CIV100", "MEC102", "CSJ100",
            "MAT023", "FIS120", "CIV210", "CIV220", "HRW101",
            "MAT024", "FIS130", "CIV230", "CIV240", "MEC231",
            "MAT060", "CIV310", "CIV320", "CIV330", "CIV340",
            "ILN250", "CIV350", "CIV390", "INF399"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "1115_Diurna_ANTIGUA",
        codigoCarrera = "1115",
        nombre = "Ingeniería Civil",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 1115 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "CIV100", "QUI021", "HRW101",
            "MAT023", "FIS120", "CIV210", "CIV220", "CSJ100",
            "MAT024", "FIS130", "CIV230", "CIV240", "MEC231"
        ),
        dataJson = ""
    ),

    // Plan 5408 (Malla Nueva) / 5405 (Malla Antigua) Ingeniería Civil Metalúrgica
    CarreraPlanEntity(
        id = "5408_Diurna_NUEVA",
        codigoCarrera = "5408",
        nombre = "Ingeniería Civil Metalúrgica",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 5408 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "MET100", "CSJ100",
            "MAT023", "FIS120", "MET210", "MET220", "HRW101",
            "MAT024", "FIS130", "MEC241", "ILN250", "MET310"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "5405_Diurna_ANTIGUA",
        codigoCarrera = "5405",
        nombre = "Ingeniería Civil Metalúrgica",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 5405 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "MET100", "HRW101",
            "MAT023", "FIS120", "MET210", "MET220", "CSJ100",
            "MAT024", "FIS130", "MEC241", "ILN250", "MET310"
        ),
        dataJson = ""
    ),

    // Plan 5614 (Malla Nueva) / 5613 (Malla Antigua) Ingeniería Civil de Minas
    CarreraPlanEntity(
        id = "5614_Diurna_NUEVA",
        codigoCarrera = "5614",
        nombre = "Ingeniería Civil de Minas",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 5614 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "MET100", "CSJ100",
            "MAT023", "FIS120", "CIV210", "CIV220", "HRW101",
            "MAT024", "FIS130", "MEC241", "ILN250", "CIV310"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "5613_Diurna_ANTIGUA",
        codigoCarrera = "5613",
        nombre = "Ingeniería Civil de Minas",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 5613 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "MET100", "HRW101",
            "MAT023", "FIS120", "CIV210", "CIV220", "CSJ100",
            "MAT024", "FIS130", "MEC241", "ILN250", "CIV310"
        ),
        dataJson = ""
    ),

    // Plan 8419 (Malla Nueva) / 8418 (Malla Antigua) Ingeniería Civil Ambiental
    CarreraPlanEntity(
        id = "8419_Diurna_NUEVA",
        codigoCarrera = "8419",
        nombre = "Ingeniería Civil Ambiental",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 8419 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "AMB100", "CSJ100",
            "MAT023", "FIS120", "CIV210", "AMB210", "HRW101",
            "MAT024", "FIS130", "AMB220", "QUI110", "ILN250",
            "MAT060", "AMB310", "AMB390", "INF399"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "8418_Diurna_ANTIGUA",
        codigoCarrera = "8418",
        nombre = "Ingeniería Civil Ambiental",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 8418 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "QUI010", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "QUI021", "AMB100", "HRW101",
            "MAT023", "FIS120", "CIV210", "AMB210", "CSJ100",
            "MAT024", "FIS130", "AMB220", "QUI110", "ILN250"
        ),
        dataJson = ""
    ),

    // Plan 6607 (Malla Nueva) / 6606 (Malla Antigua) Ingeniería Comercial
    CarreraPlanEntity(
        id = "6607_Diurna_NUEVA",
        codigoCarrera = "6607",
        nombre = "Ingeniería Comercial",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 6607 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "MAT022", "IWI131", "ICN100", "ICN110", "HRW100", "DEW100",
            "MAT023", "MAT060", "ICN210", "ICN220", "ICN230", "CSJ100",
            "ILN250", "ILN251", "ILN252", "ICN310", "ICN320", "ICN330",
            "ICN340", "ICN350", "ICN390", "INF399"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "6606_Comercial_Diurna_ANTIGUA",
        codigoCarrera = "6606",
        nombre = "Ingeniería Comercial",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 6606 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "MAT022", "IWI131", "ICN100", "ICN110", "HRW100", "DEW100",
            "MAT023", "MAT060", "ICN210", "ICN220", "ICN230", "CSJ100",
            "ILN250", "ILN251", "ILN252", "ICN310", "ICN320", "ICN330"
        ),
        dataJson = ""
    ),

    // Plan 7920 (Malla Nueva) Licenciatura en Física
    CarreraPlanEntity(
        id = "7920_Diurna_NUEVA",
        codigoCarrera = "7920",
        nombre = "Licenciatura en Física",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 7920 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "MAT022", "MAT023", "MAT024", "FIS100", "FIS110", "FIS120", "FIS130", "FIS140",
            "FIS210", "FIS220", "FIS230", "FIS240", "FIS310", "FIS320", "FIS330", "FIS390", "IWI131"
        ),
        dataJson = ""
    ),

    // Plan 8720 (Malla Nueva) Licenciatura en Astrofísica
    CarreraPlanEntity(
        id = "8720_Diurna_NUEVA",
        codigoCarrera = "8720",
        nombre = "Licenciatura en Astrofísica",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 8720 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "MAT022", "MAT023", "MAT024", "FIS100", "FIS110", "FIS120", "FIS130", "FIS140",
            "AST100", "AST110", "AST210", "AST220", "AST310", "AST320", "IWI131"
        ),
        dataJson = ""
    ),

    // Plan 8814 (Malla Nueva) Licenciatura en Matemática
    CarreraPlanEntity(
        id = "8814_Diurna_NUEVA",
        codigoCarrera = "8814",
        nombre = "Licenciatura en Matemática",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 8814 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "MAT022", "MAT023", "MAT024", "MAT210", "MAT220", "MAT230", "MAT260", "MAT270",
            "MAT310", "MAT320", "MAT330", "MAT340", "MAT350", "MAT390", "FIS100", "FIS110", "IWI131"
        ),
        dataJson = ""
    ),

    // Plan 4425 (Malla Nueva) / 4420 (Malla Antigua) Ingeniería en Diseño de Productos
    CarreraPlanEntity(
        id = "4425_Diurna_NUEVA",
        codigoCarrera = "4425",
        nombre = "Ingeniería en Diseño de Productos",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 4425 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "DIS100", "DIS110", "CSJ100",
            "DIS210", "DIS220", "DIS230", "DIS240", "HRW101"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "4420_Diurna_ANTIGUA",
        codigoCarrera = "4420",
        nombre = "Ingeniería en Diseño de Productos",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 4420 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "MAT021", "FIS100", "IWI131", "HRW100", "DEW100",
            "MAT022", "FIS110", "DIS100", "DIS110", "CSJ100",
            "DIS210", "DIS220", "DIS230", "DIS240"
        ),
        dataJson = ""
    ),

    // Plan 2920 (Malla Nueva) Ingeniería en Informática
    CarreraPlanEntity(
        id = "2920_Diurna_NUEVA",
        codigoCarrera = "2920",
        nombre = "Ingeniería en Informática",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 2920 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "INF115", "INF130", "INF134", "INF140", "INF221", "INF236", "INF239", "INF246", "INF253",
            "INF280", "INF285", "MAT021", "MAT022", "FIS100", "IWI131", "HRW100", "DEW100"
        ),
        dataJson = ""
    ),

    // Plan 1720 (Malla Nueva) / 1703 (Malla Antigua) Técnico Universitario en Informática
    CarreraPlanEntity(
        id = "1720_Diurna_NUEVA",
        codigoCarrera = "1720",
        nombre = "Técnico Universitario en Informática",
        jornada = "Diurna",
        tipoMalla = "NUEVA",
        nombreMalla = "Plan de Estudio 1720 (Malla Nueva)",
        totalMenciones = 1,
        siglas = listOf(
            "INF115", "INF130", "INF134", "INF221", "INF236", "INF280", "MAT021", "IWI131", "HRW100", "DEW100"
        ),
        dataJson = ""
    ),
    CarreraPlanEntity(
        id = "1703_Diurna_ANTIGUA",
        codigoCarrera = "1703",
        nombre = "Técnico Universitario en Informática",
        jornada = "Diurna",
        tipoMalla = "ANTIGUA",
        nombreMalla = "Plan de Estudio 1703 (Malla Antigua)",
        totalMenciones = 1,
        siglas = listOf(
            "INF115", "INF130", "INF134", "INF221", "INF236", "MAT021", "IWI131", "HRW100", "DEW100"
        ),
        dataJson = ""
    )
)

// -------------------------------------------------------------
// CATÁLOGO BASE DE ASIGNATURAS USM (TODOS LOS DEPARTAMENTOS)
// -------------------------------------------------------------
val DEFAULT_ASIGNATURAS_BASE = listOf(
    // Departamento de Matemática
    AsignaturaEntity("MAT021", "Matemática I", 5, "Departamento de Matemática", horasTeoricas = 4, horasPracticas = 2, horasAyudantias = 2),
    AsignaturaEntity("MAT021T", "Matemática I (Taller)", 0, "Departamento de Matemática", horasPracticas = 2),
    AsignaturaEntity("MAT021Y", "Matemática I (Ayudantía)", 0, "Departamento de Matemática", horasAyudantias = 2),
    AsignaturaEntity("MAT022", "Matemática II", 5, "Departamento de Matemática", requisitos = listOf("MAT021"), horasTeoricas = 4, horasPracticas = 2, horasAyudantias = 2),
    AsignaturaEntity("MAT022T", "Matemática II (Taller)", 0, "Departamento de Matemática", horasPracticas = 2),
    AsignaturaEntity("MAT022Y", "Matemática II (Ayudantía)", 0, "Departamento de Matemática", horasAyudantias = 2),
    AsignaturaEntity("MAT023", "Matemática III", 4, "Departamento de Matemática", requisitos = listOf("MAT022"), horasTeoricas = 3, horasPracticas = 2),
    AsignaturaEntity("MAT024", "Matemática IV", 4, "Departamento de Matemática", requisitos = listOf("MAT023"), horasTeoricas = 3, horasPracticas = 2),
    AsignaturaEntity("MAT060", "Álgebra y Geometría", 6, "Departamento de Matemática"),
    AsignaturaEntity("MAT060T", "Álgebra y Geometría (Taller)", 0, "Departamento de Matemática", horasPracticas = 2),
    AsignaturaEntity("MAT060Y", "Álgebra y Geometría (Ayudantía)", 0, "Departamento de Matemática", horasAyudantias = 2),
    AsignaturaEntity("MAT070", "Introducción al Cálculo", 6, "Departamento de Matemática"),
    AsignaturaEntity("MAT070T", "Introducción al Cálculo (Taller)", 0, "Departamento de Matemática", horasPracticas = 2),
    AsignaturaEntity("MAT070Y", "Introducción al Cálculo (Ayudantía)", 0, "Departamento de Matemática", horasAyudantias = 2),
    AsignaturaEntity("MAT210", "Álgebra Lineal", 4, "Departamento de Matemática", requisitos = listOf("MAT022")),
    AsignaturaEntity("MAT220", "Análisis Real", 4, "Departamento de Matemática", requisitos = listOf("MAT023")),
    AsignaturaEntity("MAT230", "Ecuaciones Diferenciales", 4, "Departamento de Matemática", requisitos = listOf("MAT024")),
    AsignaturaEntity("MAT260", "Probabilidades y Estadística", 4, "Departamento de Matemática", requisitos = listOf("MAT023")),
    AsignaturaEntity("MAT270", "Optimización Matemática", 4, "Departamento de Matemática", requisitos = listOf("MAT210")),
    AsignaturaEntity("MAT310", "Análisis Complejo", 4, "Departamento de Matemática", requisitos = listOf("MAT220")),
    AsignaturaEntity("MAT320", "Geometría Diferencial", 4, "Departamento de Matemática", requisitos = listOf("MAT220")),
    AsignaturaEntity("MAT330", "Topología General", 4, "Departamento de Matemática", requisitos = listOf("MAT220")),
    AsignaturaEntity("MAT340", "Teoría de Números", 3, "Departamento de Matemática", requisitos = listOf("MAT210")),
    AsignaturaEntity("MAT350", "Métodos Numéricos Avanzados", 4, "Departamento de Matemática", requisitos = listOf("MAT024")),
    AsignaturaEntity("MAT390", "Seminario de Titulación Matemática", 6, "Departamento de Matemática"),

    // Departamento de Física
    AsignaturaEntity("FIS100", "Introducción a la Física", 3, "Departamento de Física", horasTeoricas = 3, horasLaboratorios = 2),
    AsignaturaEntity("FIS100T", "Introducción a la Física (Taller)", 0, "Departamento de Física", horasPracticas = 2),
    AsignaturaEntity("FIS100L", "Introducción a la Física (Laboratorio)", 0, "Departamento de Física", horasLaboratorios = 2),
    AsignaturaEntity("FIS110", "Física General I", 4, "Departamento de Física", requisitos = listOf("FIS100", "MAT021"), horasTeoricas = 3, horasLaboratorios = 2),
    AsignaturaEntity("FIS120", "Física General II", 4, "Departamento de Física", requisitos = listOf("FIS110", "MAT022"), horasTeoricas = 3, horasLaboratorios = 2),
    AsignaturaEntity("FIS130", "Física General III", 4, "Departamento de Física", requisitos = listOf("FIS120", "MAT023"), horasTeoricas = 3, horasLaboratorios = 2),
    AsignaturaEntity("FIS140", "Física General IV", 3, "Departamento de Física", requisitos = listOf("FIS130")),
    AsignaturaEntity("FIS210", "Mecánica Clásica", 4, "Departamento de Física", requisitos = listOf("FIS110", "MAT023")),
    AsignaturaEntity("FIS220", "Electromagnetismo I", 4, "Departamento de Física", requisitos = listOf("FIS120", "MAT024")),
    AsignaturaEntity("FIS230", "Termodinámica Estadística", 4, "Departamento de Física", requisitos = listOf("FIS130")),
    AsignaturaEntity("FIS240", "Mecánica Cuántica I", 4, "Departamento de Física", requisitos = listOf("FIS210")),
    AsignaturaEntity("FIS310", "Mecánica Cuántica II", 4, "Departamento de Física", requisitos = listOf("FIS240")),
    AsignaturaEntity("FIS320", "Física del Estado Sólido", 4, "Departamento de Física", requisitos = listOf("FIS240")),
    AsignaturaEntity("FIS330", "Física Nuclear y Partículas", 4, "Departamento de Física", requisitos = listOf("FIS310")),
    AsignaturaEntity("FIS390", "Seminario de Titulación Física", 6, "Departamento de Física"),

    // Astrofísica
    AsignaturaEntity("AST100", "Introducción a la Astronomía", 3, "Departamento de Física"),
    AsignaturaEntity("AST110", "Astrofísica General", 3, "Departamento de Física", requisitos = listOf("FIS110")),
    AsignaturaEntity("AST210", "Astrofísica Estelar", 4, "Departamento de Física", requisitos = listOf("FIS130")),
    AsignaturaEntity("AST220", "Astrofísica Galáctica y Extragaláctica", 4, "Departamento de Física", requisitos = listOf("AST210")),
    AsignaturaEntity("AST310", "Cosmología", 4, "Departamento de Física", requisitos = listOf("AST220")),
    AsignaturaEntity("AST320", "Instrumentación Astronómica", 3, "Departamento de Física"),

    // Departamento de Informática
    AsignaturaEntity("IWI131", "Programación", 3, "Departamento de Informática", horasTeoricas = 2, horasLaboratorios = 2),
    AsignaturaEntity("INF115", "Estructuras Discretas", 3, "Departamento de Informática", requisitos = listOf("MAT021")),
    AsignaturaEntity("INF130", "Estructuras de Datos y Algoritmos", 4, "Departamento de Informática", requisitos = listOf("IWI131")),
    AsignaturaEntity("INF134", "Estructuras de Datos", 4, "Departamento de Informática", requisitos = listOf("IWI131")),
    AsignaturaEntity("INF140", "Algoritmos y Complejidad", 4, "Departamento de Informática", requisitos = listOf("INF130", "INF115")),
    AsignaturaEntity("INF221", "Estructura de Computadores", 3, "Departamento de Informática", requisitos = listOf("IWI131")),
    AsignaturaEntity("INF225", "Arquitectura y Organización de Computadores", 4, "Departamento de Informática", requisitos = listOf("INF221")),
    AsignaturaEntity("INF236", "Análisis y Diseño de Software", 3, "Departamento de Informática", requisitos = listOf("INF130")),
    AsignaturaEntity("INF239", "Bases de Datos", 3, "Departamento de Informática", requisitos = listOf("INF130")),
    AsignaturaEntity("INF246", "Sistemas Operativos", 4, "Departamento de Informática", requisitos = listOf("INF225")),
    AsignaturaEntity("INF253", "Lenguajes de Programación", 3, "Departamento de Informática", requisitos = listOf("INF140")),
    AsignaturaEntity("INF280", "Estadística Computacional", 3, "Departamento de Informática", requisitos = listOf("MAT023")),
    AsignaturaEntity("INF285", "Computación Científica", 3, "Departamento de Informática", requisitos = listOf("MAT024", "INF130")),
    AsignaturaEntity("INF320", "Redes de Computadores", 3, "Departamento de Informática", requisitos = listOf("INF246")),
    AsignaturaEntity("INF323", "Sistemas Distribuidos", 3, "Departamento de Informática", requisitos = listOf("INF320")),
    AsignaturaEntity("INF330", "Ingeniería de Software", 3, "Departamento de Informática", requisitos = listOf("INF236")),
    AsignaturaEntity("INF331", "Arquitectura de Software", 3, "Departamento de Informática", requisitos = listOf("INF330")),
    AsignaturaEntity("INF343", "Inteligencia Artificial", 3, "Departamento de Informática", requisitos = listOf("INF140", "INF280")),
    AsignaturaEntity("INF350", "Gestión de Proyectos de TI", 3, "Departamento de Informática", requisitos = listOf("INF330")),
    AsignaturaEntity("INF352", "Seguridad en Sistemas de Información", 3, "Departamento de Informática", requisitos = listOf("INF320")),
    AsignaturaEntity("INF360", "Interacción Humano Computador", 3, "Departamento de Informática", requisitos = listOf("INF236")),
    AsignaturaEntity("INF370", "Ciencia de Datos", 3, "Departamento de Informática", requisitos = listOf("INF280", "INF239")),
    AsignaturaEntity("INF376", "Taller de Desarrollo de Software", 4, "Departamento de Informática", requisitos = listOf("INF330", "INF239")),
    AsignaturaEntity("INF380", "Aprendizaje Automático", 3, "Departamento de Informática", requisitos = listOf("INF343")),
    AsignaturaEntity("INF381", "Procesamiento de Lenguaje Natural", 3, "Departamento de Informática", requisitos = listOf("INF343")),
    AsignaturaEntity("INF382", "Minería de Datos", 3, "Departamento de Informática", requisitos = listOf("INF239", "INF280")),
    AsignaturaEntity("INF385", "Criptografía y Ciberseguridad", 3, "Departamento de Informática", requisitos = listOf("INF115", "INF320")),
    AsignaturaEntity("INF390", "Memoria de Titulación I", 5, "Departamento de Informática"),
    AsignaturaEntity("INF395", "Memoria de Titulación II", 8, "Departamento de Informática", requisitos = listOf("INF390")),
    AsignaturaEntity("INF399", "Práctica Profesional", 0, "Departamento de Informática"),

    // Departamento de Industrias
    AsignaturaEntity("ILN100", "Introducción a la Ingeniería Industrial", 2, "Departamento de Industrias"),
    AsignaturaEntity("ILN210", "Optimización I", 3, "Departamento de Industrias", requisitos = listOf("MAT022")),
    AsignaturaEntity("ILN220", "Contabilidad y Costos", 3, "Departamento de Industrias"),
    AsignaturaEntity("ILN230", "Optimización II", 3, "Departamento de Industrias", requisitos = listOf("ILN210")),
    AsignaturaEntity("ILN250", "Economía I-A", 3, "Departamento de Industrias", requisitos = listOf("MAT022")),
    AsignaturaEntity("ILN251", "Microeconomía", 3, "Departamento de Industrias", requisitos = listOf("ILN250")),
    AsignaturaEntity("ILN252", "Macroeconomía", 3, "Departamento de Industrias", requisitos = listOf("ILN250")),
    AsignaturaEntity("ILN260", "Marketing", 3, "Departamento de Industrias"),
    AsignaturaEntity("ILN310", "Finanzas Corporativas", 3, "Departamento de Industrias", requisitos = listOf("ILN220")),
    AsignaturaEntity("ILN320", "Gestión de Operaciones", 3, "Departamento de Industrias", requisitos = listOf("ILN230")),
    AsignaturaEntity("ILN330", "Gestión Estratégica", 3, "Departamento de Industrias"),
    AsignaturaEntity("ILN340", "Evaluación de Proyectos", 3, "Departamento de Industrias", requisitos = listOf("ILN310")),
    AsignaturaEntity("ILN350", "Cadena de Suministro y Logística", 3, "Departamento de Industrias"),
    AsignaturaEntity("ILN360", "Dirección de Empresas", 3, "Departamento de Industrias"),
    AsignaturaEntity("ILN370", "Simulación de Procesos", 3, "Departamento de Industrias", requisitos = listOf("MAT060")),
    AsignaturaEntity("ILN380", "Innovación y Emprendimiento", 3, "Departamento de Industrias"),
    AsignaturaEntity("ILN390", "Memoria de Titulación Industrial", 6, "Departamento de Industrias"),

    // Departamento de Electrónica
    AsignaturaEntity("ELO100", "Introducción a la Ingeniería Electrónica", 2, "Departamento de Electrónica"),
    AsignaturaEntity("ELO102", "Circuitos Digitales", 3, "Departamento de Electrónica"),
    AsignaturaEntity("ELO211", "Circuitos Eléctricos", 4, "Departamento de Electrónica", requisitos = listOf("FIS120", "MAT023")),
    AsignaturaEntity("ELO212", "Señales y Sistemas", 3, "Departamento de Electrónica", requisitos = listOf("ELO211")),
    AsignaturaEntity("ELO241", "Electrónica Analógica", 4, "Departamento de Electrónica", requisitos = listOf("ELO211")),
    AsignaturaEntity("ELO270", "Sistemas de Control", 4, "Departamento de Electrónica", requisitos = listOf("ELO212")),
    AsignaturaEntity("ELO310", "Procesamiento Digital de Señales", 3, "Departamento de Electrónica", requisitos = listOf("ELO212")),
    AsignaturaEntity("ELO320", "Comunicaciones Digitales", 3, "Departamento de Electrónica", requisitos = listOf("ELO212")),
    AsignaturaEntity("ELO322", "Redes de Telecomunicaciones", 3, "Departamento de Electrónica"),
    AsignaturaEntity("ELO328", "Diseño de Sistemas Digitales", 4, "Departamento de Electrónica", requisitos = listOf("ELO102")),
    AsignaturaEntity("ELO330", "Microcontroladores y Sistemas Embebidos", 4, "Departamento de Electrónica", requisitos = listOf("ELO328")),
    AsignaturaEntity("ELO340", "Electrónica de Potencia", 4, "Departamento de Electrónica", requisitos = listOf("ELO241")),
    AsignaturaEntity("ELO350", "Robótica y Automatización", 3, "Departamento de Electrónica", requisitos = listOf("ELO270")),
    AsignaturaEntity("ELO390", "Memoria de Titulación Electrónica", 6, "Departamento de Electrónica"),

    // Departamento de Electricidad
    AsignaturaEntity("ELI100", "Introducción a la Ingeniería Eléctrica", 2, "Departamento de Electricidad"),
    AsignaturaEntity("ELI110", "Electromagnetismo Aplicado", 3, "Departamento de Electricidad", requisitos = listOf("FIS120")),
    AsignaturaEntity("ELI210", "Sistemas Eléctricos de Potencia", 4, "Departamento de Electricidad", requisitos = listOf("ELO211")),
    AsignaturaEntity("ELI220", "Máquinas Eléctricas", 4, "Departamento de Electricidad", requisitos = listOf("ELI110")),
    AsignaturaEntity("ELI230", "Líneas de Transmisión", 3, "Departamento de Electricidad", requisitos = listOf("ELI210")),
    AsignaturaEntity("ELI310", "Protecciones Eléctricas", 3, "Departamento de Electricidad", requisitos = listOf("ELI210")),
    AsignaturaEntity("ELI320", "Mercados Eléctricos y Regulación", 3, "Departamento de Electricidad"),
    AsignaturaEntity("ELI330", "Energías Renovables", 3, "Departamento de Electricidad"),
    AsignaturaEntity("ELI390", "Memoria de Titulación Eléctrica", 6, "Departamento de Electricidad"),

    // Departamento de Telemática
    AsignaturaEntity("TEL100", "Introducción a la Telemática", 2, "Departamento de Telemática"),
    AsignaturaEntity("TEL101", "Protocolos de Comunicación", 3, "Departamento de Telemática", requisitos = listOf("IWI131")),
    AsignaturaEntity("TEL211", "Enrutamiento y Conmutación", 4, "Departamento de Telemática", requisitos = listOf("TEL101")),
    AsignaturaEntity("TEL221", "Seguridad en Redes", 3, "Departamento de Telemática", requisitos = listOf("TEL211")),
    AsignaturaEntity("TEL311", "Comunicaciones Inalámbricas y Móviles", 4, "Departamento de Telemática", requisitos = listOf("TEL211")),
    AsignaturaEntity("TEL321", "Servicios y Aplicaciones Telemáticas", 3, "Departamento de Telemática"),
    AsignaturaEntity("TEL331", "Computación en la Nube", 3, "Departamento de Telemática"),
    AsignaturaEntity("TEL390", "Memoria de Titulación Telemática", 6, "Departamento de Telemática"),

    // Departamento de Ingeniería Mecánica
    AsignaturaEntity("MEC101", "Dibujo Técnico para Ingeniería", 2, "Departamento de Ingeniería Mecánica"),
    AsignaturaEntity("MEC102", "Estática", 3, "Departamento de Ingeniería Mecánica", requisitos = listOf("FIS110", "MAT022")),
    AsignaturaEntity("MEC201", "Dinámica", 3, "Departamento de Ingeniería Mecánica", requisitos = listOf("MEC102")),
    AsignaturaEntity("MEC211", "Termodinámica", 4, "Departamento de Ingeniería Mecánica", requisitos = listOf("FIS120")),
    AsignaturaEntity("MEC221", "Mecánica de Fluidos", 4, "Departamento de Ingeniería Mecánica", requisitos = listOf("MEC201")),
    AsignaturaEntity("MEC231", "Mecánica de Materiales", 4, "Departamento de Ingeniería Mecánica", requisitos = listOf("MEC102")),
    AsignaturaEntity("MEC241", "Ciencia de Materiales", 3, "Departamento de Ingeniería Mecánica"),
    AsignaturaEntity("MEC311", "Transferencia de Calor", 4, "Departamento de Ingeniería Mecánica", requisitos = listOf("MEC211")),
    AsignaturaEntity("MEC321", "Elementos de Máquinas", 4, "Departamento de Ingeniería Mecánica", requisitos = listOf("MEC231")),
    AsignaturaEntity("MEC331", "Diseño Mecánico", 4, "Departamento de Ingeniería Mecánica", requisitos = listOf("MEC321")),
    AsignaturaEntity("MEC341", "Sistemas Térmicos", 3, "Departamento de Ingeniería Mecánica", requisitos = listOf("MEC311")),
    AsignaturaEntity("MEC351", "Vibraciones Mecánicas", 3, "Departamento de Ingeniería Mecánica", requisitos = listOf("MEC201")),
    AsignaturaEntity("MEC390", "Memoria de Titulación Mecánica", 6, "Departamento de Ingeniería Mecánica"),

    // Departamento de Obras Civiles
    AsignaturaEntity("CIV100", "Introducción a la Ingeniería Civil", 2, "Departamento de Obras Civiles"),
    AsignaturaEntity("CIV210", "Topografía y Geomensura", 3, "Departamento de Obras Civiles"),
    AsignaturaEntity("CIV220", "Mecánica de Suelos", 4, "Departamento de Obras Civiles", requisitos = listOf("MEC102")),
    AsignaturaEntity("CIV230", "Hidráulica General", 4, "Departamento de Obras Civiles", requisitos = listOf("MEC221")),
    AsignaturaEntity("CIV240", "Análisis Estructural", 4, "Departamento de Obras Civiles", requisitos = listOf("MEC231")),
    AsignaturaEntity("CIV310", "Hormigón Armado", 4, "Departamento de Obras Civiles", requisitos = listOf("CIV240")),
    AsignaturaEntity("CIV320", "Estructuras de Acero", 4, "Departamento de Obras Civiles", requisitos = listOf("CIV240")),
    AsignaturaEntity("CIV330", "Ingeniería Geotécnica", 3, "Departamento de Obras Civiles", requisitos = listOf("CIV220")),
    AsignaturaEntity("CIV340", "Ingeniería de Transportes", 3, "Departamento de Obras Civiles"),
    AsignaturaEntity("CIV350", "Hidrología y Recursos Hídricos", 4, "Departamento de Obras Civiles", requisitos = listOf("CIV230")),
    AsignaturaEntity("CIV390", "Memoria de Titulación Civil", 6, "Departamento de Obras Civiles"),

    // Departamento de Química y Biotecnología
    AsignaturaEntity("QUI010", "Química y Sociedad", 3, "Departamento de Química", horasTeoricas = 3, horasLaboratorios = 1),
    AsignaturaEntity("QUI021", "Química General", 3, "Departamento de Química", requisitos = listOf("QUI010")),
    AsignaturaEntity("QUI110", "Química Orgánica", 4, "Departamento de Química", requisitos = listOf("QUI021")),
    AsignaturaEntity("QUI120", "Fisicoquímica", 4, "Departamento de Química", requisitos = listOf("QUI021", "MAT023")),
    AsignaturaEntity("QUI210", "Química Analítica", 4, "Departamento de Química", requisitos = listOf("QUI021")),
    AsignaturaEntity("QUI220", "Termodinámica Química", 4, "Departamento de Química", requisitos = listOf("QUI120")),
    AsignaturaEntity("IBT100", "Introducción a la Biotecnología", 2, "Departamento de Ingeniería Química y Ambiental"),
    AsignaturaEntity("IBT210", "Bioquímica General", 4, "Departamento de Ingeniería Química y Ambiental", requisitos = listOf("QUI110")),
    AsignaturaEntity("IBT220", "Microbiología Industrial", 4, "Departamento de Ingeniería Química y Ambiental", requisitos = listOf("IBT210")),
    AsignaturaEntity("IBT310", "Ingeniería de Bioprocesos", 4, "Departamento de Ingeniería Química y Ambiental", requisitos = listOf("IBT220")),
    AsignaturaEntity("IBT320", "Genética Molecular", 3, "Departamento de Ingeniería Química y Ambiental", requisitos = listOf("IBT210")),
    AsignaturaEntity("IBT390", "Memoria de Titulación Biotecnología", 6, "Departamento de Ingeniería Química y Ambiental"),

    // Metalurgia y Minería
    AsignaturaEntity("MET100", "Introducción a la Metalurgia", 2, "Departamento de Ingeniería Metalúrgica y Materiales"),
    AsignaturaEntity("MET210", "Metalurgia Extractiva", 4, "Departamento de Ingeniería Metalúrgica y Materiales"),
    AsignaturaEntity("MET220", "Metalurgia Física", 4, "Departamento de Ingeniería Metalúrgica y Materiales"),
    AsignaturaEntity("MET310", "Procesos Piro e Hidrometalúrgicos", 4, "Departamento de Ingeniería Metalúrgica y Materiales"),
    AsignaturaEntity("MIN100", "Introducción a la Minería", 2, "Departamento de Ingeniería Metalúrgica y Materiales"),
    AsignaturaEntity("MIN210", "Geología General", 3, "Departamento de Ingeniería Metalúrgica y Materiales"),
    AsignaturaEntity("MIN220", "Explotación de Minas", 4, "Departamento de Ingeniería Metalúrgica y Materiales"),

    // Ingeniería Comercial
    AsignaturaEntity("ICN100", "Introducción a la Administración", 2, "Departamento de Ingeniería Comercial"),
    AsignaturaEntity("ICN110", "Contabilidad Financiera", 3, "Departamento de Ingeniería Comercial"),
    AsignaturaEntity("ICN210", "Comportamiento Organizacional", 3, "Departamento de Ingeniería Comercial"),
    AsignaturaEntity("ICN220", "Finanzas I", 4, "Departamento de Ingeniería Comercial", requisitos = listOf("ICN110")),
    AsignaturaEntity("ICN230", "Marketing Estratégico", 3, "Departamento de Ingeniería Comercial"),
    AsignaturaEntity("ICN310", "Econometría", 4, "Departamento de Ingeniería Comercial", requisitos = listOf("MAT060")),
    AsignaturaEntity("ICN320", "Finanzas II", 4, "Departamento de Ingeniería Comercial", requisitos = listOf("ICN220")),
    AsignaturaEntity("ICN330", "Gestión de Personas", 3, "Departamento de Ingeniería Comercial"),
    AsignaturaEntity("ICN340", "Estrategia Empresarial", 3, "Departamento de Ingeniería Comercial"),
    AsignaturaEntity("ICN350", "Negociación y Liderazgo", 2, "Departamento de Ingeniería Comercial"),
    AsignaturaEntity("ICN390", "Proyecto de Grado Comercial", 6, "Departamento de Ingeniería Comercial"),

    // Arquitectura y Diseño
    AsignaturaEntity("ARQ100", "Taller de Arquitectura I", 5, "Departamento de Arquitectura"),
    AsignaturaEntity("ARQ110", "Geometría y Representación", 3, "Departamento de Arquitectura"),
    AsignaturaEntity("ARQ210", "Taller de Arquitectura II", 5, "Departamento de Arquitectura", requisitos = listOf("ARQ100")),
    AsignaturaEntity("ARQ220", "Historia y Teoría de la Arquitectura", 3, "Departamento de Arquitectura"),
    AsignaturaEntity("ARQ230", "Estructuras y Construcción", 3, "Departamento de Arquitectura"),
    AsignaturaEntity("ARQ310", "Taller de Arquitectura III", 5, "Departamento de Arquitectura", requisitos = listOf("ARQ210")),
    AsignaturaEntity("ARQ320", "Urbanismo y Territorio", 3, "Departamento de Arquitectura"),
    AsignaturaEntity("ARQ330", "Sistemas Constructivos Sostenibles", 3, "Departamento de Arquitectura"),
    AsignaturaEntity("ARQ390", "Proyecto de Título Arquitectura", 8, "Departamento de Arquitectura"),
    AsignaturaEntity("DIS100", "Taller de Diseño I", 4, "Departamento de Diseño de Productos"),
    AsignaturaEntity("DIS110", "Modelado Tridimensional", 3, "Departamento de Diseño de Productos"),
    AsignaturaEntity("DIS210", "Taller de Diseño II", 4, "Departamento de Diseño de Productos"),
    AsignaturaEntity("DIS220", "Ergonomía y Usabilidad", 3, "Departamento de Diseño de Productos"),
    AsignaturaEntity("DIS230", "Materiales y Procesos de Fabricación", 3, "Departamento de Diseño de Productos"),
    AsignaturaEntity("DIS310", "Ecodiseño y Sostenibilidad", 3, "Departamento de Diseño de Productos"),
    AsignaturaEntity("DIS390", "Proyecto de Título Diseño", 6, "Departamento de Diseño de Productos"),

    // Formación Fundamental / Humanísticos / Deportes
    AsignaturaEntity("HRW100", "Educación Física I", 1, "Departamento de Educación Física"),
    AsignaturaEntity("HRW101", "Educación Física II", 1, "Departamento de Educación Física", requisitos = listOf("HRW100")),
    AsignaturaEntity("HRW102", "Educación Física III", 1, "Departamento de Educación Física", requisitos = listOf("HRW101")),
    AsignaturaEntity("DEW100", "Inglés I", 2, "Departamento de Estudios Humanísticos"),
    AsignaturaEntity("DEW101", "Inglés II", 2, "Departamento de Estudios Humanísticos", requisitos = listOf("DEW100")),
    AsignaturaEntity("DEW102", "Inglés III", 2, "Departamento de Estudios Humanísticos", requisitos = listOf("DEW101")),
    AsignaturaEntity("DEW103", "Inglés IV", 2, "Departamento de Estudios Humanísticos", requisitos = listOf("DEW102")),
    AsignaturaEntity("CSJ100", "Formación Ciudadana y Ética", 2, "Departamento de Estudios Humanísticos"),
    AsignaturaEntity("CSJ101", "Pensamiento Crítico", 2, "Departamento de Estudios Humanísticos"),
    AsignaturaEntity("CSJ102", "Liderazgo y Habilidades Blandas", 2, "Departamento de Estudios Humanísticos"),
    AsignaturaEntity("IIN100", "Introducción a la Ingeniería", 2, "Facultad de Ingeniería"),
    AsignaturaEntity("IIN200", "Taller de Innovación y Emprendimiento", 3, "Facultad de Ingeniería")
)

val DEFAULT_PARALELOS_BASE: List<ParaleloEntity> = emptyList()
val DEFAULT_BLOQUES_BASE: List<BloqueHorarioEntity> = emptyList()

val DEFAULT_PROFESORES_BASE = listOf(
    ProfesorEntity(id = "1", name = "Federico Meza", accesibilidadAvg = 4.7, claridadAvg = 4.8, coherenciaAvg = 4.6, dificultadAvg = 3.2, estabilidadAvg = 4.9, gestionTiempoAvg = 4.6, rigorAvg = 4.0, safeScorePromedio = 4.6),
    ProfesorEntity(id = "2", name = "Hernán Astudillo", accesibilidadAvg = 4.5, claridadAvg = 4.6, coherenciaAvg = 4.4, dificultadAvg = 3.5, estabilidadAvg = 4.7, gestionTiempoAvg = 4.4, rigorAvg = 4.2, safeScorePromedio = 4.4),
    ProfesorEntity(id = "3", name = "Carlos Castro", accesibilidadAvg = 4.3, claridadAvg = 4.2, coherenciaAvg = 4.1, dificultadAvg = 3.8, estabilidadAvg = 4.5, gestionTiempoAvg = 4.1, rigorAvg = 4.4, safeScorePromedio = 4.2),
    ProfesorEntity(id = "4", name = "Andrea Vásquez", accesibilidadAvg = 4.6, claridadAvg = 4.7, coherenciaAvg = 4.5, dificultadAvg = 3.0, estabilidadAvg = 4.8, gestionTiempoAvg = 4.5, rigorAvg = 3.8, safeScorePromedio = 4.5),
    ProfesorEntity(id = "5", name = "Marcelo Mendoza", accesibilidadAvg = 4.8, claridadAvg = 4.7, coherenciaAvg = 4.8, dificultadAvg = 3.4, estabilidadAvg = 4.9, gestionTiempoAvg = 4.8, rigorAvg = 4.1, safeScorePromedio = 4.7),
    ProfesorEntity(id = "6", name = "Claudio Meneses", accesibilidadAvg = 4.2, claridadAvg = 4.0, coherenciaAvg = 4.0, dificultadAvg = 4.2, estabilidadAvg = 4.1, gestionTiempoAvg = 4.0, rigorAvg = 4.5, safeScorePromedio = 4.1),
    ProfesorEntity(id = "7", name = "Alfredo Rice", accesibilidadAvg = 4.4, claridadAvg = 4.3, coherenciaAvg = 4.2, dificultadAvg = 3.9, estabilidadAvg = 4.4, gestionTiempoAvg = 4.2, rigorAvg = 4.1, safeScorePromedio = 4.3),
    ProfesorEntity(id = "8", name = "Eduardo Cerpa", accesibilidadAvg = 4.6, claridadAvg = 4.5, coherenciaAvg = 4.6, dificultadAvg = 3.7, estabilidadAvg = 4.7, gestionTiempoAvg = 4.6, rigorAvg = 4.2, safeScorePromedio = 4.5)
)

val DEFAULT_REVIEWS_BASE = listOf(
    ReviewEntity(
        profesorName = "Federico Meza",
        summary = "Excelente profesor. Muy claro para explicar estructuras de datos, siempre dispuesto a responder dudas en horario de consultas.",
        activeTags = listOf("Claro", "Puntual", "Buen feedback", "Accesible"),
        accesibilidad = 5,
        claridadExpositiva = 5,
        coherenciaEvaluativa = 5,
        dificultadPercibida = 3,
        estabilidadEmocional = 5,
        gestionTiempo = 5,
        rigorCalificatorio = 4,
        score = 5
    ),
    ReviewEntity(
        profesorName = "Hernán Astudillo",
        summary = "Muy apasionado por la ingeniería de software y arquitectura. Las clases son dinámicas y los proyectos son muy formativos.",
        activeTags = listOf("Proyectos reales", "Exigente", "Buen material"),
        accesibilidad = 4,
        claridadExpositiva = 5,
        coherenciaEvaluativa = 4,
        dificultadPercibida = 4,
        estabilidadEmocional = 5,
        gestionTiempo = 4,
        rigorCalificatorio = 4,
        score = 5
    ),
    ReviewEntity(
        profesorName = "Marcelo Mendoza",
        summary = "Uno de los mejores profesores de ciencia de datos e IA en Chile. Gran nivel académico y explicaciones impecables.",
        activeTags = listOf("Eminencia", "Clases dinámicas", "Excelente nivel"),
        accesibilidad = 5,
        claridadExpositiva = 5,
        coherenciaEvaluativa = 5,
        dificultadPercibida = 3,
        estabilidadEmocional = 5,
        gestionTiempo = 5,
        rigorCalificatorio = 4,
        score = 5
    )
)

private data class SigaProfAccumulator(
    val id: String,
    val name: String,
    var departamento: String? = null,
    val sedes: MutableSet<String> = mutableSetOf(),
    val siglas: MutableSet<String> = mutableSetOf(),
    val aliases: MutableSet<String> = mutableSetOf(),
    var totalBloques: Int = 0
)
