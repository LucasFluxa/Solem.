package com.example.ui.malla

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dto.MallaCurricularModel
import com.example.data.dto.MallaRamoModel
import com.example.ui.components.SolemSearchBar
import com.example.ui.onboarding.CarreraSelectionCard
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
import com.example.ui.theme.SolemMallaAprobadoBg
import com.example.ui.theme.SolemMallaAprobadoText

private val ColorPrerrequisito = Color(0xFFF59E0B) // Ámbar Solar
private val ColorHabilita = Color(0xFF10B981)      // Bio-Esmeralda
private val ColorCorrequisito = Color(0xFF00F0FF)  // Plasma Cyan
private val ColorSinRequisito = Color(0xFF64748B)  // Slate Muted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MallaScreen(
    viewModel: MallaViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userPrefs by viewModel.userPreferences.collectAsState()
    val activeMalla by viewModel.activeMalla.collectAsState()
    val progressInfo by viewModel.progressInfo.collectAsState()
    val selectedRamoSigla by viewModel.selectedRamoSigla.collectAsState()
    val selectedPrereqs by viewModel.selectedPrerequisitos.collectAsState()
    val selectedHabilita by viewModel.selectedHabilita.collectAsState()
    val selectedCorreqs by viewModel.selectedCorrequisitos.collectAsState()
    val allCarreras by viewModel.allCarreras.collectAsState()

    var showSearchModal by remember { mutableStateOf(false) }
    var carreraQuery by remember { mutableStateOf("") }

    val filteredCarreras = remember(allCarreras, carreraQuery) {
        val query = carreraQuery.trim().lowercase()
        if (query.isBlank()) allCarreras.distinctBy { it.id }
        else allCarreras.filter { car ->
            car.nombre.lowercase().contains(query) ||
                    car.codigoCarrera.lowercase().contains(query) ||
                    car.nombreMalla.lowercase().contains(query) ||
                    (query == "inf" && car.nombre.contains("Informática", ignoreCase = true)) ||
                    (query == "tel" && car.nombre.contains("Telemática", ignoreCase = true)) ||
                    (query == "arq" && car.nombre.contains("Arquitectura", ignoreCase = true)) ||
                    (query == "ind" && car.nombre.contains("Industrial", ignoreCase = true)) ||
                    (query == "icn" && car.nombre.contains("Comercial", ignoreCase = true)) ||
                    (query == "civ" && car.nombre.contains("Civil", ignoreCase = true))
        }.distinctBy { it.id }
    }

    Scaffold(
        containerColor = SolemBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 10.dp)
        ) {
            // 1. Barra Superior: Logo USM, Carrera y Botón "Buscar malla"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SolemPrimaryBlue.copy(alpha = 0.25f))
                            .border(1.dp, SolemPrimaryBlueLight.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "USM",
                            tint = SolemAccentCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = activeMalla?.carreraNombre ?: userPrefs.selectedCarreraNombre,
                            style = MaterialTheme.typography.titleMedium,
                            color = SolemTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val planText = activeMalla?.let { "Plan ${it.planLabel} (${if (it.tipoMalla == "NUEVA") "Malla Nueva" else "Malla Antigua"})" }
                            ?: "Plan ${userPrefs.selectedCarreraCodigo}"
                        Text(
                            text = planText,
                            style = MaterialTheme.typography.labelSmall,
                            color = SolemAccentCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón "Buscar malla"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SolemSurfaceCard)
                        .border(1.dp, SolemBorder, RoundedCornerShape(12.dp))
                        .clickable { showSearchModal = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("buscar_malla_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Buscar Malla",
                            tint = SolemTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Buscar malla",
                            style = MaterialTheme.typography.labelMedium,
                            color = SolemTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Barra de Avance y Progreso de la Carrera
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SolemSurfaceCard)
                    .border(1.dp, SolemBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Avance de Carrera:",
                                style = MaterialTheme.typography.labelMedium,
                                color = SolemTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${progressInfo.porcentajeAvance}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = SolemAccentEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${progressInfo.creditosAprobados} / ${progressInfo.totalCreditos} SCT • ${progressInfo.ramosAprobadosCount} ramos",
                            style = MaterialTheme.typography.labelSmall,
                            color = SolemAccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (progressInfo.porcentajeAvance / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = SolemAccentEmerald,
                        trackColor = SolemSurfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Barra de Leyenda de Requisitos
            MallaLegendBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Grilla de Semestres y Ramos con Desplazamiento Horizontal
            if (activeMalla == null || activeMalla!!.semestres.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SolemPrimaryBlueLight)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Cargando malla curricular...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SolemTextMuted
                        )
                    }
                }
            } else {
                MallaMatrixView(
                    malla = activeMalla!!,
                    aprobados = userPrefs.ramosAprobados,
                    selectedSigla = selectedRamoSigla,
                    prereqsOfSelected = selectedPrereqs,
                    habilitaOfSelected = selectedHabilita,
                    correqsOfSelected = selectedCorreqs,
                    onRamoClick = { ramo ->
                        // Al presionar el ramo: se marca/desmarca como aprobado directamente
                        viewModel.onRamoTapped(ramo.sigla)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Modal de Selección Rápida de Mallas de Carrera
    if (showSearchModal) {
        ModalBottomSheet(
            onDismissRequest = { showSearchModal = false },
            containerColor = SolemSurface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Buscar Malla Curricular",
                        style = MaterialTheme.typography.titleLarge,
                        color = SolemTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { showSearchModal = false },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SolemSurfaceCard)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = SolemTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SolemSearchBar(
                    query = carreraQuery,
                    onQueryChange = { carreraQuery = it },
                    placeholderText = "Buscar carrera o código..."
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCarreras, key = { it.id }) { car ->
                        val isSelected = userPrefs.selectedCarreraCodigo == car.codigoCarrera &&
                                userPrefs.selectedPlanTipo == car.tipoMalla
                        CarreraSelectionCard(
                            carrera = car,
                            isSelected = isSelected,
                            onClick = {
                                viewModel.switchCarrera(car)
                                showSearchModal = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Barra de Leyenda de Requisitos
 */
@Composable
fun MallaLegendBar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SolemSurfaceCard)
            .border(1.dp, SolemBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prerrequisito
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ColorPrerrequisito)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Prerrequisito",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemTextPrimary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Habilita
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.size(width = 4.dp, height = 3.dp).background(ColorHabilita))
                    Box(modifier = Modifier.size(width = 4.dp, height = 3.dp).background(ColorHabilita))
                    Box(modifier = Modifier.size(width = 4.dp, height = 3.dp).background(ColorHabilita))
                }
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Habilita",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemTextPrimary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Correquisito
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(ColorCorrequisito))
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(ColorCorrequisito))
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(ColorCorrequisito))
                }
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Correquisito",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemTextPrimary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Sin requisito
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .border(1.dp, ColorSinRequisito, CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Sin requisito",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolemTextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Matriz Desplazable Horizontal y Verticalmente con las Columnas de Semestres
 * y Curvas Interactivas de Conexión de Requisitos.
 */
@Composable
fun MallaMatrixView(
    malla: MallaCurricularModel,
    aprobados: Set<String>,
    selectedSigla: String?,
    prereqsOfSelected: Set<String>,
    habilitaOfSelected: Set<String>,
    correqsOfSelected: Set<String>,
    onRamoClick: (MallaRamoModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    val columnWidth = 142.dp

    val cardLeftAnchors = remember { mutableStateMapOf<String, Offset>() }
    val cardRightAnchors = remember { mutableStateMapOf<String, Offset>() }
    val cardCenters = remember { mutableStateMapOf<String, Offset>() }

    var containerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
            .verticalScroll(verticalScrollState)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier.onGloballyPositioned { containerCoordinates = it }
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                malla.semestres.forEach { semestre ->
                    Column(
                        modifier = Modifier.width(columnWidth),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Encabezado de Columna: Número Romano y Total SCT
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = semestre.romano,
                                style = MaterialTheme.typography.titleLarge,
                                color = SolemTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${semestre.totalSct} SCT",
                                style = MaterialTheme.typography.labelMedium,
                                color = SolemTextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Tarjetas de Ramos en el Semestre
                        semestre.ramos.forEach { ramo ->
                            val isAprobado = aprobados.contains(ramo.sigla)
                            val isSelected = selectedSigla == ramo.sigla
                            val isPrereqOfSelected = prereqsOfSelected.contains(ramo.sigla)
                            val isHabilitadoBySelected = habilitaOfSelected.contains(ramo.sigla)
                            val isCorreqOfSelected = correqsOfSelected.contains(ramo.sigla)

                            MallaRamoCard(
                                ramo = ramo,
                                isAprobado = isAprobado,
                                isSelected = isSelected,
                                isPrereqOfSelected = isPrereqOfSelected,
                                isHabilitadoBySelected = isHabilitadoBySelected,
                                isCorreqOfSelected = isCorreqOfSelected,
                                onClick = { onRamoClick(ramo) },
                                modifier = Modifier.onGloballyPositioned { coords ->
                                    containerCoordinates?.let { parent ->
                                        if (coords.isAttached && parent.isAttached) {
                                            val left = parent.localPositionOf(coords, Offset(0f, coords.size.height / 2f))
                                            val right = parent.localPositionOf(coords, Offset(coords.size.width.toFloat(), coords.size.height / 2f))
                                            val center = parent.localPositionOf(coords, Offset(coords.size.width / 2f, coords.size.height / 2f))
                                            if (cardLeftAnchors[ramo.sigla]?.let { (it - left).getDistanceSquared() > 1f } != false) {
                                                cardLeftAnchors[ramo.sigla] = left
                                            }
                                            if (cardRightAnchors[ramo.sigla]?.let { (it - right).getDistanceSquared() > 1f } != false) {
                                                cardRightAnchors[ramo.sigla] = right
                                            }
                                            if (cardCenters[ramo.sigla]?.let { (it - center).getDistanceSquared() > 1f } != false) {
                                                cardCenters[ramo.sigla] = center
                                            }
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }

            // Capa de Canvas superpuesta para dibujar curvas interactivas de requisitos
            if (selectedSigla != null) {
                Canvas(
                    modifier = Modifier.matchParentSize()
                ) {
                    val selLeft = cardLeftAnchors[selectedSigla]
                    val selRight = cardRightAnchors[selectedSigla]
                    val selCenter = cardCenters[selectedSigla]

                    // 1. Curvas de Prerrequisitos (Línea Sólida Ámbar Solar #F59E0B)
                    if (selLeft != null) {
                        prereqsOfSelected.forEach { prereqSigla ->
                            val start = cardRightAnchors[prereqSigla] ?: cardCenters[prereqSigla]
                            if (start != null) {
                                val path = Path().apply {
                                    moveTo(start.x, start.y)
                                    val dx = (selLeft.x - start.x).coerceAtLeast(30f) / 2f
                                    cubicTo(
                                        start.x + dx, start.y,
                                        selLeft.x - dx, selLeft.y,
                                        selLeft.x, selLeft.y
                                    )
                                }
                                // Resplandor de fondo
                                drawPath(
                                    path = path,
                                    color = ColorPrerrequisito.copy(alpha = 0.35f),
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                // Línea principal continua
                                drawPath(
                                    path = path,
                                    color = ColorPrerrequisito,
                                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                                // Puntos de conexión en los extremos
                                drawCircle(color = ColorPrerrequisito, radius = 5.dp.toPx(), center = start)
                                drawCircle(color = ColorPrerrequisito, radius = 5.dp.toPx(), center = selLeft)
                            }
                        }
                    }

                    // 2. Curvas de Habilitaciones (Línea Segmentada Verde Bio-Esmeralda #10B981)
                    if (selRight != null) {
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
                        habilitaOfSelected.forEach { habSigla ->
                            val end = cardLeftAnchors[habSigla] ?: cardCenters[habSigla]
                            if (end != null) {
                                val path = Path().apply {
                                    moveTo(selRight.x, selRight.y)
                                    val dx = (end.x - selRight.x).coerceAtLeast(30f) / 2f
                                    cubicTo(
                                        selRight.x + dx, selRight.y,
                                        end.x - dx, end.y,
                                        end.x, end.y
                                    )
                                }
                                // Resplandor de fondo
                                drawPath(
                                    path = path,
                                    color = ColorHabilita.copy(alpha = 0.25f),
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, pathEffect = dashEffect)
                                )
                                // Línea segmentada
                                drawPath(
                                    path = path,
                                    color = ColorHabilita,
                                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, pathEffect = dashEffect)
                                )
                                // Puntos de conexión en los extremos
                                drawCircle(color = ColorHabilita, radius = 5.dp.toPx(), center = selRight)
                                drawCircle(color = ColorHabilita, radius = 5.dp.toPx(), center = end)
                            }
                        }
                    }

                    // 3. Curvas de Correquisitos (Línea Punteada Cyan Neón #00F0FF)
                    if (selCenter != null) {
                        val dotEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 8f), 0f)
                        correqsOfSelected.forEach { correqSigla ->
                            val end = cardCenters[correqSigla]
                            if (end != null) {
                                val path = Path().apply {
                                    moveTo(selCenter.x, selCenter.y)
                                    val dx = (end.x - selCenter.x) / 2f
                                    cubicTo(
                                        selCenter.x + dx, selCenter.y,
                                        end.x - dx, end.y,
                                        end.x, end.y
                                    )
                                }
                                drawPath(
                                    path = path,
                                    color = ColorCorrequisito,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, pathEffect = dotEffect)
                                )
                                drawCircle(color = ColorCorrequisito, radius = 4.dp.toPx(), center = selCenter)
                                drawCircle(color = ColorCorrequisito, radius = 4.dp.toPx(), center = end)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta individual de Ramo:
 * - Aprobado: Fondo Verde Menta vibrante con texto oscuro y checkmark.
 * - No aprobado: Fondo oscuro elegante con texto blanco.
 * - Bordes interactivos con los colores de requisitos al tocar (Blanco brillante para seleccionado).
 */
@Composable
fun MallaRamoCard(
    ramo: MallaRamoModel,
    isAprobado: Boolean,
    isSelected: Boolean,
    isPrereqOfSelected: Boolean,
    isHabilitadoBySelected: Boolean,
    isCorreqOfSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardHeight = 106.dp

    // Fondo y Colores de Texto según esté Aprobado o Pendiente
    val bgColor = if (isAprobado) SolemMallaAprobadoBg else SolemSurfaceCard
    val textColor = if (isAprobado) SolemMallaAprobadoText else SolemTextPrimary
    val subTextColor = if (isAprobado) SolemMallaAprobadoText.copy(alpha = 0.85f) else SolemTextMuted

    // Determinar Borde según Relaciones de Selección
    val (borderWidth, borderColor) = when {
        isSelected -> 3.dp to Color.White
        isPrereqOfSelected -> 2.5.dp to ColorPrerrequisito
        isHabilitadoBySelected -> 2.5.dp to ColorHabilita
        isCorreqOfSelected -> 2.5.dp to ColorCorrequisito
        else -> 1.dp to SolemBorder.copy(alpha = 0.6f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
            .testTag("ramo_card_${ramo.sigla}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Fila Superior: Punto indicador de estado/requisito
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dot indicador
                val dotColor = when {
                    isPrereqOfSelected -> ColorPrerrequisito
                    isHabilitadoBySelected -> ColorHabilita
                    isCorreqOfSelected -> ColorCorrequisito
                    isAprobado -> SolemMallaAprobadoText
                    ramo.prerequisitos.isEmpty() -> ColorSinRequisito
                    else -> SolemAccentCyan
                }

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )

                if (isAprobado) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Aprobado",
                        tint = SolemMallaAprobadoText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Nombre de la Asignatura
            Text(
                text = ramo.nombre,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp,
                lineHeight = 12.5.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Fila Inferior: Créditos SCT a la izquierda, Sigla a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${ramo.creditos} SCT",
                    style = MaterialTheme.typography.labelSmall,
                    color = subTextColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.5.sp
                )

                Text(
                    text = ramo.sigla,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}
