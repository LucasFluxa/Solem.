package com.example.ui.ramos

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AsignaturaEntity
import com.example.data.local.entity.BloqueHorarioEntity
import com.example.data.local.entity.ParaleloEntity
import com.example.ui.components.AsignaturasScopeToggle
import com.example.ui.components.ContextBadge
import com.example.ui.components.SolemSearchBar
import com.example.ui.theme.SolemAccentCyan
import com.example.ui.theme.SolemAccentEmerald
import com.example.ui.theme.SolemBackground
import com.example.ui.theme.SolemBorder
import com.example.ui.theme.SolemPrimaryBlue
import com.example.ui.theme.SolemPrimaryBlueLight
import com.example.ui.theme.SolemSurface
import com.example.ui.theme.SolemSurfaceCard
import com.example.ui.theme.SolemSurfaceVariant
import com.example.ui.theme.SolemTextMuted
import com.example.ui.theme.SolemTextPrimary
import com.example.ui.theme.SolemTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamosScreen(
    viewModel: RamosViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val asignaturas by viewModel.asignaturas.collectAsState()
    val departamentos by viewModel.departamentos.collectAsState()
    val selectedDept by viewModel.selectedDept.collectAsState()
    val isFiltered by viewModel.isFilteredByPreference.collectAsState()
    val userPrefs by viewModel.userPreferences.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = SolemBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Header con título y botón de sincronización
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EXPLORADOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolemPrimaryBlueLight,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Asignaturas",
                        style = MaterialTheme.typography.displayMedium,
                        color = SolemTextPrimary
                    )
                }

                IconButton(
                    onClick = { viewModel.checkAndSyncData(forceRefresh = true) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SolemSurfaceCard)
                        .testTag("sync_button")
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SolemPrimaryBlueLight,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar datos",
                            tint = SolemTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selector: Mi Malla vs Todas las Asignaturas
            AsignaturasScopeToggle(
                isFilteredByPreference = isFiltered,
                onToggle = { viewModel.toggleFilterScope(it) },
                userPreferences = userPrefs
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Buscador
            SolemSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.searchQuery.value = it },
                placeholderText = "Buscar por sigla, nombre o depto..."
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtros horizontales por Departamento
            if (departamentos.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeptFilterChip(
                        text = "Todos",
                        isSelected = selectedDept == null,
                        onClick = { viewModel.selectDepartamento(null) }
                    )
                    departamentos.take(12).forEach { dept ->
                        DeptFilterChip(
                            text = dept,
                            isSelected = selectedDept == dept,
                            onClick = { viewModel.selectDepartamento(dept) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Contador de resultados y estado de malla
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFiltered) {
                        val mallaLabel = if (userPrefs.selectedPlanTipo == "ANTIGUA") "Malla Antigua" else "Malla Nueva"
                        "${asignaturas.size} asignaturas en $mallaLabel"
                    } else {
                        "${asignaturas.size} asignaturas en catálogo general"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFiltered) SolemAccentCyan else SolemTextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de Asignaturas
            if (asignaturas.isEmpty() && !uiState.isSyncing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No se encontraron asignaturas",
                            style = MaterialTheme.typography.titleMedium,
                            color = SolemTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isFiltered) "Prueba cambiando a 'Todas las Asignaturas' o revisa la malla en Ajustes"
                            else "Intenta con otra búsqueda o sincroniza los datos",
                            style = MaterialTheme.typography.bodySmall,
                            color = SolemTextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(asignaturas, key = { it.sigla }, contentType = { "asignatura_card" }) { ramo ->
                        val isInHorario = userPrefs.misRamosInscritos.contains(ramo.sigla)
                        AsignaturaCard(
                            asignatura = ramo,
                            isInHorario = isInHorario,
                            onClick = { viewModel.selectAsignatura(ramo) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Modal Detalle de Asignatura
        uiState.selectedAsignatura?.let { selected ->
            val isInHorario = userPrefs.misRamosInscritos.contains(selected.sigla)
            ModalBottomSheet(
                onDismissRequest = { viewModel.selectAsignatura(null) },
                sheetState = sheetState,
                containerColor = SolemSurface,
                dragHandle = null
            ) {
                AsignaturaDetailSheet(
                    asignatura = selected,
                    paralelos = uiState.selectedParalelos,
                    bloques = uiState.selectedBloques,
                    isInHorario = isInHorario,
                    onEnrollParalelo = { sigla, par, camp, per ->
                        viewModel.enrollParalelo(sigla, par, camp, per)
                    },
                    onUnenroll = { sigla ->
                        viewModel.unenrollRamo(sigla)
                    },
                    onClose = { viewModel.selectAsignatura(null) }
                )
            }
        }
    }
}

@Composable
fun DeptFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) SolemPrimaryBlue else SolemSurfaceCard
    val borderCol = if (isSelected) SolemPrimaryBlueLight else SolemBorder
    val txtCol = if (isSelected) SolemTextPrimary else SolemTextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = txtCol,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun AsignaturaCard(
    asignatura: AsignaturaEntity,
    isInHorario: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SolemSurfaceCard)
            .border(1.dp, SolemBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("asignatura_card_${asignatura.sigla}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sigla destacada con tipografía técnica
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SolemPrimaryBlue.copy(alpha = 0.2f))
                            .border(1.dp, SolemPrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = asignatura.sigla,
                            style = MaterialTheme.typography.labelLarge,
                            color = SolemPrimaryBlueLight,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isInHorario) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SolemAccentEmerald.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "En Mi Horario",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolemAccentEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Créditos y horas
                if (asignatura.creditos > 0) {
                    Text(
                        text = "${asignatura.creditos} CRÉDITOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolemAccentCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nombre de la asignatura
            Text(
                text = asignatura.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = SolemTextPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Departamento y horas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (asignatura.departamento.isNotBlank()) asignatura.departamento else "USM",
                    style = MaterialTheme.typography.bodySmall,
                    color = SolemTextMuted
                )

                if (asignatura.horasTeoricas > 0 || asignatura.horasLaboratorios > 0 || asignatura.horasAyudantias > 0) {
                    Text(
                        text = "T:${asignatura.horasTeoricas} P:${asignatura.horasPracticas} L:${asignatura.horasLaboratorios} A:${asignatura.horasAyudantias}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolemTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun AsignaturaDetailSheet(
    asignatura: AsignaturaEntity,
    paralelos: List<ParaleloEntity>,
    bloques: List<BloqueHorarioEntity>,
    isInHorario: Boolean,
    onEnrollParalelo: (String, String, String, String) -> Unit,
    onUnenroll: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SolemPrimaryBlue.copy(alpha = 0.25f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = asignatura.sigla,
                    style = MaterialTheme.typography.titleMedium,
                    color = SolemPrimaryBlueLight,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isInHorario) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SolemAccentEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Inscrito en Horario",
                            style = MaterialTheme.typography.labelSmall,
                            color = SolemAccentEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = "${asignatura.creditos} Créditos",
                    style = MaterialTheme.typography.labelMedium,
                    color = SolemAccentCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = asignatura.nombre,
            style = MaterialTheme.typography.titleLarge,
            color = SolemTextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Departamento: ${if (asignatura.departamento.isNotBlank()) asignatura.departamento else "USM"}",
            style = MaterialTheme.typography.bodyMedium,
            color = SolemTextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Horas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SolemSurfaceCard)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            HourItem(label = "Teóricas", value = asignatura.horasTeoricas)
            HourItem(label = "Prácticas", value = asignatura.horasPracticas)
            HourItem(label = "Laboratorio", value = asignatura.horasLaboratorios)
            HourItem(label = "Ayudantía", value = asignatura.horasAyudantias)
        }

        // Requisitos
        if (asignatura.requisitos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "REQUISITOS",
                style = MaterialTheme.typography.labelSmall,
                color = SolemPrimaryBlueLight
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                asignatura.requisitos.forEach { req ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SolemSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = req, style = MaterialTheme.typography.labelSmall, color = SolemTextPrimary)
                    }
                }
            }
        }

        // Paralelos y Botón de Añadir a Mi Horario
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PARALELOS DISPONIBLES (${paralelos.size})",
                style = MaterialTheme.typography.labelSmall,
                color = SolemPrimaryBlueLight
            )

            if (isInHorario) {
                Button(
                    onClick = {
                        onUnenroll(asignatura.sigla)
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolemSurfaceVariant,
                        contentColor = SolemTextMuted
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Quitar de Mi Horario", fontSize = 11.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (paralelos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SolemSurfaceCard)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Sin paralelos con bloque registrado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SolemTextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!isInHorario) {
                        Button(
                            onClick = {
                                onEnrollParalelo(asignatura.sigla, "1", "", "")
                                onClose()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SolemPrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Añadir a Mi Horario (Paralelo 1)", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(paralelos) { p ->
                    val paralBloques = bloques.filter { it.paraleloId == p.id }
                    val profesores = paralBloques.mapNotNull { it.profesor }.filter { it.isNotBlank() }.distinct().joinToString(", ")

                    val diasNombres = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
                    val scheduleDistribution = paralBloques
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(SolemSurfaceCard)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Paralelo ${p.paralelo} • ${p.campus}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SolemTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (profesores.isNotBlank()) {
                                    Text(
                                        text = "Prof: $profesores",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SolemTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                if (scheduleDistribution.isNotBlank()) {
                                    Text(
                                        text = scheduleDistribution,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SolemAccentCyan,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onEnrollParalelo(asignatura.sigla, p.paralelo, p.campus, p.periodo)
                                    onClose()
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
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun HourItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value hrs",
            style = MaterialTheme.typography.labelLarge,
            color = SolemTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SolemTextMuted,
            fontSize = 10.sp
        )
    }
}
