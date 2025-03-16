package com.ema.salud_timer.persona.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ema.salud_timer.persona.data.model.PersonaEntity
import com.ema.salud_timer.persona.domain.PersonaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonaViewModel @Inject constructor(
    private val personaUseCase: PersonaUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val personas: StateFlow<List<PersonaEntity>> =
        personaUseCase.getAllPersonas()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Para edición
    private val personaId: Int? = savedStateHandle["personaId"]

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre

    private val _apellidos = MutableStateFlow("")
    val apellidos: StateFlow<String> = _apellidos

    private val _edad = MutableStateFlow("")
    val edad: StateFlow<String> = _edad

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    // Agregar el estado de error que faltaba
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadPersona()
    }

    private fun loadPersona() {
        personaId?.let { id ->
            viewModelScope.launch {
                _isLoading.value = true
                personaUseCase.getPersonaById(id)
                    .catch { e ->
                        _isLoading.value = false
                        _error.value = "Error al cargar: ${e.message}"
                    }
                    .collect { persona ->
                        if (persona != null) {
                            _nombre.value = persona.nombre
                            _apellidos.value = persona.apellidos
                            _edad.value = persona.edad.toString()
                        } else {
                            _error.value = "El perfil ya no existe"
                        }
                        _isLoading.value = false
                    }
            }
        }
    }

    fun onNombreChange(value: String) {
        _nombre.value = value
    }

    fun onApellidosChange(value: String) {
        _apellidos.value = value
    }

    fun onEdadChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d+$"))) {
            _edad.value = value
        }
    }

    fun savePersona() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val edadInt = _edad.value.toIntOrNull() ?: 0

                val persona = PersonaEntity(
                    id = personaId ?: 0,
                    nombre = _nombre.value,
                    apellidos = _apellidos.value,
                    edad = edadInt
                )

                if (personaId == null) {
                    // Nueva persona
                    personaUseCase.savePersona(persona)
                } else {
                    // Actualizar persona existente
                    personaUseCase.updatePersona(persona)
                }

                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Error al guardar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePersona() {
        personaId?.let { id ->
            viewModelScope.launch {
                _isLoading.value = true

                try {
                    // Intentar obtener la persona actual
                    val personaFlow = personaUseCase.getPersonaById(id)
                    val persona = personaFlow.firstOrNull()

                    if (persona != null) {
                        // Eliminar la persona
                        personaUseCase.deletePersona(persona)
                        _saveSuccess.value = true
                    } else {
                        _error.value = "No se pudo encontrar el perfil para eliminar"
                    }
                } catch (e: Exception) {
                    _error.value = "Error al eliminar: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun resetState() {
        _saveSuccess.value = false
        _error.value = null
    }
}