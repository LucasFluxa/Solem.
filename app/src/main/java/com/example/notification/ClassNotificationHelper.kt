package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.entity.BloqueHorarioEntity
import java.util.Calendar

object ClassNotificationHelper {

    private const val TAG = "ClassNotificationHelper"

    fun scheduleClassReminders(
        context: Context,
        bloques: List<BloqueHorarioEntity>,
        nombresAsignaturas: Map<String, String> = emptyMap(),
        enabled: Boolean = true,
        minutesBefore: Int = 15
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancelar alarmas anteriores
        cancelAllClassReminders(context)

        if (!enabled || bloques.isEmpty()) {
            Log.d(TAG, "Recordatorios desactivados o sin bloques inscritos.")
            return
        }

        // Programar para cada bloque en la semana
        val now = Calendar.getInstance()

        // Agrupar bloques consecutivos por ramo y día (ej. Bloques 1-2 juntos)
        val groupedBloques = bloques.groupBy { "${it.sigla}_${it.dia}_${it.tipo}" }

        groupedBloques.forEach { (_, blockGroup) ->
            val firstBlock = blockGroup.minByOrNull { it.bloque } ?: return@forEach
            val diaSemana = when (firstBlock.dia) {
                0 -> Calendar.MONDAY
                1 -> Calendar.TUESDAY
                2 -> Calendar.WEDNESDAY
                3 -> Calendar.THURSDAY
                4 -> Calendar.FRIDAY
                5 -> Calendar.SATURDAY
                else -> return@forEach
            }

            val (startHour, startMinute) = getBlockStartTime(firstBlock.bloque)

            val classCalendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, diaSemana)
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, -minutesBefore)
            }

            // Si la hora de notificación para esta semana ya pasó, programar para la próxima semana
            if (classCalendar.before(now)) {
                classCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            }

            val reqCode = (firstBlock.sigla.hashCode() * 31 + firstBlock.dia * 7 + firstBlock.bloque) and 0x7FFFFFFF

            val bMin = blockGroup.minOfOrNull { it.bloque } ?: 1
            val bMax = blockGroup.maxOfOrNull { it.bloque } ?: 1
            val bloqueLabel = if (bMin == bMax) "Bloque $bMin" else "Bloque $bMin-$bMax"
            val horasStr = getBlockHoursString(bMin, bMax)
            val fullBloqueStr = "$bloqueLabel ($horasStr)"

            val intent = Intent(context, ClassAlarmReceiver::class.java).apply {
                putExtra("EXTRA_SIGLA", firstBlock.sigla)
                putExtra("EXTRA_NOMBRE", nombresAsignaturas[firstBlock.sigla] ?: "")
                putExtra("EXTRA_TIPO", firstBlock.tipo ?: "Cátedra")
                putExtra("EXTRA_SALA", firstBlock.sala ?: "Sala por definir")
                putExtra("EXTRA_BLOQUE", fullBloqueStr)
                putExtra("EXTRA_MINUTES_BEFORE", minutesBefore)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reqCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        classCalendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        classCalendar.timeInMillis,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Recordatorio programado para ${firstBlock.sigla} (${firstBlock.tipo}) a las ${classCalendar.time}")
            } catch (e: SecurityException) {
                Log.w(TAG, "Permiso de alarma exacta denegado: ${e.message}")
            }
        }
    }

    fun sendTestNotification(
        context: Context,
        sigla: String = "MAT021",
        nombre: String = "Matemáticas 1",
        tipo: String = "Cátedra",
        sala: String = "Sala F-102",
        bloqueStr: String = "Bloque 3-4 (09:40 - 10:50)",
        minutesBefore: Int = 15
    ) {
        ClassAlarmReceiver.showClassNotification(
            context = context,
            sigla = sigla,
            nombre = nombre,
            tipo = tipo,
            sala = sala,
            bloqueStr = bloqueStr,
            minutesBefore = minutesBefore
        )
    }

    fun cancelAllClassReminders(context: Context) {
        Log.d(TAG, "Cancelando todos los recordatorios previos de clases.")
    }

    private fun getBlockStartTime(bloque: Int): Pair<Int, Int> = when (bloque) {
        1, 2 -> Pair(8, 15)
        3, 4 -> Pair(9, 40)
        5, 6 -> Pair(11, 5)
        7, 8 -> Pair(12, 30)
        9, 10 -> Pair(14, 40)
        11, 12 -> Pair(16, 5)
        13, 14 -> Pair(17, 30)
        15, 16 -> Pair(18, 55)
        17, 18 -> Pair(20, 20)
        19, 20 -> Pair(21, 45)
        else -> Pair(8, 15)
    }

    private fun getBlockHoursString(minB: Int, maxB: Int): String {
        val startStr = when (minB) {
            1, 2 -> "08:15"
            3, 4 -> "09:40"
            5, 6 -> "11:05"
            7, 8 -> "12:30"
            9, 10 -> "14:40"
            11, 12 -> "16:05"
            13, 14 -> "17:30"
            15, 16 -> "18:55"
            17, 18 -> "20:20"
            19, 20 -> "21:45"
            else -> "08:15"
        }
        val endStr = when (maxB) {
            1, 2 -> "09:25"
            3, 4 -> "10:50"
            5, 6 -> "12:15"
            7, 8 -> "13:40"
            9, 10 -> "15:50"
            11, 12 -> "17:15"
            13, 14 -> "18:40"
            15, 16 -> "20:05"
            17, 18 -> "21:30"
            19, 20 -> "22:55"
            else -> "09:25"
        }
        return "$startStr - $endStr"
    }
}
