package com.example.ui.configuracion
import androidx.compose.material.icons.filled.Code

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.notification.ClassNotificationHelper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.example.ui.components.SolemSearchBar
import com.example.ui.onboarding.SEDES_USM_DEFAULT
import com.example.ui.theme.SolemAccentCyan
import com.example.ui.theme.SolemAccentEmerald
import com.example.ui.theme.SolemBackground
import com.example.ui.theme.SolemBorder
import com.example.ui.theme.SolemPrimaryBlue
import com.example.ui.theme.SolemPrimaryBlueLight
import com.example.ui.theme.SolemSurfaceCard
import com.example.ui.theme.SolemSurfaceVariant
import com.example.ui.theme.SolemTextMuted
import com.example.ui.theme.SolemTextPrimary
import com.example.ui.theme.SolemTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    userPreferencesRepository: UserPreferencesRepository,
    repository: UsmDataRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val userPrefs by userPreferencesRepository.userPreferencesFlow.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                coroutineScope.launch {
                    userPreferencesRepository.updateNotificationSettings(
                        enabled = true,
                        minutesBefore = userPrefs.minutosAnticipacionNotificacion
                    )
                }
            }
        }
    )
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
                    else -> "Sede USM"
                }
                campus to desc
            }
        } else {
            SEDES_USM_DEFAULT
        }
    }

    val asignaturas by repository.getAllAsignaturas().collectAsState(initial = emptyList())
    val asignaturasMap = remember(asignaturas) { asignaturas.associateBy { it.sigla } }

    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var showCarreraSheet by remember { mutableStateOf(false) }
    var showCampusSheet by remember { mutableStateOf(false) }
    var showPeriodoSheet by remember { mutableStateOf(false) }
    var carreraQuery by remember { mutableStateOf("") }

    val distinctCarreras = remember(carreras) {
        carreras.distinctBy { it.id }
    }

    val filteredCarreras = remember(distinctCarreras, carreraQuery) {
        val query = carreraQuery.trim().lowercase()
        if (query.isBlank()) distinctCarreras
        else distinctCarreras.filter { car ->
            car.nombre.lowercase().contains(query) ||
                    car.codigoCarrera.lowercase().contains(query) ||
                    car.nombreMalla.lowercase().contains(query) ||
                    (query == "inf" && car.nombre.contains("Informática", ignoreCase = true)) ||
                    (query == "tel" && car.nombre.contains("Telemática", ignoreCase = true)) ||
                    (query == "arq" && car.nombre.contains("Arquitectura", ignoreCase = true)) ||
                    (query == "ind" && car.nombre.contains("Industrial", ignoreCase = true)) ||
                    (query == "elo" && car.nombre.contains("Electrónica", ignoreCase = true)) ||
                    (query == "eli" && car.nombre.contains("Eléctrica", ignoreCase = true)) ||
                    (query == "mec" && car.nombre.contains("Mecánica", ignoreCase = true)) ||
                    (query == "qui" && car.nombre.contains("Química", ignoreCase = true)) ||
                    (query == "civ" && car.nombre.contains("Civil", ignoreCase = true)) ||
                    (query == "mat" && car.nombre.contains("Matemática", ignoreCase = true)) ||
                    (query == "fis" && car.nombre.contains("Física", ignoreCase = true)) ||
                    (query == "icn" && car.nombre.contains("Comercial", ignoreCase = true))
        }
    }

    Scaffold(
        containerColor = SolemBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.displayMedium,
                    color = SolemTextPrimary
                )
                Text(
                    text = "Personaliza tu sede, carrera, versión de malla y tu horario personal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SolemTextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 1. Sede / Campus
            item {
                Text(
                    text = "SEDE / CAMPUS USM",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemAccentCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolemSurfaceCard)
                        .border(1.dp, SolemBorder, RoundedCornerShape(16.dp))
                        .clickable { showCampusSheet = true }
                        .padding(16.dp)
                        .testTag("settings_campus_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SolemAccentCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = SolemAccentCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = userPrefs.selectedCampus,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SolemTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Toca para cambiar sede o campus",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextMuted
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { showCampusSheet = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cambiar", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 2. Carrera Universitaria
            item {
                Text(
                    text = "CARRERA UNIVERSITARIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemAccentCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolemSurfaceCard)
                        .border(1.dp, SolemBorder, RoundedCornerShape(16.dp))
                        .clickable { showCarreraSheet = true }
                        .padding(16.dp)
                        .testTag("settings_carrera_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SolemPrimaryBlue.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = SolemPrimaryBlueLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = userPrefs.selectedCarreraNombre.ifBlank { "Carrera ${userPrefs.selectedCarreraCodigo}" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SolemTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Código oficial: ${userPrefs.selectedCarreraCodigo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextMuted
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { showCarreraSheet = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cambiar", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. Periodo Académico
            item {
                Text(
                    text = "PERIODO ACADÉMICO / SEMESTRE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemAccentCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolemSurfaceCard)
                        .border(1.dp, SolemBorder, RoundedCornerShape(16.dp))
                        .clickable { showPeriodoSheet = true }
                        .padding(16.dp)
                        .testTag("settings_periodo_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SolemAccentEmerald.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = SolemAccentEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Semestre ${userPrefs.selectedPeriodo}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SolemTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (userPrefs.selectedPeriodo == "2026-2") {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SolemAccentEmerald.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Actual",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SolemAccentEmerald,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Periodo activo de asignaturas y paralelos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextMuted
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { showPeriodoSheet = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cambiar", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 4. Gestión de Horario Personal
            item {
                Text(
                    text = "MI HORARIO INSCRITO",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemAccentCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolemSurfaceCard)
                        .border(1.dp, SolemBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = SolemAccentEmerald,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "${userPrefs.misRamosInscritos.size} ramos inscritos",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = SolemTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${userPrefs.misParalelosInscritos.size} paralelos seleccionados",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SolemTextMuted
                                    )
                                }
                            }

                            if (userPrefs.misParalelosInscritos.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            userPreferencesRepository.clearHorario()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Limpiar todo", fontSize = 12.sp, color = Color(0xFFFF5252))
                                }
                            }
                        }

                        if (userPrefs.misParalelosInscritos.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aún no tienes ramos en tu horario personal. Ve a 'Asignaturas' para añadir los paralelos que estás cursando.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SolemTextMuted
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                userPrefs.misParalelosInscritos.forEach { entry ->
                                    val parts = entry.split(":")
                                    val sigla = parts.getOrNull(0) ?: entry
                                    val par = parts.getOrNull(1) ?: "1"
                                    val asig = asignaturasMap[sigla]
                                    val asigNombre = asig?.nombre ?: sigla
                                    val tag = when {
                                        sigla.endsWith("T", ignoreCase = true) -> "Taller"
                                        sigla.endsWith("Y", ignoreCase = true) -> "Ayudantía"
                                        sigla.endsWith("L", ignoreCase = true) -> "Lab"
                                        else -> "Cátedra"
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SolemSurfaceVariant.copy(alpha = 0.6f))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(SolemPrimaryBlue.copy(alpha = 0.3f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = sigla,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = SolemPrimaryBlueLight,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(SolemAccentCyan.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "P$par • $tag",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = SolemAccentCyan,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = asigNombre,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SolemTextPrimary,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    userPreferencesRepository.unenrollParalelo(sigla, par)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Quitar este paralelo",
                                                tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Recordatorios y Notificaciones de Clase
            item {
                Text(
                    text = "RECORDATORIOS DE CLASE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemAccentCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolemSurfaceCard)
                        .border(1.dp, SolemBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Avisos de Próxima Clase & Sala",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SolemTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Notificación con tu sala asignada antes de iniciar el bloque",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextMuted
                                )
                            }
                            androidx.compose.material3.Switch(
                                checked = userPrefs.notificacionesClaseActivas,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED

                                            if (!hasPermission) {
                                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                coroutineScope.launch {
                                                    userPreferencesRepository.updateNotificationSettings(
                                                        enabled = true,
                                                        minutesBefore = userPrefs.minutosAnticipacionNotificacion
                                                    )
                                                }
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                userPreferencesRepository.updateNotificationSettings(
                                                    enabled = true,
                                                    minutesBefore = userPrefs.minutosAnticipacionNotificacion
                                                )
                                            }
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            userPreferencesRepository.updateNotificationSettings(
                                                enabled = false,
                                                minutesBefore = userPrefs.minutosAnticipacionNotificacion
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        if (userPrefs.notificacionesClaseActivas) {
                            HorizontalDivider(color = SolemBorder.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Anticipación del aviso",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextSecondary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(10, 15, 30).forEach { mins ->
                                        val isSelected = userPrefs.minutosAnticipacionNotificacion == mins
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) SolemPrimaryBlue else SolemSurfaceVariant)
                                                .clickable {
                                                    coroutineScope.launch {
                                                        userPreferencesRepository.updateNotificationSettings(
                                                            enabled = true,
                                                            minutesBefore = mins
                                                        )
                                                    }
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "$mins min",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) Color.White else SolemTextMuted,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Botón de prueba directa de notificación
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (!hasPermission) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            return@OutlinedButton
                                        }
                                    }
                                    ClassNotificationHelper.sendTestNotification(
                                        context = context,
                                        sigla = userPrefs.misParalelosInscritos.firstOrNull()?.split(":")?.firstOrNull() ?: "MAT070",
                                        tipo = "Cátedra",
                                        sala = "Sala P202",
                                        bloqueStr = "Bloque 3-4",
                                        minutesBefore = userPrefs.minutosAnticipacionNotificacion
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SolemAccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Probar notificación de ejemplo ahora", fontSize = 12.sp, color = SolemAccentCyan)
                            }
                        }
                    }
                }
            }

            // 6. Sincronización y Actualizaciones
            item {
                Text(
                    text = "DATOS Y SINCRONIZACIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemAccentCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolemSurfaceCard)
                        .border(1.dp, SolemBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Base de Datos SIGA Offline",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SolemTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Actualización automática en segundo plano cada 12h",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SolemAccentEmerald.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Disponible",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SolemAccentEmerald,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isSyncing = true
                                    syncMessage = null
                                    val res = repository.syncAllData(true)
                                    isSyncing = false
                                    syncMessage = if (res.isSuccess) "¡Datos SIGA actualizados exitosamente!" else "Error al actualizar datos SIGA"
                                }
                            },
                            enabled = !isSyncing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SolemPrimaryBlue,
                                contentColor = Color.White
                            )
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Actualizando datos SIGA...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Actualización SIGA")
                            }
                        }

                        if (syncMessage != null) {
                            Text(
                                text = syncMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (syncMessage!!.contains("exitosa")) SolemAccentEmerald else SolemAccentCyan
                            )
                        }
                    }
                }
            }

            // 6. Acerca de y Declaración de Privacidad
            item {
                Text(
                    text = "ACERCA DE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemAccentCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolemSurfaceCard)
                        .border(1.dp, SolemBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SolemPrimaryBlueLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Solem.",
                                style = MaterialTheme.typography.titleMedium,
                                color = SolemTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { uriHandler.openUri("https://github.com/LucasFluxa/Solem.") }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "GitHub",
                                tint = SolemPrimaryBlueLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Ver código fuente en GitHub",
                                style = MaterialTheme.typography.bodySmall,
                                color = SolemPrimaryBlueLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        HorizontalDivider(
                            color = SolemBorder,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Versión",
                                style = MaterialTheme.typography.bodySmall,
                                color = SolemTextMuted
                            )
                            Text(
                                text = "1.0.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = SolemAccentCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Aviso de Privacidad y Descargo de Responsabilidad Oficial
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SolemSurfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, SolemBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Esta aplicación es un desarrollo independiente y no posee afiliación, patrocinio ni respaldo oficial por parte de la Universidad Técnica Federico Santa María ni de sus entidades asociadas. Garantizamos tu privacidad: la app no recopila datos personales ni utiliza cookies de terceros.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SolemTextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Modal para Selección de Sede
    if (showCampusSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCampusSheet = false },
            containerColor = SolemSurfaceCard,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Seleccionar Sede o Campus",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SolemTextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(campusList) { (campus, desc) ->
                        val isSelected = userPrefs.selectedCampus == campus
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SolemPrimaryBlue.copy(alpha = 0.25f) else SolemSurfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    coroutineScope.launch {
                                        userPreferencesRepository.updateCampus(campus)
                                        showCampusSheet = false
                                    }
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = campus,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SolemTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SolemAccentEmerald
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Modal para Selección de Carrera
    if (showCarreraSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCarreraSheet = false },
            containerColor = SolemSurfaceCard,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Seleccionar Carrera",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SolemTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                SolemSearchBar(
                    query = carreraQuery,
                    onQueryChange = { carreraQuery = it },
                    placeholderText = "Buscar carrera o código..."
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    items(filteredCarreras, key = { it.id }) { car ->
                        val isSelected = userPrefs.selectedCarreraCodigo == car.codigoCarrera
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SolemPrimaryBlue.copy(alpha = 0.25f) else SolemSurfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    coroutineScope.launch {
                                        userPreferencesRepository.updateCarrera(car.codigoCarrera, car.nombre)
                                        showCarreraSheet = false
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SolemPrimaryBlue.copy(alpha = 0.3f))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = car.codigoCarrera,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SolemPrimaryBlueLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = car.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SolemTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = car.nombreMalla.ifBlank { if (car.tipoMalla == "ANTIGUA") "Plan Tradicional" else "Plan Innovado" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) SolemAccentCyan else SolemTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SolemAccentEmerald
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Modal para Selección de Periodo Académico
    if (showPeriodoSheet) {
        val periodos = listOf(
            "2026-2" to "Segundo Semestre 2026 (Semestre Actual)",
            "2026-1" to "Primer Semestre 2026",
            "2025-2" to "Segundo Semestre 2025",
            "2025-1" to "Primer Semestre 2025",
            "2024-2" to "Segundo Semestre 2024",
            "2024-1" to "Primer Semestre 2024"
        )
        ModalBottomSheet(
            onDismissRequest = { showPeriodoSheet = false },
            containerColor = SolemSurfaceCard,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Seleccionar Periodo Académico",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SolemTextPrimary
                )
                Text(
                    text = "Filtra la oferta de asignaturas, paralelos y profesores por semestre para evitar mezclas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SolemTextMuted
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(periodos) { (periodoCode, desc) ->
                        val isSelected = userPrefs.selectedPeriodo == periodoCode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SolemPrimaryBlue.copy(alpha = 0.25f) else SolemSurfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    coroutineScope.launch {
                                        userPreferencesRepository.updatePeriodo(periodoCode)
                                        showPeriodoSheet = false
                                    }
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Periodo $periodoCode",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SolemTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (periodoCode == "2026-2") {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SolemAccentEmerald.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Actual",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SolemAccentEmerald,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SolemAccentEmerald
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
