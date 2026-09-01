package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferences
import com.example.ui.theme.SolemAccentCyan
import com.example.ui.theme.SolemBorder
import com.example.ui.theme.SolemPrimaryBlue
import com.example.ui.theme.SolemPrimaryBlueLight
import com.example.ui.theme.SolemSurfaceCard
import com.example.ui.theme.SolemSurfaceVariant
import com.example.ui.theme.SolemTextMuted
import com.example.ui.theme.SolemTextPrimary
import com.example.ui.theme.SolemTextSecondary

/**
 * Encabezado de contexto que muestra la Sede, Carrera y Malla activa del usuario.
 */
@Composable
fun ContextBadge(
    userPreferences: UserPreferences,
    onClickChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val campusShort = when {
        userPreferences.selectedCampus.contains("San Joaquín", ignoreCase = true) -> "San Joaquín"
        userPreferences.selectedCampus.contains("Vitacura", ignoreCase = true) -> "Vitacura"
        userPreferences.selectedCampus.contains("Valparaíso", ignoreCase = true) -> "Casa Central"
        userPreferences.selectedCampus.contains("Viña", ignoreCase = true) -> "Viña del Mar"
        userPreferences.selectedCampus.contains("Concepción", ignoreCase = true) -> "Concepción"
        else -> userPreferences.selectedCampus.take(16)
    }

    val carreraLabel = if (userPreferences.selectedCarreraNombre.isNotBlank()) {
        userPreferences.selectedCarreraNombre
    } else if (userPreferences.selectedCarreraCodigo.isNotBlank()) {
        "Carrera ${userPreferences.selectedCarreraCodigo}"
    } else {
        "Sin carrera"
    }

    val mallaLabel = if (userPreferences.selectedPlanTipo == "ANTIGUA") "Malla Antigua" else "Malla Nueva"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SolemSurfaceCard)
            .border(1.dp, SolemBorder, RoundedCornerShape(14.dp))
            .clickable { onClickChange() }
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .testTag("context_badge")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SolemPrimaryBlue.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SolemAccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$campusShort • $carreraLabel",
                        style = MaterialTheme.typography.labelMedium,
                        color = SolemTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$mallaLabel • Semestre ${userPreferences.selectedPeriodo} • Ajustes",
                        style = MaterialTheme.typography.bodySmall,
                        color = SolemAccentCyan,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SolemSurfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar ajustes",
                    tint = SolemPrimaryBlueLight,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Selector para Asignaturas: "Mi Malla" vs "Todas las Asignaturas"
 */
@Composable
fun AsignaturasScopeToggle(
    isFilteredByPreference: Boolean,
    onToggle: (Boolean) -> Unit,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val carreraShort = if (userPreferences.selectedCarreraCodigo.isNotBlank()) {
        userPreferences.selectedCarreraCodigo
    } else {
        "Mi Carrera"
    }
    val mallaTag = if (userPreferences.selectedPlanTipo == "ANTIGUA") "Antigua" else "Nueva"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SolemSurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, SolemBorder, RoundedCornerShape(14.dp))
            .padding(4.dp)
            .testTag("asignaturas_scope_toggle")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val opt1Active = isFilteredByPreference
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (opt1Active) SolemPrimaryBlue else Color.Transparent)
                    .clickable { onToggle(true) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = if (opt1Active) Color.White else SolemTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mi Malla ($carreraShort - $mallaTag)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (opt1Active) Color.White else SolemTextSecondary,
                        fontWeight = if (opt1Active) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            val opt2Active = !isFilteredByPreference
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (opt2Active) SolemPrimaryBlue else Color.Transparent)
                    .clickable { onToggle(false) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = if (opt2Active) Color.White else SolemTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Todas las Asignaturas",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (opt2Active) Color.White else SolemTextSecondary,
                        fontWeight = if (opt2Active) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Selector para Profesores: "Mi Sede" vs "Todos los Profesores"
 */
@Composable
fun ProfesoresScopeToggle(
    isFilteredByPreference: Boolean,
    onToggle: (Boolean) -> Unit,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val campusShort = when {
        userPreferences.selectedCampus.contains("San Joaquín", ignoreCase = true) -> "San Joaquín"
        userPreferences.selectedCampus.contains("Vitacura", ignoreCase = true) -> "Vitacura"
        userPreferences.selectedCampus.contains("Valparaíso", ignoreCase = true) -> "Casa Central"
        userPreferences.selectedCampus.contains("Viña", ignoreCase = true) -> "Viña del Mar"
        userPreferences.selectedCampus.contains("Concepción", ignoreCase = true) -> "Concepción"
        else -> "Mi Sede"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SolemSurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, SolemBorder, RoundedCornerShape(14.dp))
            .padding(4.dp)
            .testTag("profesores_scope_toggle")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val opt1Active = isFilteredByPreference
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (opt1Active) SolemPrimaryBlue else Color.Transparent)
                    .clickable { onToggle(true) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (opt1Active) Color.White else SolemTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mi Sede ($campusShort)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (opt1Active) Color.White else SolemTextSecondary,
                        fontWeight = if (opt1Active) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            val opt2Active = !isFilteredByPreference
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (opt2Active) SolemPrimaryBlue else Color.Transparent)
                    .clickable { onToggle(false) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = if (opt2Active) Color.White else SolemTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Todos los Profesores",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (opt2Active) Color.White else SolemTextSecondary,
                        fontWeight = if (opt2Active) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
