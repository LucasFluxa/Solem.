package com.example.ui.horario

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.local.entity.AsignaturaEntity
import com.example.data.local.entity.ParaleloEntity
import com.example.ui.components.ContextBadge
import com.example.ui.components.SolemSearchBar
import com.example.ui.theme.SolemAccentAmber
import com.example.ui.theme.SolemAccentCyan
import com.example.ui.theme.SolemAccentEmerald
import com.example.ui.theme.SolemAccentRose
import com.example.ui.theme.SolemBackground
import com.example.ui.theme.SolemBorder
import com.example.ui.theme.SolemPrimaryBlue
import com.example.ui.theme.SolemPrimaryBlueLight
import com.example.ui.theme.SolemSurfaceCard
import com.example.ui.theme.SolemSurfaceVariant
import com.example.ui.theme.SolemTextMuted
import com.example.ui.theme.SolemTextPrimary
import com.example.ui.theme.SolemTextSecondary

data class BloqueSlot(
    val id: Int,
    val label: String,
    val shortLabel: String,
    val horas: String,
    val bloques: List<Int>
)

private val BLOQUES_HORARIOS = listOf(
    BloqueSlot(1, "Bloque 1-2", "1-2", "08:15 - 09:25", listOf(1, 2)),
    BloqueSlot(2, "Bloque 3-4", "3-4", "09:40 - 10:50", listOf(3, 4)),
    BloqueSlot(3, "Bloque 5-6", "5-6", "11:05 - 12:15", listOf(5, 6)),
    BloqueSlot(4, "Bloque 7-8", "7-8", "12:30 - 13:40", listOf(7, 8)),
    BloqueSlot(5, "Bloque 9-10", "9-10", "14:40 - 15:50", listOf(9, 10)),
    BloqueSlot(6, "Bloque 11-12", "11-12", "16:05 - 17:15", listOf(11, 12)),
    BloqueSlot(7, "Bloque 13-14", "13-14", "17:30 - 18:40", listOf(13, 14)),
    BloqueSlot(8, "Bloque 15-16", "15-16", "18:55 - 20:05", listOf(15, 16))
)

