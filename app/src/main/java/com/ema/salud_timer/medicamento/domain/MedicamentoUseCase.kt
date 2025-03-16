package com.ema.salud_timer.medicamento.domain

import com.ema.salud_timer.medicamento.data.model.MedicamentoEntity
import com.ema.salud_timer.medicamento.data.model.MedicamentoWithPersona
import com.ema.salud_timer.medicamento.data.repository.MedicamentoRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class MedicamentoUseCase @Inject constructor(private val repository: MedicamentoRepository) {
    fun getMedicamentosByPersonaId(personaId: Int): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosByPersonaId(personaId)

    fun getMedicamentoById(id: Int): Flow<MedicamentoEntity> =
        repository.getMedicamentoById(id)

    fun getMedicamentosWithPersona(personaId: Int): Flow<List<MedicamentoWithPersona>> =
        repository.getMedicamentosWithPersona(personaId)

    suspend fun saveMedicamento(
        id: Int = 0,
        nombre: String,
        descripcion: String,
        tipoMedicamento: String,
        dosis: String,
        frecuencia: String,
        horaInicio: String? = null,
        horaFija: String? = null,
        intervaloHoras: Int? = null,
        diasSemana: String? = null,
        fechaInicio: Date,
        fechaFin: Date? = null,
        personaId: Int,
        activo: Boolean = true
    ): Long {
        // Validaciones
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(dosis.isNotBlank()) { "La dosis no puede estar vacía" }

        // Validación específica según el tipo de frecuencia
        when (frecuencia) {
            "intervalo" -> {
                require(intervaloHoras != null && intervaloHoras > 0) { "El intervalo de horas debe ser mayor a 0" }
                require(horaInicio != null) { "La hora de inicio es requerida para frecuencia por intervalos" }
            }
            "hora_fija" -> {
                require(horaFija != null) { "La hora fija es requerida para este tipo de frecuencia" }
            }
        }

        val medicamento = MedicamentoEntity(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            tipoMedicamento = tipoMedicamento,
            dosis = dosis,
            frecuencia = frecuencia,
            horaInicio = horaInicio,
            horaFija = horaFija,
            intervaloHoras = intervaloHoras,
            diasSemana = diasSemana,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            personaId = personaId,
            activo = activo
        )

        return repository.saveMedicamento(medicamento)
    }

    suspend fun updateMedicamento(medicamento: MedicamentoEntity) =
        repository.updateMedicamento(medicamento)

    suspend fun toggleMedicamentoActivo(id: Int, activo: Boolean) =
        repository.toggleMedicamentoActivo(id, activo)

    suspend fun deleteMedicamento(medicamento: MedicamentoEntity) =
        repository.deleteMedicamento(medicamento)
}