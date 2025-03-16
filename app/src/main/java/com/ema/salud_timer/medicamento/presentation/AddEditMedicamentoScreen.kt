package com.ema.salud_timer.medicamento.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicamentoScreen(
    medicamentoId: Int? = null,
    personaId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: MedicamentoViewModel = hiltViewModel()
) {
    val nombre by viewModel.nombre.collectAsState()
    val descripcion by viewModel.descripcion.collectAsState()
    val dosis by viewModel.dosis.collectAsState()
    val frecuencia by viewModel.frecuencia.collectAsState()
    val horaInicio by viewModel.horaInicio.collectAsState()
    val horaFija by viewModel.horaFija.collectAsState()
    val intervaloHoras by viewModel.intervaloHoras.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val error by viewModel.error.collectAsState()

    val frecuenciaOptions = listOf("Cada X horas", "Hora fija", "Días específicos")
    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    val showTimePicker = remember { mutableStateOf(false) }
    val timePickerFor = remember { mutableStateOf("") }

    // Efecto para navegar hacia atrás cuando se guarda con éxito
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onNavigateBack()
            Log.e("AddEditMedicamentoScreen", "¡Ambos ID son nulos! personaId=$personaId, medicamentoId=$medicamentoId")

        }
    }

    // Mostrar Snackbar para errores
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (medicamentoId == null) "Nuevo Medicamento" else "Editar Medicamento"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Campos de formulario
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { viewModel.onNombreChange(it) },
                    label = { Text("Nombre del medicamento") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { viewModel.onDescripcionChange(it) },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dosis,
                    onValueChange = { viewModel.onDosisChange(it) },
                    label = { Text("Dosis") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Horario y frecuencia",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selector de tipo de frecuencia
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { /* Para mantener simple este ejemplo, no implementamos la lógica completa */ }
                ) {
                    OutlinedTextField(
                        value = frecuencia,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Tipo de frecuencia") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = false,
                        onDismissRequest = {}
                    ) {
                        frecuenciaOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.onFrecuenciaChange(option)
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campos específicos según el tipo de frecuencia
                when (frecuencia) {
                    "Cada X horas" -> {
                        // Intervalo de horas
                        OutlinedTextField(
                            value = intervaloHoras,
                            onValueChange = { viewModel.onIntervaloHorasChange(it) },
                            label = { Text("Cantidad de horas") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hora de inicio
                        OutlinedTextField(
                            value = horaInicio ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora de inicio") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    timePickerFor.value = "inicio"
                                    showTimePicker.value = true
                                },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = "Seleccionar hora"
                                )
                            }
                        )
                    }
                    "Hora fija" -> {
                        // Hora fija
                        OutlinedTextField(
                            value = horaFija ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora fija diaria") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    timePickerFor.value = "fija"
                                    showTimePicker.value = true
                                },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = "Seleccionar hora"
                                )
                            }
                        )
                    }
                    "Días específicos" -> {
                        // Días de la semana
                        Text(
                            "Seleccione los días",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Chips para días de la semana
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            diasSemana.forEach { dia ->
                                FilterChip(
                                    selected = selectedDays.contains(dia),
                                    onClick = { viewModel.toggleDaySelection(dia) },
                                    label = {
                                        Text(dia.substring(0, 1))
                                    },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hora fija para los días seleccionados
                        OutlinedTextField(
                            value = horaFija ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora en días seleccionados") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    timePickerFor.value = "fija"
                                    showTimePicker.value = true
                                },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = "Seleccionar hora"
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.saveMedicamento() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && nombre.isNotBlank() && dosis.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Guardar medicamento")
                    }
                }
            }
        }
    }

    // Esto sería un TimePickerDialog pero por simplicidad no lo implemento completamente
    if (showTimePicker.value) {
        // Implementar diálogo de selección de hora
        // Este es un placeholder simplificado
        AlertDialog(
            onDismissRequest = { showTimePicker.value = false },
            title = { Text("Seleccionar hora") },
            text = {
                Text("Aquí iría el selector de hora")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Simular selección de hora
                        val horaSeleccionada = "08:00"
                        if (timePickerFor.value == "inicio") {
                            viewModel.onHoraInicioChange(horaSeleccionada)
                        } else {
                            viewModel.onHoraFijaChange(horaSeleccionada)
                        }
                        showTimePicker.value = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}