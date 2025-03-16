package com.ema.salud_timer.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ema.salud_timer.home.presentation.HomeScreen
import com.ema.salud_timer.medicamento.presentation.AddEditMedicamentoScreen
import com.ema.salud_timer.medicamento.presentation.PersonaMedicamentosScreen
import com.ema.salud_timer.persona.presentation.AddEditPersonaScreen
import com.ema.salud_timer.persona.presentation.PersonaListScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.HOME) {
        // Home/Dashboard
        composable(route = Screens.HOME) {
            HomeScreen(
                onNavigateToPersonas = {
                    // Este botón va a la lista de personas para ver medicamentos
                    navController.navigate(Screens.PERSONA_LIST)
                },
                onNavigateToAdminPersonas = {
                    // Este botón va a la administración de perfiles
                    navController.navigate(Screens.ADMIN_PERSONA_LIST)
                },
                onNavigateToAddPersona = {
                    navController.navigate(Screens.ADD_PERSONA)
                },
                onPersonaClick = { personaId ->
                    // Al hacer clic en una tarjeta de persona, ver sus medicamentos
                    navController.navigate("${Screens.PERSONA_MEDICAMENTOS}/$personaId")
                }
            )
        }

        // ===== FLUJO DE ADMINISTRACIÓN DE PERFILES =====

        // Pantalla de lista de perfiles para administrar
        composable(route = Screens.ADMIN_PERSONA_LIST) {
            PersonaListScreen(
                onPersonaClick = { personaId ->
                    // Al hacer clic en la lista de administración, vamos a editar persona
                    navController.navigate("${Screens.EDIT_PERSONA}/$personaId")
                },
                onAddPersonaClick = {
                    navController.navigate(Screens.ADD_PERSONA)
                },
                onNavigateBack = {
                    navController.navigateUp()
                },
                screenTitle = "Administrar Perfiles"
            )
        }

        // Agregar persona
        composable(route = Screens.ADD_PERSONA) {
            AddEditPersonaScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Editar persona
        composable(
            route = "${Screens.EDIT_PERSONA}/{personaId}",
            arguments = listOf(navArgument("personaId") { type = NavType.IntType })
        ) { backStackEntry ->
            AddEditPersonaScreen(
                personaId = backStackEntry.arguments?.getInt("personaId"),
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // ===== FLUJO DE GESTIÓN DE MEDICAMENTOS =====

        // Lista de personas para ver/asignar medicamentos
        composable(route = Screens.PERSONA_LIST) {
            PersonaListScreen(
                onPersonaClick = { personaId ->
                    // Al hacer clic en esta lista, vamos a los medicamentos de esa persona
                    navController.navigate("${Screens.PERSONA_MEDICAMENTOS}/$personaId")
                },
                onAddPersonaClick = {
                    navController.navigate(Screens.ADD_PERSONA)
                },
                onNavigateBack = {
                    navController.navigateUp()
                },
                screenTitle = "Seleccionar Perfil"
            )
        }

        // Pantalla de medicamentos de una persona
        composable(
            route = "${Screens.PERSONA_MEDICAMENTOS}/{personaId}",
            arguments = listOf(navArgument("personaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val personaId = backStackEntry.arguments?.getInt("personaId") ?: 0
            PersonaMedicamentosScreen(
                personaId = personaId,
                onAddMedicamento = {
                    // Navegar a pantalla para agregar medicamento
                    navController.navigate("${Screens.ADD_MEDICAMENTO}/$personaId")
                },
                onEditMedicamento = { medicamentoId ->
                    // Navegar para editar medicamento
                    navController.navigate("${Screens.EDIT_MEDICAMENTO}/$medicamentoId")
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Agregar medicamento
        composable(
            route = "${Screens.ADD_MEDICAMENTO}/{personaId}",
            arguments = listOf(navArgument("personaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val personaId = backStackEntry.arguments?.getInt("personaId") ?: 0
            AddEditMedicamentoScreen(
                personaId = personaId,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Editar medicamento
        // En NavigationWrapper.kt
        composable(
            route = "${Screens.EDIT_MEDICAMENTO}/{medicamentoId}/{personaId}",
            arguments = listOf(
                navArgument("medicamentoId") { type = NavType.IntType },
                navArgument("personaId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val medicamentoId = backStackEntry.arguments?.getInt("medicamentoId") ?: 0
            val personaId = backStackEntry.arguments?.getInt("personaId") ?: 0
            AddEditMedicamentoScreen(
                medicamentoId = medicamentoId,
                personaId = personaId,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}