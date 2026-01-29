package com.example.takeanote1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.takeanote1.NotesApplication
import com.example.takeanote1.ui.auth.AuthViewModel
import com.example.takeanote1.ui.auth.LoginScreen
import com.example.takeanote1.ui.home.HomeScreen
import com.example.takeanote1.ui.home.NotesViewModel
import com.example.takeanote1.ui.addnote.AddNoteScreen
import com.example.takeanote1.ui.completed.CompletedNotesScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as NotesApplication

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(app.repository, app.userPreferences)
    )

    val notesViewModel: NotesViewModel = viewModel(
        factory = NotesViewModel.Factory(
            app.repository,
            app.userPreferences,
            app.notificationRepository
        )
    )

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = notesViewModel,
                onAddNoteClick = { navController.navigate("add_note") },
                onHistoryClick = { navController.navigate("completed") },
                onEditNoteClick = { noteId -> navController.navigate("add_note/$noteId") }
            )
        }

        // Add / Edit Note
        composable(
            route = "add_note/{noteId}",
            arguments = listOf(navArgument("noteId") {
                type = NavType.StringType
                defaultValue = "" // empty = new note
            })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.takeIf { it.isNotEmpty() }
            AddNoteScreen(
                viewModel = notesViewModel,
                noteId = noteId,
                onBack = { navController.popBackStack() }
            )
        }

        // Add Note (without id)
        composable("add_note") {
            AddNoteScreen(
                viewModel = notesViewModel,
                noteId = null,
                onBack = { navController.popBackStack() }
            )
        }

        composable("completed") {
            CompletedNotesScreen(
                viewModel = notesViewModel,
                onBack = { navController.popBackStack() },
                onEditNoteClick = { noteId -> navController.navigate("add_note/$noteId") }
            )
        }
    }
}
