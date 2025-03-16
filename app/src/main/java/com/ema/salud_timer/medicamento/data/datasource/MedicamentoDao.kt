package com.ema.salud_timer.medicamento.data.datasource

import androidx.room.*
import com.ema.salud_timer.medicamento.data.model.MedicamentoEntity
import com.ema.salud_timer.medicamento.data.model.MedicamentoWithPersona
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos WHERE personaId = :personaId")
    fun getMedicamentosByPersonaId(personaId: Int): Flow<List<MedicamentoEntity>>

    @Query("SELECT * FROM medicamentos WHERE id = :id")
    fun getMedicamentoById(id: Int): Flow<MedicamentoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicamento(medicamento: MedicamentoEntity): Long

    @Update
    suspend fun updateMedicamento(medicamento: MedicamentoEntity)

    @Query("UPDATE medicamentos SET activo = :activo WHERE id = :id")
    suspend fun updateMedicamentoActivo(id: Int, activo: Boolean)

    @Delete
    suspend fun deleteMedicamento(medicamento: MedicamentoEntity)

    @Transaction
    @Query("SELECT * FROM medicamentos WHERE personaId = :personaId")
    fun getMedicamentosWithPersona(personaId: Int): Flow<List<MedicamentoWithPersona>>
}