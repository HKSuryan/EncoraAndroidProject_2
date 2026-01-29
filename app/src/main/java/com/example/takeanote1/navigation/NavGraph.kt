package com.example.takeanote1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.takeanote1.NotesApplication
import com.example.takeanote1.ui.auth.LoginScreen
import com.example.takeanote1.ui.home.HomeScreen
import com.example.takeanote1.ui.auth.AuthViewModel
import com.example.takeanote1.ui.home.NotesViewModel
import com.example.takeanote1.ui.addnote.AddNoteScreen
import com.example.takeanote1.ui.completed.CompletedNotesScreen
import com.example.takeanote1.ui.reminder.ReminderViewModel
import com.example.takeanote1.ui.reminder.RemindersListScreen
import com.example.takeanote1.ui.reminder.AddReminderScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as NotesApplication
    
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(app.repository, app.userPreferences)
    )
    
    val notesViewModel: NotesViewModel = viewModel(
        factory = NotesViewModel.Factory(app.repository, app.userPreferences)
    )

    val reminderViewModel: ReminderViewModel = viewModel(
        factory = ReminderViewModel.Factory(app.repository, app.userPreferences)
    )

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { 
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                viewModel = notesViewModel,
                onAddNoteClick = { navController.navigate("add_note") },
                onHistoryClick = { navController.navigate("completed") },
                onRemindersClick = { navController.navigate("reminders")}
            )
        }
        composable("add_note") {
            AddNoteScreen(
                viewModel = notesViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("completed") {
            CompletedNotesScreen(
                viewModel = notesViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("reminders") {
            RemindersListScreen(
                viewModel = reminderViewModel,
                onAddReminderClick = { navController.navigate("add_reminder") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("add_reminder") {
            AddReminderScreen(
                viewModel = reminderViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Add reminder with note ID (optional route for creating reminder from a note)
        composable(
            route = "add_reminder/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId")
            AddReminderScreen(
                viewModel = reminderViewModel,
                noteId = noteId,
                onBack = { navController.popBackStack() }
            )
        }

    }
}