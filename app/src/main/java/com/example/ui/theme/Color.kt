package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==============================================================
// PALETA SOLEM: AVANT-GARDE CYBER BLUE & PLASMA CYAN (SIN MORADO)
// ==============================================================

// Superficies y Fondos de Profundidad (Onyx / Obsidian / Deep Carbon)
val SolemBackground = Color(0xFF090A0F)        // Lienzo principal ultra profundo y limpio
val SolemBackgroundSecondary = Color(0xFF0D111A) // Fondo alternativo
val SolemSurface = Color(0xFF101420)           // Superficies elevadas de nivel 1
val SolemSurfaceCard = Color(0xFF151B2B)       // Tarjetas interactivas de nivel 2
val SolemSurfaceVariant = Color(0xFF1D263B)    // Contenedores secundarios e inputs
val SolemSurfaceElevated = Color(0xFF24304A)   // Modales y sheets elevados
val SolemBorder = Color(0xFF2B3856)            // Bordes definidos pero sutiles
val SolemBorderSubtle = Color(0xFF1E273C)      // Divisores delicados

// Colores Primarios: Hyper Cobalt / Electric Blue (Sin Morado)
val SolemPrimaryBlue = Color(0xFF2563EB)       // Azul eléctrico vibrante
val SolemPrimaryBlueLight = Color(0xFF60A5FA)  // Azul luminoso accesible
val SolemPrimaryBlueDark = Color(0xFF1E3A8A)   // Base azul profunda
val SolemPrimaryGlow = Color(0xFF3B82F6)       // Acento de brillo azul

// Aliases para compatibilidad transparente en toda la app
val SolemPrimaryViolet = SolemPrimaryBlue
val SolemPrimaryVioletLight = SolemPrimaryBlueLight
val SolemPrimaryVioletDark = SolemPrimaryBlueDark

// Acentos de Vanguardia: Plasma Cyan, Bio-Emerald, Solar Amber, Rose Spark
val SolemAccentCyan = Color(0xFF00F0FF)        // Cyan eléctrico hiper nítido
val SolemAccentCyanSubtle = Color(0xFF0284C7)  // Cyan accesible en fondos
val SolemAccentEmerald = Color(0xFF10B981)     // Verde bio-luminiscente (Aprobado)
val SolemAccentEmeraldMint = Color(0xFF34D399) // Menta accesible
val SolemAccentAmber = Color(0xFFF59E0B)       // Ámbar solar (Prerrequisitos)
val SolemAccentRose = Color(0xFFF43F5E)        // Rosa / Carmesí de alerta
val SolemAccentIndigo = Color(0xFF38BDF8)      // Celeste eléctrico

// Tipografía de Alto Contraste (WCAG AAA / AA)
val SolemTextPrimary = Color(0xFFF8FAFC)       // Blanco nieve de máxima legibilidad
val SolemTextSecondary = Color(0xFF94A3B8)     // Gris slate claro accesible
val SolemTextMuted = Color(0xFF64748B)         // Gris slate intermedio

// Tags & Badges
val SolemTagBadgeBg = Color(0xFF1E3A8A)        // Contenedor de insignia azul
val SolemTagBadgeText = Color(0xFFDBEAFE)      // Texto de insignia azul

// Tarjeta Aprobada en Malla (Verde Menta de Alto Contraste)
val SolemMallaAprobadoBg = Color(0xFF2DD4BF)   // Menta vibrante
val SolemMallaAprobadoText = Color(0xFF041B15) // Texto oscuro de 14:1 de contraste

// Gradientes de Vanguardia (Azul Eléctrico & Cyan)
val SolemGradientPrimary = Brush.horizontalGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF00F0FF))
)
val SolemGradientEmerald = Brush.horizontalGradient(
    colors = listOf(Color(0xFF059669), Color(0xFF10B981))
)
val SolemGradientCard = Brush.verticalGradient(
    colors = listOf(Color(0xFF192135), Color(0xFF131826))
)
