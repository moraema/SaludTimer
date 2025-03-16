package com.ema.salud_timer.persona.data.repository

import com.ema.salud_timer.persona.data.datasource.PersonaDao
import com.ema.salud_timer.persona.data.model.PersonaEntity
import kotlinx.coroutines.flow.Flow

class PersonaRepository(private val personaDao: PersonaDao) {
    fun getAllPersonas(): Flow<List<PersonaEntity>> = personaDao.getAll()

    fun getPersonaById(id: Int): Flow<PersonaEntity> = personaDao.getById(id)

    suspend fun insertPersona(persona: PersonaEntity): Long = personaDao.insert(persona)

    suspend fun updatePersona(persona: PersonaEntity) = personaDao.update(persona)

    suspend fun deletePersona(persona: PersonaEntity) = personaDao.delete(persona)
}