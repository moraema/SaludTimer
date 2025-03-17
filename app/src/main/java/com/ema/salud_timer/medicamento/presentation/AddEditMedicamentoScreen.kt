package com.ema.salud_timer.medicamento.presentation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
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
    val tipoMedicamento by viewModel.tipoMedicamento.collectAsState()
    val dosis by viewModel.dosis.collectAsState()
    val tipoFrecuencia by viewModel.tipoFrecuencia.collectAsState()
    val horaInicio by viewModel.horaInicio.collectAsState()
    val horaFija by viewModel.horaFija.collectAsState()
    val intervaloHoras by viewModel.intervaloHoras.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val fechaInicio by viewModel.fechaInicio.collectAsState()
    val fechaFin by viewModel.fechaFin.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val tiposMedicamento = listOf("pastilla", "jarabe")
    val tiposFrecuencia = listOf("hora_fija", "intervalo")
    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    // Formatear fechas
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaInicioFormateada = remember(fechaInicio) { dateFormat.format(fechaInicio) }
    val fechaFinFormateada = remember(fechaFin) { fechaFin?.let { dateFormat.format(it) } ?: "No definida" }

    // Efecto para navegar hacia atrás cuando se guarda con éxito
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onNavigateBack()
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
                // Información básica del medicamento
                Text(
                    "Información del medicamento",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                // Tipo de medicamento (pastilla o jarabe)
                Text("Tipo de medicamento", style = MaterialTheme.typography.bodyLarge)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    tiposMedicamento.forEach { tipo ->
                        FilterChip(
                            selected = tipoMedicamento == tipo,
                            onClick = { viewModel.onTipoMedicamentoChange(tipo) },
                            label = {
                                Text(if (tipo == "pastilla") "Pastilla" else "Jarabe")
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de dosis con el formato adecuado según el tipo
                OutlinedTextField(
                    value = dosis,
                    onValueChange = { viewModel.onDosisChange(it) },
                    label = {
                        Text(
                            if (tipoMedicamento == "pastilla") "Dosis (cantidad de pastillas)"
                            else "Dosis (ml)"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    trailingIcon = {
                        Text(
                            if (tipoMedicamento == "pastilla") "pastillas" else "ml",
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Fechas de inicio y finalización
                Text(
                    "Período de medicación",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = fechaInicioFormateada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de inicio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Usar DatePickerDialog directamente
                            val calendar = Calendar.getInstance()
                            calendar.time = fechaInicio

                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCalendar = Calendar.getInstance()
                                    selectedCalendar.set(year, month, dayOfMonth)
                                    viewModel.onFechaInicioChange(selectedCalendar.time)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "Seleccionar fecha"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = fechaFinFormateada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de finalización (opcional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Usar DatePickerDialog directamente
                            val calendar = Calendar.getInstance()
                            if (fechaFin != null) {
                                calendar.time = fechaFin
                            } else {
                                calendar.add(Calendar.DAY_OF_YEAR, 7)
                            }

                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCalendar = Calendar.getInstance()
                                    selectedCalendar.set(year, month, dayOfMonth)
                                    viewModel.onFechaFinChange(selectedCalendar.time)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "Seleccionar fecha"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Horario y frecuencia
                Text(
                    "Horario y frecuencia",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tipo de frecuencia (hora fija o intervalo)
                Text("¿Cómo debe tomarse?", style = MaterialTheme.typography.bodyLarge)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilterChip(
                        selected = tipoFrecuencia == "hora_fija",
                        onClick = { viewModel.onTipoFrecuenciaChange("hora_fija") },
                        label = { Text("A una hora fija") }
                    )

                    FilterChip(
                        selected = tipoFrecuencia == "intervalo",
                        onClick = { viewModel.onTipoFrecuenciaChange("intervalo") },
                        label = { Text("En intervalos") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campos específicos según el tipo de frecuencia
                when (tipoFrecuencia) {
                    "hora_fija" -> {
                        // Hora fija
                        OutlinedTextField(
                            value = horaFija ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora de toma diaria") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Usar TimePickerDialog directamente
                                    val calendar = Calendar.getInstance()
                                    if (horaFija != null && horaFija!!.matches(Regex("\\d{2}:\\d{2}"))) {
                                        val parts = horaFija!!.split(":")
                                        calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                                        calendar.set(Calendar.MINUTE, parts[1].toInt())
                                    }

                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                                            viewModel.onHoraFijaChange(formattedTime)
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true // Formato 24 horas
                                    ).show()
                                },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = "Seleccionar hora"
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selección de días (opcional)
                        Text(
                            "Días específicos (opcional)",
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
                    }
                    "intervalo" -> {
                        // Intervalo de horas
                        OutlinedTextField(
                            value = intervaloHoras,
                            onValueChange = { viewModel.onIntervaloHorasChange(it) },
                            label = { Text("Cada cuántas horas") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            trailingIcon = {
                                Text(
                                    "horas",
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hora de inicio
                        OutlinedTextField(
                            value = horaInicio ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora de la primera toma") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Usar TimePickerDialog directamente
                                    val calendar = Calendar.getInstance()
                                    if (horaInicio != null && horaInicio!!.matches(Regex("\\d{2}:\\d{2}"))) {
                                        val parts = horaInicio!!.split(":")
                                        calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                                        calendar.set(Calendar.MINUTE, parts[1].toInt())
                                    }

                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                                            viewModel.onHoraInicioChange(formattedTime)
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true // Formato 24 horas
                                    ).show()
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

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}