package com.example.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.UsmDataRepository
import com.example.ui.components.SolemSearchBar
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

val SEDES_USM_DEFAULT = listOf(
    "Campus Santiago San Joaquín" to "Santiago, RM",
    "Campus Santiago Vitacura" to "Santiago, RM",
    "Casa Central Valparaíso" to "Valparaíso, V Región",
    "Sede Viña del Mar" to "Viña del Mar, V Región",
    "Sede Concepción" to "Hualpén, VIII Región"
)

@Composable
fun OnboardingScreen(
    repository: UsmDataRepository,
    userPreferencesRepository: UserPreferencesRepository,
    onFinished: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isSyncing = true
        repository.syncAllData(false)
        isSyncing = false
    }

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

    var currentStep by remember { mutableIntStateOf(1) } // 1: Campus, 2: Carrera
    var selectedCampus by remember { mutableStateOf(campusList.firstOrNull()?.first ?: "Casa Central Valparaíso") }
    var selectedCarrera by remember { mutableStateOf<CarreraPlanEntity?>(null) }
    var carreraSearchQuery by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val filteredCarreras = remember(carreras, carreraSearchQuery) {
        val query = carreraSearchQuery.trim().lowercase()
        val list = if (query.isBlank()) {
            carreras
        } else {
            carreras.filter { car ->
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
        list.distinctBy { it.id }
    }

    Scaffold(
        containerColor = SolemBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Step Indicator Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    IconButton(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SolemSurfaceCard)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = SolemTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(38.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepDot(step = 1, currentStep = currentStep)
                    StepDot(step = 2, currentStep = currentStep)
                }

                Box(modifier = Modifier.size(38.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(
                            slideOutHorizontally { -it } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(
                            slideOutHorizontally { it } + fadeOut()
                        )
                    }
                },
                label = "step_animation"
            ) { step ->
                when (step) {
                    1 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "PASO 1 DE 2",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolemPrimaryBlueLight,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Selecciona tu Sede o Campus",
                                style = MaterialTheme.typography.displayMedium,
                                color = SolemTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Te mostraremos los horarios y paralelos correspondientes a tu lugar de estudio.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SolemTextMuted
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(campusList) { (campus, desc) ->
                                    val isSelected = selectedCampus == campus
                                    CampusSelectionCard(
                                        campusName = campus,
                                        description = desc,
                                        isSelected = isSelected,
                                        onClick = { selectedCampus = campus }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Tarjeta de Privacidad y Descargo Oficial (Primera ejecución)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SolemSurfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, SolemBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Esta aplicación es un desarrollo independiente y no posee afiliación, patrocinio ni respaldo oficial por parte de la Universidad Técnica Federico Santa María ni de sus entidades asociadas. Garantizamos tu privacidad: la app no recopila datos personales ni utiliza cookies de terceros.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SolemTextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { currentStep = 2 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("continue_to_carrera_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SolemPrimaryBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Continuar a Selección de Carrera",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    2 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "PASO 2 DE 2",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolemPrimaryBlueLight,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¿Cuál es tu Carrera?",
                                style = MaterialTheme.typography.displayMedium,
                                color = SolemTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sede seleccionada: $selectedCampus",
                                style = MaterialTheme.typography.bodySmall,
                                color = SolemAccentCyan
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            SolemSearchBar(
                                query = carreraSearchQuery,
                                onQueryChange = { carreraSearchQuery = it },
                                placeholderText = "Buscar carrera o código..."
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (filteredCarreras.isNotEmpty()) "${filteredCarreras.size} opciones disponibles" else "Cargando carreras...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SolemTextMuted
                                )

                                if (isSyncing) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = SolemPrimaryBlueLight,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Sincronizando...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SolemPrimaryBlueLight
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (filteredCarreras.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = SolemTextMuted,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Text(
                                            text = if (carreraSearchQuery.isNotBlank()) "No se encontraron carreras con '$carreraSearchQuery'" else "Cargando carreras...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SolemTextMuted
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isSyncing = true
                                                    repository.syncAllData(true)
                                                    isSyncing = false
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Recargar carreras")
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(filteredCarreras, key = { it.id }) { c ->
                                        val isSelected = selectedCarrera?.id == c.id
                                        CarreraSelectionCard(
                                            carrera = c,
                                            isSelected = isSelected,
                                            onClick = { selectedCarrera = c }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val effectiveCarrera = selectedCarrera ?: filteredCarreras.firstOrNull()
                            val canProceed = effectiveCarrera != null && !isSaving

                            Button(
                                onClick = {
                                    val car = effectiveCarrera
                                    if (car != null) {
                                        isSaving = true
                                        coroutineScope.launch {
                                            userPreferencesRepository.savePreferences(
                                                campus = selectedCampus,
                                                carreraCodigo = car.codigoCarrera,
                                                carreraNombre = car.nombre,
                                                planTipo = car.tipoMalla,
                                                completed = true
                                            )
                                            onFinished()
                                        }
                                    }
                                },
                                enabled = canProceed,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("onboarding_finish_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SolemPrimaryBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Comenzar a usar Solem.",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepDot(step: Int, currentStep: Int) {
    val isActive = currentStep >= step
    Box(
        modifier = Modifier
            .size(if (isActive) 24.dp else 8.dp, 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) SolemPrimaryBlueLight else SolemSurfaceVariant)
    )
}

@Composable
fun CampusSelectionCard(
    campusName: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) SolemSurfaceCard else SolemSurfaceVariant.copy(alpha = 0.5f)
    val borderCol = if (isSelected) SolemPrimaryBlueLight else SolemBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("campus_card_$campusName")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) SolemPrimaryBlue.copy(alpha = 0.3f)
                        else SolemSurfaceCard
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (isSelected) SolemAccentCyan else SolemTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = campusName,
                    style = MaterialTheme.typography.titleMedium,
                    color = SolemTextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SolemTextMuted
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(SolemAccentEmerald),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SolemBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CarreraSelectionCard(
    carrera: CarreraPlanEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) SolemSurfaceCard else SolemSurfaceVariant.copy(alpha = 0.5f)
    val borderCol = if (isSelected) SolemPrimaryBlueLight else SolemBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("carrera_card_${carrera.codigoCarrera}_${carrera.tipoMalla}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) SolemPrimaryBlue.copy(alpha = 0.3f)
                        else SolemSurfaceCard
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = carrera.codigoCarrera,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) SolemPrimaryBlueLight else SolemTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = carrera.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SolemTextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val mallaLabel = carrera.nombreMalla.ifBlank {
                    if (carrera.tipoMalla == "ANTIGUA") "Malla Antigua" else "Malla Nueva"
                }
                Text(
                    text = "$mallaLabel • ${carrera.siglas.size} ramos",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (carrera.tipoMalla == "ANTIGUA") SolemTextMuted else SolemAccentCyan,
                    fontSize = 11.sp
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(SolemAccentEmerald),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SolemBackground,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
