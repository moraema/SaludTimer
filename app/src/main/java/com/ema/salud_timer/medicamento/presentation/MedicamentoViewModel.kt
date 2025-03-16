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

    // Tipo de medicamento (pastilla o jarabe)
    private val _tipoMedicamento = MutableStateFlow("pastilla")
    val tipoMedicamento: StateFlow<String> = _tipoMedicamento

    private val _dosis = MutableStateFlow("")
    val dosis: StateFlow<String> = _dosis

    // Tipo de frecuencia (hora_fija o intervalo)
    private val _tipoFrecuencia = MutableStateFlow("hora_fija")
    val tipoFrecuencia: StateFlow<String> = _tipoFrecuencia

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

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Cargar datos de la persona
                personaId?.let { id ->
                    personaUseCase.getPersonaById(id)
                        .catch { e ->
                            _error.value = "Error al cargar datos de persona: ${e.message}"
                            _isLoading.value = false
                        }
                        .collect { persona ->
                            _persona.value = persona

                            // Cargar datos del medicamento si existe ID
                            loadMedicamento()

                            _isLoading.value = false
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

    private fun loadMedicamento() {
        medicamentoId?.let { id ->
            viewModelScope.launch {
                try {
                    medicamentoUseCase.getMedicamentoById(id)
                        .collect { medicamento ->
                            // Cargar datos básicos del medicamento
                            _nombre.value = medicamento.nombre
                            _descripcion.value = medicamento.descripcion
                            _tipoMedicamento.value = medicamento.tipoMedicamento ?: "pastilla"
                            _dosis.value = medicamento.dosis

                            // Cargar tipo de frecuencia
                            _tipoFrecuencia.value = medicamento.frecuencia

                            // Cargar datos específicos según el tipo de frecuencia
                            if (_tipoFrecuencia.value == "intervalo") {
                                _intervaloHoras.value = medicamento.intervaloHoras?.toString() ?: "8"
                                _horaInicio.value = medicamento.horaInicio
                            } else {
                                _horaFija.value = medicamento.horaFija

                                // Cargar días seleccionados si existen
                                medicamento.diasSemana?.let { dias ->
                                    _selectedDays.value = dias.split(",")
                                }
                            }

                            // Cargar fechas
                            _fechaInicio.value = medicamento.fechaInicio
                            _fechaFin.value = medicamento.fechaFin
                        }
                } catch (e: Exception) {
                    _error.value = "Error al cargar medicamento: ${e.message}"
                }
            }
        }
    }

    fun onNombreChange(value: String) {
        _nombre.value = value
    }

    fun onDescripcionChange(value: String) {
        _descripcion.value = value
    }

    fun onTipoMedicamentoChange(value: String) {
        _tipoMedicamento.value = value
    }

    fun onDosisChange(value: String) {
        _dosis.value = value
    }

    fun onTipoFrecuenciaChange(value: String) {
        _tipoFrecuencia.value = value

        // Resetear campos específicos según el tipo de frecuencia
        if (value == "hora_fija") {
            _intervaloHoras.value = ""
            _horaInicio.value = null

            // Establecer hora fija por defecto si está vacía
            if (_horaFija.value == null) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.HOUR_OF_DAY, 1)
                calendar.set(Calendar.MINUTE, 0)
                _horaFija.value = dateFormat.format(calendar.time)
            }
        } else { // intervalo
            _horaFija.value = null
            _selectedDays.value = emptyList()

            // Valores por defecto para intervalos
            if (_intervaloHoras.value.isEmpty()) {
                _intervaloHoras.value = "8"
            }

            // Establecer hora de inicio por defecto si está vacía
            if (_horaInicio.value == null) {
                val calendar = Calendar.getInstance()
                _horaInicio.value = dateFormat.format(calendar.time)
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

                val intervaloHorasValue = if (_tipoFrecuencia.value == "intervalo") {
                    _intervaloHoras.value.toIntOrNull()
                } else {
                    null
                }

                val result = medicamentoUseCase.saveMedicamento(
                    id = medicamentoId ?: 0,
                    nombre = _nombre.value,
                    descripcion = _descripcion.value,
                    tipoMedicamento = _tipoMedicamento.value,
                    dosis = _dosis.value,
                    frecuencia = _tipoFrecuencia.value,
                    horaInicio = if (_tipoFrecuencia.value == "intervalo") _horaInicio.value else null,
                    horaFija = if (_tipoFrecuencia.value == "hora_fija") _horaFija.value else null,
                    intervaloHoras = intervaloHorasValue,
                    diasSemana = if (_tipoFrecuencia.value == "hora_fija") diasSemanaValue else null,
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