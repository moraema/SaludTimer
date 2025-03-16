package com.ema.salud_timer.di

import android.content.Context
import com.ema.salud_timer.core.database.AppDatabase
import com.ema.salud_timer.medicamento.data.repository.MedicamentoRepository
import com.ema.salud_timer.medicamento.domain.MedicamentoUseCase
import com.ema.salud_timer.persona.data.repository.PersonaRepository
import com.ema.salud_timer.persona.domain.PersonaUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    // Persona
    @Provides
    @Singleton
    fun providePersonaRepository(database: AppDatabase): PersonaRepository {
        return PersonaRepository(database.personaDao())
    }

    @Provides
    @Singleton
    fun providePersonaUseCase(repository: PersonaRepository): PersonaUseCase {
        return PersonaUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMedicamentoRepository(database: AppDatabase): MedicamentoRepository {
        return MedicamentoRepository(database.medicamentoDao())
    }

    @Provides
    @Singleton
    fun provideMedicamentoUseCase(repository: MedicamentoRepository): MedicamentoUseCase {
        return MedicamentoUseCase(repository)
    }
}