private val DIAS_SEMANA = listOf(
    0 to "LUNES",
    1 to "MARTES",
    2 to "MIÉRCOLES",
    3 to "JUEVES",
    4 to "VIERNES",
    5 to "SÁBADO"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioScreen(
    viewModel: HorarioViewModel,
    onOpenSettings: () -> Unit,
    onNavigateToRamos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDay by viewModel.selectedDay.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val topesDeHorario by viewModel.topesDeHorario.collectAsState()
    val userPrefs by viewModel.userPreferences.collectAsState()
    val miHorarioBloques by viewModel.miHorarioBloques.collectAsState()
    val bloquesDelDia by viewModel.bloquesForDay.collectAsState()
    val activeCarreraPlan by viewModel.activeCarreraPlanes.collectAsState()

    val scope = rememberCoroutineScope()
    var showAddModal by remember { mutableStateOf(false) }
    var pendingConflict by remember { mutableStateOf<PendingEnrollConflict?>(null) }
    val searchQuery by viewModel.searchQueryAsignatura.collectAsState()
    var selectedAsignaturaForEnroll by remember { mutableStateOf<AsignaturaEntity?>(null) }
    var selectedDetailClass by remember { mutableStateOf<EnrolledClassBlock?>(null) }

    val careerSiglas = remember(activeCarreraPlan) {
        activeCarreraPlan?.siglas?.toSet() ?: emptySet()
    }

    // Ocultar Sábado a menos que el usuario tenga clases inscritas ese día
    val hasSaturdayClasses = remember(miHorarioBloques) {
        miHorarioBloques.any { it.dia == 5 }
    }

    val activeDias = remember(hasSaturdayClasses) {
        if (hasSaturdayClasses) {
            DIAS_SEMANA
        } else {
            DIAS_SEMANA.filter { it.first < 5 }
        }
    }

    // Ocultar bloques más allá de 11-12 (13-14, 15-16, etc.) a menos que el usuario tenga clases vespertinas
    val maxEnrolledBloque = remember(miHorarioBloques) {
        miHorarioBloques.maxOfOrNull { it.bloque } ?: 0
    }

    val activeBloques = remember(maxEnrolledBloque) {
        if (maxEnrolledBloque > 12) {
            val maxSlotIndex = BLOQUES_HORARIOS.indexOfLast { slot -> slot.bloques.any { it <= maxEnrolledBloque } }
            if (maxSlotIndex >= 5) {
                BLOQUES_HORARIOS.take(maxSlotIndex + 1)
            } else {
                BLOQUES_HORARIOS.take(6)
            }
        } else {
            BLOQUES_HORARIOS.take(6)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(miHorarioBloques, userPrefs.notificacionesClaseActivas, userPrefs.minutosAnticipacionNotificacion) {
        val entityList = miHorarioBloques.map {
            com.example.data.local.entity.BloqueHorarioEntity(
                paraleloId = "${it.campus}_${it.periodo}_${it.sigla}_${it.paralelo}",
                sigla = it.sigla,
                paralelo = it.paralelo,
                campus = it.campus ?: "",
                dia = it.dia,
                bloque = it.bloque,
                profesor = it.profesor,
                sala = it.sala,
                tipo = it.tipo ?: "Cátedra",
                periodo = it.periodo
            )
        }
        val namesMap = miHorarioBloques.associate { it.sigla to it.asignaturaNombre }
        com.example.notification.ClassNotificationHelper.scheduleClassReminders(
            context = context,
            bloques = entityList,
            nombresAsignaturas = namesMap,
            enabled = userPrefs.notificacionesClaseActivas,
            minutesBefore = userPrefs.minutosAnticipacionNotificacion
        )
    }

    LaunchedEffect(activeDias) {
        if (activeDias.none { it.first == selectedDay }) {
            viewModel.selectDay(activeDias.firstOrNull()?.first ?: 0)
        }
    }

    val availableAsignaturas by viewModel.availableAsignaturas.collectAsState()

    Scaffold(
        containerColor = SolemBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddModal = true },
                containerColor = SolemPrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Añadir Ramo", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("add_ramo_fab")
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MI HORARIO USM",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolemPrimaryBlueLight,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = if (viewMode == HorarioViewMode.SEMANAL_MATRIZ) "Horario Semanal" else "Horario por Día",
                        style = MaterialTheme.typography.displayMedium,
                        color = SolemTextPrimary
                    )
                }

                // Switcher entre Horario Semanal y Vista Diaria
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SolemSurfaceCard)
                        .border(1.dp, SolemBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewMode == HorarioViewMode.SEMANAL_MATRIZ) SolemPrimaryBlue else Color.Transparent)
                            .clickable { viewModel.setViewMode(HorarioViewMode.SEMANAL_MATRIZ) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarViewMonth,
                                contentDescription = "Horario Semanal",
                                tint = if (viewMode == HorarioViewMode.SEMANAL_MATRIZ) Color.White else SolemTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Semana",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (viewMode == HorarioViewMode.SEMANAL_MATRIZ) Color.White else SolemTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewMode == HorarioViewMode.DIARIA_DETALLADA) SolemPrimaryBlue else Color.Transparent)
                            .clickable { viewModel.setViewMode(HorarioViewMode.DIARIA_DETALLADA) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ViewDay,
                                contentDescription = "Vista Diaria",
                                tint = if (viewMode == HorarioViewMode.DIARIA_DETALLADA) Color.White else SolemTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Día",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (viewMode == HorarioViewMode.DIARIA_DETALLADA) Color.White else SolemTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Alerta de Topes de Horario (si existen)
            if (topesDeHorario.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SolemAccentAmber.copy(alpha = 0.15f))
                        .border(1.dp, SolemAccentAmber.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Tope de Horario",
                            tint = SolemAccentAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "¡Alerta de Tope de Horario (${topesDeHorario.size})!",
                                style = MaterialTheme.typography.labelMedium,
                                color = SolemAccentAmber,
                                fontWeight = FontWeight.Bold
                            )
                            topesDeHorario.forEach { tope ->
                                val diaNombre = DIAS_SEMANA.find { it.first == tope.dia }?.second ?: "Día ${tope.dia}"
                                val bloqueSlot = BLOQUES_HORARIOS.find { tope.bloque in it.bloques }
                                val bloqueNombre = bloqueSlot?.label ?: "Bloque ${tope.bloque}"
                                val siglas = tope.clases.joinToString(" y ") { "${it.sigla} (P${it.paralelo})" }
                                Text(
                                    text = "• $diaNombre $bloqueNombre: $siglas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            var isLoadingDelay by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(400)
                isLoadingDelay = false
            }

            AnimatedContent(
                targetState = miHorarioBloques.isEmpty(),
                label = "HorarioContentAnimation"
            ) { isEmpty ->
                if (isEmpty) {
                    if (!isLoadingDelay) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SolemSurfaceCard)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(SolemPrimaryBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = SolemPrimaryBlueLight,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tu horario está vacío",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SolemTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Añade los ramos y paralelos para armar tu horario.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextMuted,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { showAddModal = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SolemPrimaryBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Añadir Asignatura a Horario")
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().fillMaxSize())
                    }
                } else {
                    if (viewMode == HorarioViewMode.SEMANAL_MATRIZ) {
                        HorarioSemanalMatrixGrid(
                            miHorarioBloques = miHorarioBloques,
                            topes = topesDeHorario,
                            activeDias = activeDias,
                            activeBloques = activeBloques,
                            onSelectClass = { selectedDetailClass = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // VISTA DIARIA DETALLADA
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                activeDias.forEach { (diaNum, diaNombre) ->
                                    val isSelected = selectedDay == diaNum
                                    val bg = if (isSelected) SolemPrimaryBlue else SolemSurfaceCard
                                    val txt = if (isSelected) Color.White else SolemTextSecondary

                                    val countForThisDay = activeBloques.count { slot ->
                                        miHorarioBloques.any { it.dia == diaNum && it.bloque in slot.bloques }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(bg)
                                            .border(1.dp, if (isSelected) SolemPrimaryBlueLight else SolemBorder, RoundedCornerShape(16.dp))
                                            .clickable { viewModel.selectDay(diaNum) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("day_tab_$diaNum")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = diaNombre,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = txt,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (countForThisDay > 0) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White.copy(alpha = 0.25f) else SolemAccentCyan.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "$countForThisDay",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isSelected) Color.White else SolemAccentCyan,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(activeBloques) { slot ->
                                    val matchesInBloque = bloquesDelDia
                                        .filter { it.bloque in slot.bloques }
                                        .distinctBy { "${it.sigla}_${it.paralelo}_${it.tipo}" }

                                    MiHorarioBloqueCard(
                                        bloqueLabel = slot.label,
                                        horarioHoras = slot.horas,
                                        clases = matchesInBloque,
                                        onRemoveParalelo = { sigla, paralelo -> viewModel.unenrollParalelo(sigla, paralelo) }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Detalle de Clase al tocar una celda
    selectedDetailClass?.let { cl ->
        ClassDetailBottomSheet(
            clase = cl,
            onDismiss = { selectedDetailClass = null },
            onUnenrollParalelo = { sigla, paralelo ->
                viewModel.unenrollParalelo(sigla, paralelo)
                selectedDetailClass = null
            },
            onUnenrollRamo = { sigla ->
                viewModel.unenrollRamo(sigla)
                selectedDetailClass = null
            }
        )
    }

    // Diálogo de Resolución de Choque de Horario / Ramo Intercalado
    pendingConflict?.let { conflict ->
        ConflictResolutionDialog(
            conflict = conflict,
            onDismiss = { pendingConflict = null },
            onConfirmIntercalated = { firstSigla ->
                val other = conflict.conflictingClasses.first()
                viewModel.enrollIntercalado(
                    sigla1 = conflict.newSigla,
                    par1 = conflict.newParalelo,
                    sigla2 = other.sigla,
                    par2 = other.paralelo,
                    firstSigla = firstSigla,
                    campus = conflict.newCampus,
                    periodo = conflict.newPeriodo
                )
                pendingConflict = null
                showAddModal = false
                selectedAsignaturaForEnroll = null
            },
            onConfirmIgnoredTope = {
                val other = conflict.conflictingClasses.first()
                viewModel.ignoreTopeAndEnroll(
                    sigla1 = conflict.newSigla,
                    par1 = conflict.newParalelo,
                    sigla2 = other.sigla,
                    par2 = other.paralelo,
                    campus = conflict.newCampus,
                    periodo = conflict.newPeriodo
                )
                pendingConflict = null
                showAddModal = false
                selectedAsignaturaForEnroll = null
            }
        )
    }

    // Modal para Añadir Ramo a Mi Horario
    if (showAddModal) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddModal = false
                selectedAsignaturaForEnroll = null
            },
            containerColor = SolemSurfaceCard,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (selectedAsignaturaForEnroll == null) {
                    Text(
                        text = "Añadir a Mi Horario",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SolemTextPrimary
                    )
                    Text(
                        text = "Selecciona una asignatura de tu malla o búscala por código.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SolemTextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SolemSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.searchQueryAsignatura.value = it },
                        placeholderText = "Buscar por sigla o nombre..."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                    ) {
                        items(availableAsignaturas, key = { it.sigla }) { asig ->
                            val isInscribed = userPrefs.misRamosInscritos.contains(asig.sigla)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isInscribed) SolemPrimaryBlue.copy(alpha = 0.2f) else SolemSurfaceVariant.copy(alpha = 0.5f))
                                    .clickable { selectedAsignaturaForEnroll = asig }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = asig.sigla,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = SolemPrimaryBlueLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${asig.creditos} créditos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SolemAccentCyan,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = asig.nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SolemTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isInscribed) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SolemAccentEmerald.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Inscrito",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SolemAccentEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Seleccionar",
                                        tint = SolemTextMuted
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val asig = selectedAsignaturaForEnroll!!
                    val campusFilteredParalelos by viewModel.getParalelosForSigla(asig.sigla).collectAsState(initial = emptyList())
                    val allParalelos by viewModel.getAllParalelosForSigla(asig.sigla).collectAsState(initial = emptyList())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = asig.sigla,
                                style = MaterialTheme.typography.labelMedium,
                                color = SolemAccentCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = asig.nombre,
                                style = MaterialTheme.typography.titleLarge,
                                color = SolemTextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${asig.departamento} • ${asig.creditos} créditos SCT",
                                style = MaterialTheme.typography.bodySmall,
                                color = SolemTextMuted
                            )
                        }

                        IconButton(
                            onClick = { selectedAsignaturaForEnroll = null },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SolemSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Volver", tint = SolemTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "PARALELOS DISPONIBLES (${userPrefs.selectedCampus})",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolemPrimaryBlueLight,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (campusFilteredParalelos.isEmpty() && allParalelos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SolemSurfaceVariant.copy(alpha = 0.5f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column {
                                Text(
                                    text = "Añadir en Paralelo 1",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SolemTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sede: ${userPrefs.selectedCampus}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextMuted
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val conflicts = viewModel.getConflictingEnrolledClasses(asig.sigla, "1", userPrefs.selectedCampus, userPrefs.selectedPeriodo)
                                            if (conflicts.isNotEmpty()) {
                                                pendingConflict = PendingEnrollConflict(
                                                    newSigla = asig.sigla,
                                                    newParalelo = "1",
                                                    newCampus = userPrefs.selectedCampus,
                                                    newPeriodo = userPrefs.selectedPeriodo,
                                                    conflictingClasses = conflicts
                                                )
                                            } else {
                                                viewModel.enrollParalelo(asig.sigla, "1")
                                                showAddModal = false
                                                selectedAsignaturaForEnroll = null
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SolemPrimaryBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Confirmar (Paralelo 1)")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp)
                        ) {
                            items(campusFilteredParalelos, key = { it.id }) { par ->
                                val parBloques by viewModel.getBloquesForParalelo(par.id).collectAsState(initial = emptyList())
                                val profs = parBloques.mapNotNull { it.profesor }.filter { it.isNotBlank() }.distinct()
                                val profText = if (profs.isNotEmpty()) profs.joinToString(", ") else "Docente USM"

                                val diasNombres = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
                                val scheduleDistribution = parBloques
                                    .groupBy { it.dia }
                                    .toSortedMap()
                                    .map { (diaIdx, bList) ->
                                        val diaNom = diasNombres.getOrElse(diaIdx) { "D$diaIdx" }
                                        val minB = bList.minOfOrNull { it.bloque } ?: 0
                                        val maxB = bList.maxOfOrNull { it.bloque } ?: 0
                                        val bStr = if (minB == maxB) "$minB" else "$minB-$maxB"
                                        val sala = bList.mapNotNull { it.sala }.firstOrNull { it.isNotBlank() } ?: "Sala por definir"
                                        "$diaNom $bStr ($sala)"
                                    }.joinToString(" • ")

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SolemSurfaceVariant.copy(alpha = 0.7f))
                                        .border(1.dp, SolemBorder, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                        ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(SolemPrimaryBlue.copy(alpha = 0.3f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "Paralelo ${par.paralelo}",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = SolemPrimaryBlueLight,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${par.campus} • Cupo: ${par.cupo ?: "N/D"}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SolemTextMuted,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        val conflicts = viewModel.getConflictingEnrolledClasses(asig.sigla, par.paralelo, par.campus, par.periodo)
                                                        if (conflicts.isNotEmpty()) {
                                                            pendingConflict = PendingEnrollConflict(
                                                                newSigla = asig.sigla,
                                                                newParalelo = par.paralelo,
                                                                newCampus = par.campus,
                                                                newPeriodo = par.periodo,
                                                                conflictingClasses = conflicts
                                                            )
                                                        } else {
                                                            viewModel.enrollParalelo(asig.sigla, par.paralelo, par.campus, par.periodo)
                                                            showAddModal = false
                                                            selectedAsignaturaForEnroll = null
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = SolemPrimaryBlue,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Text("Añadir", fontSize = 12.sp, maxLines = 1, softWrap = false)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Profesor y Salas
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = SolemAccentEmerald,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Profesor: $profText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SolemAccentEmerald,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Room,
                                                contentDescription = null,
                                                tint = SolemAccentCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = scheduleDistribution.ifBlank { "Horario y salas por definir" },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SolemAccentCyan,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun MiHorarioBloqueCard(
    bloqueLabel: String,
    horarioHoras: String,
    clases: List<EnrolledClassBlock>,
    onRemoveParalelo: (sigla: String, paralelo: String) -> Unit
) {
    val hasClases = clases.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (hasClases) SolemSurfaceCard else SolemSurfaceCard.copy(alpha = 0.5f))
            .border(
                1.dp,
                if (hasClases) SolemPrimaryBlueLight.copy(alpha = 0.5f) else SolemBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = bloqueLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (hasClases) SolemPrimaryBlueLight else SolemTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = horarioHoras,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasClases) SolemAccentCyan else SolemTextMuted,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasClases) SolemAccentEmerald.copy(alpha = 0.15f) else SolemSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (hasClases) "${clases.size} asignada(s)" else "Libre",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasClases) SolemAccentEmerald else SolemTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (hasClases) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    clases.forEach { c ->
                        val (bgTint, borderTint) = getCourseColor(c.sigla)
                        val tipoTag = when (c.tipo?.lowercase()?.trim()) {
                            "taller", "práctica", "practica" -> "Taller"
                            "ayudantía", "ayudantia" -> "Ayudantía"
                            "laboratorio", "lab" -> "Lab"
                            else -> "Cátedra"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgTint.copy(alpha = 0.12f))
                                .border(1.dp, borderTint.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                // Fila 1: Badges de Sigla, Paralelo, Tipo, Intercalado y Botón Eliminar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f, fill = false),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Badge de Sigla
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(borderTint.copy(alpha = 0.25f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = c.sigla,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = borderTint,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Badge de Paralelo
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SolemAccentCyan.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "P${c.paralelo}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SolemAccentCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }

                                        // Badge de Tipo
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SolemSurfaceVariant)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tipoTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SolemTextMuted,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        // Badge de Semana Intercalada (si aplica)
                                        if (c.semanaIntercalada != null) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF7C4DFF).copy(alpha = 0.25f))
                                                    .border(0.5.dp, Color(0xFFB388FF), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Semana ${c.semanaIntercalada}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFB388FF),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { onRemoveParalelo(c.sigla, c.paralelo) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Quitar paralelo o taller",
                                            tint = SolemTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Fila 2: Nombre Completo de la Asignatura
                                Text(
                                    text = c.asignaturaNombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SolemTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                val prof = c.profesor?.takeIf { it.isNotBlank() } ?: "Docente USM"
                                val sala = c.sala?.takeIf { it.isNotBlank() } ?: "Sala por definir"

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = SolemAccentEmerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = prof,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SolemAccentEmerald,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Room,
                                            contentDescription = null,
                                            tint = SolemAccentCyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = sala,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SolemAccentCyan,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val COURSE_PALETTE = listOf(
    Color(0xFF2979FF) to Color(0xFF82B1FF), // Electric Cobalt
    Color(0xFF00E676) to Color(0xFF69F0AE), // Neon Emerald
    Color(0xFF7C4DFF) to Color(0xFFB388FF), // Plasma Violet
    Color(0xFFFF9100) to Color(0xFFFFD180), // Amber Flame
    Color(0xFFFF5252) to Color(0xFFFF8A80), // Cyber Coral
    Color(0xFF00E5FF) to Color(0xFF84FFFF), // Cyan Wave
    Color(0xFFFFD600) to Color(0xFFFFFF8D), // Solar Gold
    Color(0xFF536DFE) to Color(0xFF8C9EFF), // Deep Indigo
    Color(0xFF1DE9B6) to Color(0xFFA7FFEB), // Mint Teal
    Color(0xFFF50057) to Color(0xFFFF80AB), // Neon Magenta
    Color(0xFF00B0FF) to Color(0xFF80D8FF), // Vivid Sky
    Color(0xFF76FF03) to Color(0xFFCCFF90)  // Lime Electric
)

fun getCourseColor(sigla: String): Pair<Color, Color> {
    val baseSigla = if (sigla.any { it.isDigit() }) {
        sigla.replace(Regex("[^0-9]+\$"), "")
    } else sigla
    val idx = (baseSigla.hashCode() and 0x7FFFFFFF) % COURSE_PALETTE.size
    return COURSE_PALETTE[idx]
}

@Composable
fun HorarioSemanalMatrixGrid(
    miHorarioBloques: List<EnrolledClassBlock>,
    topes: List<HorarioTope>,
    activeDias: List<Pair<Int, String>>,
    activeBloques: List<BloqueSlot>,
    onSelectClass: (EnrolledClassBlock) -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    val blockWidth = 96.dp
    val headerTimeWidth = 60.dp
    val rowHeight = 76.dp

    val density = androidx.compose.ui.platform.LocalDensity.current
    val todayWeekdayIndex = remember {
        when (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> 0
            java.util.Calendar.TUESDAY -> 1
            java.util.Calendar.WEDNESDAY -> 2
            java.util.Calendar.THURSDAY -> 3
            java.util.Calendar.FRIDAY -> 4
            else -> 0
        }
    }

    LaunchedEffect(todayWeekdayIndex, activeDias) {
        val targetDayPosition = activeDias.indexOfFirst { it.first == todayWeekdayIndex }
        if (targetDayPosition > 0) {
            val blockWidthPx = with(density) { blockWidth.toPx() }
            val headerWidthPx = with(density) { headerTimeWidth.toPx() }
            val targetOffsetPx = headerWidthPx + (targetDayPosition * blockWidthPx) - (blockWidthPx / 2)
            horizontalScrollState.animateScrollTo(targetOffsetPx.toInt().coerceAtLeast(0))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SolemSurfaceCard)
            .border(1.dp, SolemBorder, RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState)
        ) {
            // Header Row: Bloque + Días activos (Lunes a Viernes, o Sábado si tiene ramos)
            Row(
                modifier = Modifier
                    .background(SolemSurfaceVariant.copy(alpha = 0.8f))
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier.width(headerTimeWidth),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bloque",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolemTextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                activeDias.forEach { (_, diaNombre) ->
                    Box(
                        modifier = Modifier.width(blockWidth),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = diaNombre.take(3),
                            style = MaterialTheme.typography.labelMedium,
                            color = SolemPrimaryBlueLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Grilla de Filas por cada Bloque USM activo
            activeBloques.forEach { slot ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Columna de Horas
                    Column(
                        modifier = Modifier
                            .width(headerTimeWidth)
                            .height(rowHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SolemSurfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = slot.shortLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = SolemPrimaryBlueLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        val startHour = slot.horas.substringBefore(" - ")
                        val endHour = slot.horas.substringAfter(" - ")
                        Text(
                            text = startHour,
                            style = MaterialTheme.typography.labelSmall,
                            color = SolemTextMuted,
                            fontSize = 9.sp
                        )
                        Text(
                            text = endHour,
                            style = MaterialTheme.typography.labelSmall,
                            color = SolemTextMuted,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Columnas de Días activos
                    activeDias.forEach { (diaNum, _) ->
                        val matchingClasses = miHorarioBloques
                            .filter { it.dia == diaNum && it.bloque in slot.bloques }
                            .distinctBy { "${it.sigla}_${it.paralelo}_${it.tipo}" }
                        val isIntercalado = matchingClasses.size > 1 && matchingClasses.all { it.semanaIntercalada != null }
                        val isTope = matchingClasses.size > 1 && !isIntercalado

                        Box(
                            modifier = Modifier
                                .width(blockWidth)
                                .height(rowHeight)
                                .padding(horizontal = 2.dp)
                        ) {
                            if (matchingClasses.isEmpty()) {
                                // Celda Vacía
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SolemBackground.copy(alpha = 0.35f))
                                        .border(0.5.dp, SolemBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                )
                            } else if (isIntercalado) {
                                // Celda con Clases Intercaladas (Semanas A/B)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF7C4DFF).copy(alpha = 0.22f))
                                        .border(1.5.dp, Color(0xFFB388FF).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                        .clickable { onSelectClass(matchingClasses.first()) }
                                        .padding(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "INTERCALADO",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFB388FF),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 7.5.sp
                                            )
                                            Text(
                                                text = "A/B",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SolemAccentCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp
                                            )
                                        }
                                        matchingClasses.take(2).forEach { cl ->
                                            val semLabel = cl.semanaIntercalada?.let { "[$it]" } ?: ""
                                            Text(
                                                text = "$semLabel ${cl.sigla}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SolemTextPrimary,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            } else if (isTope) {
                                // Celda con Tope de Horario
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SolemAccentAmber.copy(alpha = 0.25f))
                                        .border(1.5.dp, SolemAccentAmber, RoundedCornerShape(8.dp))
                                        .clickable { onSelectClass(matchingClasses.first()) }
                                        .padding(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "¡TOPE!",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SolemAccentAmber,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = "${matchingClasses.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SolemAccentAmber,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                        matchingClasses.take(2).forEach { cl ->
                                            Text(
                                                text = "${cl.sigla} P${cl.paralelo}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SolemTextPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Celda con 1 Clase
                                val cl = matchingClasses.first()
                                val (bgTint, borderTint) = getCourseColor(cl.sigla)

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bgTint.copy(alpha = 0.18f))
                                        .border(1.dp, borderTint.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        .clickable { onSelectClass(cl) }
                                        .padding(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cl.sigla,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = borderTint,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "P${cl.paralelo}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SolemAccentCyan,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 9.sp
                                            )
                                        }

                                        val salaText = cl.sala?.takeIf { it.isNotBlank() } ?: "Sala ?"
                                        Text(
                                            text = salaText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SolemTextPrimary,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        val profShort = cl.profesor?.takeIf { it.isNotBlank() }?.split(" ")?.let { parts ->
                                            if (parts.size >= 2) "${parts.first()} ${parts.last()}" else parts.first()
                                        } ?: "Docente"

                                        Text(
                                            text = profShort,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SolemTextMuted,
                                            fontSize = 8.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailBottomSheet(
    clase: EnrolledClassBlock,
    onDismiss: () -> Unit,
    onUnenrollParalelo: (sigla: String, paralelo: String) -> Unit,
    onUnenrollRamo: (sigla: String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SolemSurfaceCard,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            val (bgTint, borderTint) = getCourseColor(clase.sigla)
            val tipoTag = when (clase.tipo?.lowercase()?.trim()) {
                "taller", "práctica", "practica" -> "Taller"
                "ayudantía", "ayudantia" -> "Ayudantía"
                "laboratorio", "lab" -> "Laboratorio"
                else -> "Cátedra"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(borderTint.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = clase.sigla,
                            style = MaterialTheme.typography.titleMedium,
                            color = borderTint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SolemAccentCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Paralelo ${clase.paralelo}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SolemAccentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SolemSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tipoTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = SolemTextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = clase.periodo,
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = clase.asignaturaNombre,
                style = MaterialTheme.typography.titleLarge,
                color = SolemTextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fila de Profesor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SolemSurfaceVariant)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = SolemAccentEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Profesor Asignado",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolemTextMuted
                    )
                    Text(
                        text = clase.profesor?.takeIf { it.isNotBlank() } ?: "Docente Departamento USM",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SolemAccentEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de Sala y Tipo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SolemSurfaceVariant)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Room,
                    contentDescription = null,
                    tint = SolemAccentCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Ubicación & Modalidad",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolemTextMuted
                    )
                    Text(
                        text = "${clase.sala ?: "Sala por asignar"} • $tipoTag",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SolemTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de Campus y Periodo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SolemSurfaceVariant)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = SolemPrimaryBlueLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Campus / Sede",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolemTextMuted
                    )
                    Text(
                        text = "${clase.campus ?: "Casa Central Valparaíso"} (${clase.periodo})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SolemTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón único para eliminar la asignatura completa
            Button(
                onClick = { onUnenrollRamo(clase.sigla) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SolemAccentRose.copy(alpha = 0.2f),
                    contentColor = SolemAccentRose
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar del Horario", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
