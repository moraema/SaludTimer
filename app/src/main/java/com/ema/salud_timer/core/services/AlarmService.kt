package com.ema.salud_timer.core.services

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import com.ema.salud_timer.medicamento.data.model.MedicamentoEntity
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class AlarmService @Inject constructor(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager



    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarma(medicamento: MedicamentoEntity) {
        Log.d("AlarmService", "Programando alarma para medicamento: ${medicamento.nombre} y fecha ${medicamento.fechaInicio} y hora ${medicamento.horaFija}")
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("medicamento_id", medicamento.id)
            putExtra("nombre_medicamento", medicamento.nombre)
            putExtra("dosis_medicamento", medicamento.dosis)
            putExtra("tipo_medicamento", medicamento.tipoMedicamento)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicamento.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            time = medicamento.fechaInicio
            if (medicamento.horaInicio != null) {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val horaInicioDate = sdf.parse(medicamento.horaInicio)
                if (horaInicioDate != null) {
                    set(Calendar.HOUR_OF_DAY, horaInicioDate.hours)
                    set(Calendar.MINUTE, horaInicioDate.minutes)
                    set(Calendar.SECOND, 0)
                }
            }
        }

        if (medicamento.frecuencia == "intervalo" && medicamento.intervaloHoras != null) {
            val intervaloMillis = medicamento.intervaloHoras * 60 * 60 * 1000L
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                intervaloMillis,
                pendingIntent
            )
        } else if (medicamento.frecuencia == "hora_fija" && medicamento.horaFija != null) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val horaFijaDate = sdf.parse(medicamento.horaFija)

            if (horaFijaDate != null) {
                calendar.set(Calendar.HOUR_OF_DAY, horaFijaDate.hours)
                calendar.set(Calendar.MINUTE, horaFijaDate.minutes)
                calendar.set(Calendar.SECOND, 0)

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }
    }
}