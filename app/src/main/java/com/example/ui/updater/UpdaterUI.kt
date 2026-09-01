package com.example.ui.updater

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import com.example.ui.theme.*

@Composable
fun UpdateChecker(currentVersion: String) {
    var newVersionData by remember { mutableStateOf<JSONObject?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Endpoint fijo en github donde pondrás el version.json
                val url = "https://raw.githubusercontent.com/LucasFluxa/Solem./main/version.json"
                val jsonString = URL(url).readText()
                val json = JSONObject(jsonString)
                val remoteVersion = json.getString("version")
                
                // Comparación ultra básica
                if (remoteVersion != currentVersion) {
                    newVersionData = json
                    showUpdateDialog = true
                }
            } catch (e: Exception) {
                // Ignore internet errors silently
            }
        }
    }

    if (showUpdateDialog && newVersionData != null) {
        val version = newVersionData!!.getString("version")
        val changelogArray = newVersionData!!.optJSONArray("changelog")
        val downloadUrl = newVersionData!!.optString("downloadUrl", "https://github.com/LucasFluxa/Solem./releases/latest")
        
        val changelog = buildString {
            if (changelogArray != null) {
                for (i in 0 until changelogArray.length()) {
                    append("• ").append(changelogArray.getString(i)).append("\n")
                }
            } else {
                append("¡Descubre las nuevas funcionalidades!")
            }
        }

        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = SolemAccentCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¡Nueva versión!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SolemTextPrimary)
                }
            },
            text = {
                Column {
                    Text("La versión $version ya está disponible.", style = MaterialTheme.typography.bodyMedium, color = SolemTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Novedades:", fontWeight = FontWeight.SemiBold, color = SolemTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(changelog.trim(), style = MaterialTheme.typography.bodySmall, color = SolemTextMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                        context.startActivity(intent)
                        showUpdateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolemAccentCyan)
                ) {
                    Text("Descargar ahora")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Más tarde", color = SolemTextMuted)
                }
            },
            containerColor = SolemBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
