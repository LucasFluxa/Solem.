package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

class ClassAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sigla = intent.getStringExtra("EXTRA_SIGLA") ?: return
        val tipo = intent.getStringExtra("EXTRA_TIPO") ?: "Clase"
        val nombre = intent.getStringExtra("EXTRA_NOMBRE") ?: ""
        val sala = intent.getStringExtra("EXTRA_SALA") ?: "Sala por definir"
        val bloqueStr = intent.getStringExtra("EXTRA_BLOQUE") ?: ""
        val minutesBefore = intent.getIntExtra("EXTRA_MINUTES_BEFORE", 15)

        showClassNotification(context, sigla, nombre, tipo, sala, bloqueStr, minutesBefore)
    }

    companion object {
        fun showClassNotification(
            context: Context,
            sigla: String,
            nombre: String,
            tipo: String,
            sala: String,
            bloqueStr: String,
            minutesBefore: Int = 15
        ) {
            val channelId = "class_reminders_channel"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Recordatorios de Clases",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Avisos de sala y horario de tus próximas clases"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = "$sigla en $minutesBefore min"
            val displayNombre = if (nombre.isNotBlank()) nombre else tipo
            val content = "$displayNombre · Sala $sala"

            val bigText = if (bloqueStr.isNotBlank()) {
                "$displayNombre · Sala $sala\n$bloqueStr"
            } else {
                content
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(com.example.R.drawable.ic_notification_logo)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            try {
                NotificationManagerCompat.from(context).notify(sigla.hashCode(), notification)
            } catch (e: SecurityException) {
                // Android 13+ permission catch
            }
        }
    }
}
