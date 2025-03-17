package com.ema.salud_timer.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ema.salud_timer.R
import java.util.Locale

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val medicamentoId = intent.getIntExtra("medicamento_id", -1)
        val nombreMedicamento = intent.getStringExtra("nombre_medicamento")
        val dosisMedicamento = intent.getStringExtra("dosis_medicamento")
        val tipoMedicamento = intent.getStringExtra("tipo_medicamento")

        val unidadPredeterminada = when (tipoMedicamento?.lowercase(Locale.getDefault())) {
            "pastilla" -> "tableta(s)"
            "jarabe" -> "ml"
            else -> "dosis"
        }

        if (medicamentoId != -1 && nombreMedicamento != null && dosisMedicamento != null) {
            mostrarNotification(context, nombreMedicamento, dosisMedicamento, unidadPredeterminada)
        }
    }

    private fun mostrarNotification(context: Context, nombreMedicamento: String, dosisMedicamento: String, unidadPredeterminada: String) {
        if (nombreMedicamento.isNullOrEmpty()) {
            Log.e("AlarmReceiver", "Nombre del medicamento está vacío o es nulo, no se puede mostrar la notificación")
            return
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val mensajeNotificacion = "Debes tomar tu medicamento: $nombreMedicamento ($dosisMedicamento $unidadPredeterminada)"

        val notification = NotificationCompat.Builder(context, "canal_de_medicamento")
            .setContentTitle("Es hora de tomar el medicamento")
            .setContentText(mensajeNotificacion)
            .setSmallIcon(R.drawable.medicamento)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(nombreMedicamento.hashCode(), notification)
        Log.d("AlarmReceiver", "Notificación mostrada para medicamento: $nombreMedicamento")
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "canal_de_medicamento"
            val channelName = "Medicamentos"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Canal para notificaciones de medicamentos"
            }

            // Obtén el NotificationManager y crea el canal
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

}