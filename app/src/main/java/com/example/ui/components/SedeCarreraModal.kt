package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CarreraPlanEntity
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.UsmDataRepository
import com.example.ui.onboarding.CampusSelectionCard
import com.example.ui.onboarding.CarreraSelectionCard
import com.example.ui.onboarding.SEDES_USM_DEFAULT
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SedeCarreraModal(
    userPreferences: UserPreferences,
    userPreferencesRepository: UserPreferencesRepository,
    repository: UsmDataRepository,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val carreras by repository.getAllCarreras().collectAsState(initial = emptyList())
    val campusListRaw by repository.getCampusList().collectAsState(initial = emptyList())

    val campusList = remember(campusListRaw) {
        if (campusListRaw.isNotEmpty()) {
            campusListRaw.map { campus ->
                val desc = when {
                    campus.contains("Santiago", ignoreCase = true) -> "Región Metropolitana"
                    campus.contains("Valparaíso", ignoreCase = true) -> "Región de Valparaíso"
                    campus.contains("Viña", ignoreCase = true) -> "Región de Valparaíso"
                    campus.contains("Concepción", ignoreCase = true) -> "Región del Biobío"
                    else -> "USM"
                }
                campus to desc
            }
        } else {
            SEDES_USM_DEFAULT
        }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Sede, 1: Carrera
    var tempCampus by remember { mutableStateOf(userPreferences.selectedCampus) }
    var tempCarreraCodigo by remember { mutableStateOf(userPreferences.selectedCarreraCodigo) }
    var tempCarreraNombre by remember { mutableStateOf(userPreferences.selectedCarreraNombre) }
    var carreraQuery by remember { mutableStateOf("") }

    val filteredCarreras = remember(carreras, carreraQuery) {
        if (carreraQuery.isBlank()) {
            carreras
        } else {
            carreras.filter {
                it.nombre.contains(carreraQuery, ignoreCase = true) ||
                        it.codigoCarrera.contains(carreraQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SolemSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Bar del Modal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AJUSTES DE PERFIL",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolemPrimaryBlueLight,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Cambiar Sede y Carrera",
                        style = MaterialTheme.typography.titleLarge,
                        color = SolemTextPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SolemSurfaceCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = SolemTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de pestaña Sede vs Carrera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SolemSurfaceVariant)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 0) SolemPrimaryBlue else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1. Sede / Campus",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selectedTab == 0) Color.White else SolemTextSecondary,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 1) SolemPrimaryBlue else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2. Carrera / Plan",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selectedTab == 1) Color.White else SolemTextSecondary,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Selección de Sede
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(campusList) { (campus, desc) ->
                            val isSelected = tempCampus == campus
                            CampusSelectionCard(
                                campusName = campus,
                                description = desc,
                                isSelected = isSelected,
                                onClick = {
                                    tempCampus = campus
                                    selectedTab = 1 // Auto-avance a carrera
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // Selección de Carrera
                    SolemSearchBar(
                        query = carreraQuery,
                        onQueryChange = { carreraQuery = it },
                        placeholderText = "Buscar carrera o código..."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCarreras, key = { it.id }) { c ->
                            val isSelected = tempCarreraCodigo == c.codigoCarrera
                            CarreraSelectionCard(
                                carrera = c,
                                isSelected = isSelected,
                                onClick = {
                                    tempCarreraCodigo = c.codigoCarrera
                                    tempCarreraNombre = c.nombre
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Guardar Cambios
            Button(
                onClick = {
                    scope.launch {
                        userPreferencesRepository.savePreferences(
                            campus = tempCampus,
                            carreraCodigo = tempCarreraCodigo,
                            carreraNombre = tempCarreraNombre,
                            completed = true
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_config_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SolemPrimaryBlue,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar Preferencias",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
