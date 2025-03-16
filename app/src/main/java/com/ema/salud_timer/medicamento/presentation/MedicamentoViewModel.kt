package com.ema.salud_timer.medicamento.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ema.salud_timer.medicamento.data.model.MedicamentoEntity
import com.ema.salud_timer.medicamento.domain.MedicamentoUseCase
import com.ema.salud_timer.persona.data.model.PersonaEntity
import com.ema.salud_timer.persona.domain.PersonaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MedicamentoViewModel @Inject constructor(
    private val medicamentoUseCase: MedicamentoUseCase,
    private val personaUseCase: PersonaUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ID de la persona seleccionada
    private val personaId: Int? = savedStateHandle["personaId"]

    // ID del medicamento a editar (si se está editando)
    private val medicamentoId: Int? = savedStateHandle["medicamentoId"]

    // Detalles de la persona
    private val _persona = MutableStateFlow<PersonaEntity?>(null)
    val persona: StateFlow<PersonaEntity?> = _persona

    // Lista de medicamentos
    val medicamentos = personaId?.let {
        medicamentoUseCase.getMedicamentosByPersonaId(it)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } ?: MutableStateFlow(emptyList())

    // Estados para agregar/editar medicamento
    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre

    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion

    private val _dosis = MutableStateFlow("")
    val dosis: StateFlow<String> = _dosis

    private val _frecuencia = MutableStateFlow("Cada X horas")
    val frecuencia: StateFlow<String> = _frecuencia

    private val _horaInicio = MutableStateFlow<String?>(null)
    val horaInicio: StateFlow<String?> = _horaInicio

    private val _horaFija = MutableStateFlow<String?>(null)
    val horaFija: StateFlow<String?> = _horaFija

    private val _intervaloHoras = MutableStateFlow("8")
    val intervaloHoras: StateFlow<String> = _intervaloHoras

    private val _selectedDays = MutableStateFlow<List<String>>(emptyList())
    val selectedDays: StateFlow<List<String>> = _selectedDays

    private val _fechaInicio = MutableStateFlow(Calendar.getInstance().time)
    val fechaInicio: StateFlow<Date> = _fechaInicio

    private val _fechaFin = MutableStateFlow<Date?>(null)
    val fechaFin: StateFlow<Date?> = _fechaFin

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        loadData()
    }

    // Parte crítica de MedicamentoViewModel.kt
    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Cargar datos de la persona
                personaId?.let { id ->
                    personaUseCase.getPersonaById(id)
                        .catch { e ->
                            _error.value = "Error al cargar datos de persona: ${e.message}"
                            _isLoading.value = false // Importante terminar la carga en caso de error
                        }
                        .collect { persona ->
                            _persona.value = persona
                            _isLoading.value = false // Importante terminar la carga cuando se completa
                        }
                } ?: run {
                    // Si personaId es null, finalizar la carga
                    _isLoading.value = false
                    _error.value = "ID de persona no proporcionado"
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar datos: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun onNombreChange(value: String) {
        _nombre.value = value
    }

    fun onDescripcionChange(value: String) {
        _descripcion.value = value
    }

    fun onDosisChange(value: String) {
        _dosis.value = value
    }

    fun onFrecuenciaChange(value: String) {
        _frecuencia.value = value

        // Resetear campos específicos según el tipo de frecuencia
        when (value) {
            "Cada X horas" -> {
                _horaFija.value = null
                _selectedDays.value = emptyList()

                // Establecer hora de inicio por defecto si está vacía
                if (_horaInicio.value == null) {
                    val calendar = Calendar.getInstance()
                    _horaInicio.value = dateFormat.format(calendar.time)
                }
            }
            "Hora fija" -> {
                _intervaloHoras.value = "8"
                _selectedDays.value = emptyList()

                // Establecer hora fija por defecto si está vacía
                if (_horaFija.value == null) {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.HOUR_OF_DAY, 1) // Próxima hora
                    calendar.set(Calendar.MINUTE, 0)
                    _horaFija.value = dateFormat.format(calendar.time)
                }
            }
            "Días específicos" -> {
                _intervaloHoras.value = "8"
                _horaInicio.value = null

                // Establecer hora fija por defecto si está vacía
                if (_horaFija.value == null) {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.HOUR_OF_DAY, 1)
                    calendar.set(Calendar.MINUTE, 0)
                    _horaFija.value = dateFormat.format(calendar.time)
                }
            }
        }
    }

    fun onIntervaloHorasChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d+$"))) {
            _intervaloHoras.value = value
        }
    }

    fun onHoraInicioChange(value: String) {
        _horaInicio.value = value
    }

    fun onHoraFijaChange(value: String) {
        _horaFija.value = value
    }

    fun toggleDaySelection(day: String) {
        val currentSelection = _selectedDays.value.toMutableList()

        if (currentSelection.contains(day)) {
            currentSelection.remove(day)
        } else {
            currentSelection.add(day)
        }

        _selectedDays.value = currentSelection
    }

    fun onFechaInicioChange(date: Date) {
        _fechaInicio.value = date
    }

    fun onFechaFinChange(date: Date?) {
        _fechaFin.value = date
    }

    fun saveMedicamento() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val personaIdValue = personaId
                if (personaIdValue == null) {
                    _error.value = "No se ha seleccionado una persona"
                    return@launch
                }

                val diasSemanaValue = if (_selectedDays.value.isNotEmpty()) {
                    _selectedDays.value.joinToString(",")
                } else {
                    null
                }

                val result = medicamentoUseCase.saveMedicamento(
                    id = medicamentoId ?: 0,
                    nombre = _nombre.value,
                    descripcion = _descripcion.value,
                    dosis = _dosis.value,
                    frecuencia = if (_frecuencia.value == "Cada X horas") "Cada ${_intervaloHoras.value} horas" else _frecuencia.value,
                    horaInicio = _horaInicio.value,
                    horaFija = _horaFija.value,
                    diasSemana = diasSemanaValue,
                    fechaInicio = _fechaInicio.value,
                    fechaFin = _fechaFin.value,
                    personaId = personaIdValue
                )

                if (result > 0) {
                    _saveSuccess.value = true
                } else {
                    _error.value = "Error al guardar el medicamento"
                }
            } catch (e: IllegalArgumentException) {
                _error.value = e.message
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMedicamento() {
        medicamentoId?.let { id ->
            viewModelScope.launch {
                _isLoading.value = true

                try {
                    val medicamento = medicamentoUseCase.getMedicamentoById(id).first()
                    medicamentoUseCase.deleteMedicamento(medicamento)
                    _saveSuccess.value = true
                } catch (e: Exception) {
                    _error.value = "Error al eliminar: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun toggleMedicamentoActivo(id: Int, activo: Boolean) {
        viewModelScope.launch {
            try {
                medicamentoUseCase.toggleMedicamentoActivo(id, activo)
            } catch (e: Exception) {
                _error.value = "Error al cambiar estado: ${e.message}"
            }
        }
    }

    fun resetState() {
        _saveSuccess.value = false
        _error.value = null
    }
}