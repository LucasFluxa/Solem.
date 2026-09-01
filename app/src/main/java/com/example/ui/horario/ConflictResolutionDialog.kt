package com.example.ui.horario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Immutable
data class PendingEnrollConflict(
    val newSigla: String,
    val newParalelo: String,
    val newCampus: String,
    val newPeriodo: String,
    val conflictingClasses: List<EnrolledClassBlock>
)

@Composable
fun ConflictResolutionDialog(
    conflict: PendingEnrollConflict,
    onDismiss: () -> Unit,
    onConfirmIntercalated: (firstSigla: String) -> Unit,
    onConfirmIgnoredTope: () -> Unit
) {
    var isIntercalatedSelection by remember { mutableStateOf<Boolean?>(null) } // null: choosing, true: yes, false: no
    val conflictingSigla = conflict.conflictingClasses.firstOrNull()?.sigla ?: "Ramo Inscrito"
    val conflictingPar = conflict.conflictingClasses.firstOrNull()?.paralelo ?: "1"
    var firstSiglaChoice by remember { mutableStateOf(conflict.newSigla) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SolemSurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .border(1.dp, SolemBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isIntercalatedSelection == true) SolemAccentCyan.copy(alpha = 0.15f)
                            else SolemAccentAmber.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIntercalatedSelection == true) Icons.Default.SwapHoriz else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isIntercalatedSelection == true) SolemAccentCyan else SolemAccentAmber,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isIntercalatedSelection == true) "Configurar Ramo Intercalado" else "¡Coincidencia de Horario!",
                    style = MaterialTheme.typography.titleMedium,
                    color = SolemTextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Clash description
                Text(
                    text = "El paralelo ${conflict.newSigla} (P${conflict.newParalelo}) comparte horario con ${conflictingSigla} (P${conflictingPar}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = SolemTextMuted,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isIntercalatedSelection == null) {
                    // Question: Is it intercalated?
                    Text(
                        text = "¿Estas clases se dictan de forma intercalada (semanas alternadas / quincenales)?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SolemTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { isIntercalatedSelection = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SolemPrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sí, intercalado", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { isIntercalatedSelection = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SolemBorder)
                        ) {
                            Text("No, es choque", color = SolemTextPrimary, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = SolemTextMuted, fontSize = 12.sp)
                    }

                } else if (isIntercalatedSelection == true) {
                    // Selection of order: Which week comes first?
                    Text(
                        text = "¿Cuál de las asignaturas se dicta primero en la Semana 1 (Semana A)?",
                        style = MaterialTheme.typography.bodySmall,
                        color = SolemTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Option A: newSigla first
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (firstSiglaChoice == conflict.newSigla) SolemPrimaryBlue.copy(alpha = 0.2f)
                                else SolemSurfaceVariant
                            )
                            .border(
                                1.5.dp,
                                if (firstSiglaChoice == conflict.newSigla) SolemAccentCyan
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { firstSiglaChoice = conflict.newSigla }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = firstSiglaChoice == conflict.newSigla,
                                    onClick = { firstSiglaChoice = conflict.newSigla },
                                    colors = RadioButtonDefaults.colors(selectedColor = SolemAccentCyan)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = "${conflict.newSigla} primero (Semana 1 / A)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SolemTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Luego ${conflictingSigla} en la Semana 2 (B)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SolemTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option B: conflictingSigla first
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (firstSiglaChoice == conflictingSigla) SolemPrimaryBlue.copy(alpha = 0.2f)
                                else SolemSurfaceVariant
                            )
                            .border(
                                1.5.dp,
                                if (firstSiglaChoice == conflictingSigla) SolemAccentCyan
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { firstSiglaChoice = conflictingSigla }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = firstSiglaChoice == conflictingSigla,
                                    onClick = { firstSiglaChoice = conflictingSigla },
                                    colors = RadioButtonDefaults.colors(selectedColor = SolemAccentCyan)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = "${conflictingSigla} primero (Semana 1 / A)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SolemTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Luego ${conflict.newSigla} en la Semana 2 (B)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SolemTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onConfirmIntercalated(firstSiglaChoice) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SolemPrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Guardar como Intercalados", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(onClick = { isIntercalatedSelection = null }) {
                        Text("Atrás", color = SolemTextMuted, fontSize = 12.sp)
                    }

                } else {
                    // Option "No, es un choque": alert + ignore option
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SolemAccentAmber.copy(alpha = 0.12f))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = SolemAccentAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ambas asignaturas ocurrirán al mismo tiempo. Puedes agregarlo e ignorar la advertencia si tienes autorización docente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SolemTextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onConfirmIgnoredTope,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SolemAccentAmber),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ignorar choque y agregar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { isIntercalatedSelection = null }) {
                            Text("Atrás", color = SolemTextMuted, fontSize = 12.sp)
                        }
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar inscripción", color = SolemTextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
