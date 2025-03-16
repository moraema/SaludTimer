package com.ema.salud_timer.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ema.salud_timer.medicamento.data.datasource.MedicamentoDao
import com.ema.salud_timer.medicamento.data.model.MedicamentoEntity
import com.ema.salud_timer.persona.data.datasource.PersonaDao
import com.ema.salud_timer.persona.data.model.PersonaEntity

@Database(
    entities = [PersonaEntity::class, MedicamentoEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
    abstract fun medicamentoDao(): MedicamentoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saludtimer_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}