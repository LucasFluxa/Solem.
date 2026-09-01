package com.example.ui.profesores

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BloqueHorarioEntity
import com.example.data.local.entity.ProfesorEntity
import com.example.ui.components.ProfesoresScopeToggle
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
fun ProfesoresScreen(
    viewModel: ProfesoresViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val profesores by viewModel.profesores.collectAsState()
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

            Column {
                Text(
                    text = "DOCENCIA USM",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemPrimaryBlueLight,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Profesores",
                    style = MaterialTheme.typography.displayMedium,
                    color = SolemTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Switch: Mi Sede vs Todos los Profesores
            ProfesoresScopeToggle(
                isFilteredByPreference = isFiltered,
                onToggle = { viewModel.toggleFilterScope(it) },
                userPreferences = userPrefs
            )

            Spacer(modifier = Modifier.height(12.dp))

            SolemSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.searchQuery.value = it },
                placeholderText = "Buscar profesor o sigla de ramo..."
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isFiltered) {
                    "${profesores.size} profesores dictando en ${userPrefs.selectedCampus}"
                } else {
                    "${profesores.size} profesores en el catálogo USM"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isFiltered) SolemAccentCyan else SolemTextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (profesores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No se encontraron profesores",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SolemTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFiltered) "Prueba cambiando a 'Todos los Profesores' para ver otras sedes"
                            else "Intenta con otro término de búsqueda",
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
                    items(profesores, key = { it.id }, contentType = { "profesor_card" }) { prof ->
                        ProfesorCard(
                            profesor = prof,
                            blocks = emptyList(),
                            onClick = { viewModel.selectProfesor(prof) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        uiState.selectedProfesor?.let { selected ->
            ModalBottomSheet(
                onDismissRequest = { viewModel.selectProfesor(null) },
                sheetState = sheetState,
                containerColor = SolemSurface,
                dragHandle = null
            ) {
                ProfesorDetailSheet(
                    profesor = selected,
                    bloques = uiState.profesorBloques,
                    onClose = { viewModel.selectProfesor(null) }
                )
            }
        }
    }
}

@Composable
fun ProfesorCard(
    profesor: ProfesorEntity,
    blocks: List<BloqueHorarioEntity>,
    onClick: () -> Unit
) {
    val campuses = remember(profesor.sedes, blocks) {
        if (!profesor.sedes.isNullOrBlank()) {
            profesor.sedes.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct()
        } else {
            blocks.mapNotNull { it.campus }.filter { it.isNotBlank() }.distinct()
        }
    }
    val siglas = remember(profesor.ramosImpartidos, blocks) {
        if (!profesor.ramosImpartidos.isNullOrBlank()) {
            profesor.ramosImpartidos.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct()
        } else {
            blocks.map { it.sigla }.filter { it.isNotBlank() }.distinct()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SolemSurfaceCard)
            .border(1.dp, SolemBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("profesor_card_${profesor.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SolemPrimaryBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SolemPrimaryBlueLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = profesor.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = SolemTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!profesor.departamento.isNullOrBlank()) {
                            Text(
                                text = profesor.departamento,
                                style = MaterialTheme.typography.bodySmall,
                                color = SolemTextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (siglas.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SolemSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${siglas.size} ramo(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = SolemAccentCyan,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Sedes donde dicta
            if (campuses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SolemAccentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = campuses.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = SolemTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Asignaturas en chips
            if (siglas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    siglas.forEach { sigla ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SolemPrimaryBlue.copy(alpha = 0.15f))
                                .border(1.dp, SolemPrimaryBlueLight.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = sigla,
                                style = MaterialTheme.typography.labelSmall,
                                color = SolemPrimaryBlueLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfesorDetailSheet(
    profesor: ProfesorEntity,
    bloques: List<BloqueHorarioEntity>,
    onClose: () -> Unit
) {
    val campuses = remember(profesor.sedes, bloques) {
        if (!profesor.sedes.isNullOrBlank()) {
            profesor.sedes.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct()
        } else {
            bloques.mapNotNull { it.campus }.filter { it.isNotBlank() }.distinct()
        }
    }
    val groupedByParalelo = remember(bloques) {
        bloques.groupBy { "${it.sigla} - Paralelo ${it.paralelo} (${it.campus})" }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // Nombre del profesor
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SolemPrimaryBlue.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = SolemPrimaryBlueLight,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = profesor.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = SolemTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = profesor.departamento ?: "Docente Departamento USM",
                    style = MaterialTheme.typography.bodySmall,
                    color = SolemAccentEmerald
                )
            }
        }

        if (campuses.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SolemAccentCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Sedes: ${campuses.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SolemTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Lista detallada de ramos y paralelos que imparte
        val totalCount = if (groupedByParalelo.isNotEmpty()) groupedByParalelo.size else (profesor.ramosImpartidos?.split(",")?.size ?: 0)
        Text(
            text = "ASIGNATURAS Y PARALELOS QUE IMPARTE ($totalCount)",
            style = MaterialTheme.typography.labelSmall,
            color = SolemPrimaryBlueLight,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (groupedByParalelo.isEmpty()) {
            if (!profesor.ramosImpartidos.isNullOrBlank()) {
                val ramosList = profesor.ramosImpartidos.split(",").map { it.trim() }.filter { it.isNotBlank() }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    ramosList.forEach { sigla ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SolemPrimaryBlue.copy(alpha = 0.2f))
                                .border(1.dp, SolemPrimaryBlueLight.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sigla,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SolemPrimaryBlueLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No hay registros de horarios activos en este periodo para este docente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SolemTextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groupedByParalelo.entries.toList(), key = { it.key }) { (header, bList) ->
                    val firstBlock = bList.firstOrNull()
                    val salas = bList.mapNotNull { it.sala }.filter { it.isNotBlank() }.distinct()
                    val salaStr = if (salas.isNotEmpty()) salas.joinToString(", ") else "Sala por definir"
                    val tipoStr = firstBlock?.tipo ?: "Cátedra"
                    val diasNombres = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
                    val scheduleSummary = bList
                        .groupBy { it.dia }
                        .toSortedMap()
                        .map { (diaIdx, list) ->
                            val dia = diasNombres.getOrElse(diaIdx) { "D$diaIdx" }
                            val minB = list.minOfOrNull { it.bloque } ?: 0
                            val maxB = list.maxOfOrNull { it.bloque } ?: 0
                            val bStr = if (minB == maxB) "$minB" else "$minB-$maxB"
                            "$dia $bStr"
                        }.joinToString(" • ")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SolemSurfaceCard)
                            .border(1.dp, SolemBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SolemTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SolemSurfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tipoStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SolemAccentCyan,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📍 $salaStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextSecondary,
                                    fontSize = 11.sp
                                )
                                if (scheduleSummary.isNotBlank()) {
                                    Text(
                                        text = "⏰ $scheduleSummary",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SolemPrimaryBlueLight,
                                        fontWeight = FontWeight.Medium,
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
