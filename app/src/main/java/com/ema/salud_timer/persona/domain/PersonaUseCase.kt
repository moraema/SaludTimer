package com.ema.salud_timer.persona.domain

import com.ema.salud_timer.persona.data.model.PersonaEntity
import com.ema.salud_timer.persona.data.repository.PersonaRepository
import kotlinx.coroutines.flow.Flow

class PersonaUseCase(private val repository: PersonaRepository) {
    fun getAllPersonas(): Flow<List<PersonaEntity>> = repository.getAllPersonas()

    fun getPersonaById(id: Int): Flow<PersonaEntity> = repository.getPersonaById(id)

    suspend fun savePersona(persona: PersonaEntity): Long = repository.insertPersona(persona)

    suspend fun updatePersona(persona: PersonaEntity) = repository.updatePersona(persona)

    suspend fun deletePersona(persona: PersonaEntity) = repository.deletePersona(persona)
}