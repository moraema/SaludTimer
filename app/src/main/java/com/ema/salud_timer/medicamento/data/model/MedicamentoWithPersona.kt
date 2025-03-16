package com.ema.salud_timer.medicamento.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ema.salud_timer.persona.data.model.PersonaEntity

data class MedicamentoWithPersona(
    @Embedded val medicamento: MedicamentoEntity,
    @Relation(
        parentColumn = "personaId",
        entityColumn = "id"
    )
    val persona: PersonaEntity
)