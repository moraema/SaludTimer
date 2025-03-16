package com.ema.salud_timer.medicamento.data.repository

import com.ema.salud_timer.medicamento.data.datasource.MedicamentoDao
import com.ema.salud_timer.medicamento.data.model.MedicamentoEntity
import com.ema.salud_timer.medicamento.data.model.MedicamentoWithPersona
import kotlinx.coroutines.flow.Flow

class MedicamentoRepository(private val medicamentoDao: MedicamentoDao) {
    fun getMedicamentosByPersonaId(personaId: Int): Flow<List<MedicamentoEntity>> =
        medicamentoDao.getMedicamentosByPersonaId(personaId)

    fun getMedicamentoById(id: Int): Flow<MedicamentoEntity> =
        medicamentoDao.getMedicamentoById(id)

    fun getMedicamentosWithPersona(personaId: Int): Flow<List<MedicamentoWithPersona>> =
        medicamentoDao.getMedicamentosWithPersona(personaId)

    suspend fun saveMedicamento(medicamento: MedicamentoEntity): Long =
        medicamentoDao.insertMedicamento(medicamento)

    suspend fun updateMedicamento(medicamento: MedicamentoEntity) =
        medicamentoDao.updateMedicamento(medicamento)

    suspend fun toggleMedicamentoActivo(id: Int, activo: Boolean) =
        medicamentoDao.updateMedicamentoActivo(id, activo)

    suspend fun deleteMedicamento(medicamento: MedicamentoEntity) =
        medicamentoDao.deleteMedicamento(medicamento)
}