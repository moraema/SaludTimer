package com.ema.salud_timer.medicamento.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ema.salud_timer.persona.data.model.PersonaEntity
import java.util.Date

@Entity(
    tableName = "medicamentos",
    foreignKeys = [
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["personaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personaId")]
)
data class MedicamentoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val tipoMedicamento: String, // "jarabe" o "pastilla"
    val dosis: String,
    val frecuencia: String, // "hora_fija" o "intervalo"
    val horaInicio: String? = null,
    val horaFija: String? = null,
    val intervaloHoras: Int? = null,
    val diasSemana: String? = null,
    val fechaInicio: Date,
    val fechaFin: Date? = null,
    val personaId: Int,
    val activo: Boolean = true
)