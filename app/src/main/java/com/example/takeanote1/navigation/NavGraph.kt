package com.example.takeanote1.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.takeanote1.NotesApplication
import com.example.takeanote1.ui.auth.LoginScreen
import com.example.takeanote1.ui.auth.AuthViewModel
import com.example.takeanote1.ui.home.HomeScreen
import com.example.takeanote1.ui.home.NotesViewModel
import com.example.takeanote1.ui.addnote.AddNoteScreen
import com.example.takeanote1.ui.completed.CompletedNotesScreen
import com.example.takeanote1.data.GoogleSignInManager

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as NotesApplication
    val activity = context as? Activity

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(app.repository, app.userPreferences)
    )

    val notesViewModel: NotesViewModel = viewModel(
        factory = NotesViewModel.Factory(app.repository, app.userPreferences)
    )

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // ---------------- LOGIN SCREEN ----------------
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

        // ---------------- HOME SCREEN ----------------
        composable("home") {
            HomeScreen(
                viewModel = notesViewModel,
                authViewModel = authViewModel,  // pass auth VM for logout & switch account
                onAddNoteClick = { navController.navigate("add_note") },
                onHistoryClick = { navController.navigate("completed") },
                onLoginNavigate = {             // callback after logout or account switch
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // ---------------- ADD NOTE SCREEN ----------------
        composable("add_note") {
            AddNoteScreen(
                viewModel = notesViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- COMPLETED NOTES SCREEN ----------------
        composable("completed") {
            CompletedNotesScreen(
                viewModel = notesViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
