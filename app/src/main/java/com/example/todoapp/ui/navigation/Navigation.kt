package com.example.todoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todoapp.data.utils.ExportDbUtil
import com.example.todoapp.ui.screens.general.LoginScreen
import com.example.todoapp.ui.screens.general.StartScreen
import com.example.todoapp.ui.screens.general.DataScreen
import com.example.todoapp.ui.screens.notes.NotesAddEditScreen
import com.example.todoapp.ui.screens.notes.NotesScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    exportDbUtil: ExportDbUtil
) {
    NavHost(navController = navController, startDestination = Screens.Start.route) {
        composable(route = Screens.Start.route) {
            StartScreen(navController)
        }

        composable(route = Screens.Login.route) {
            LoginScreen(navController)
        }

        composable(route = Screens.Notes.route) {
            NotesScreen(navController = navController)
        }
        composable(route = Screens.NotesAddEdit.route) { backStackEntry ->
            val noteId = requireNotNull(backStackEntry.arguments?.getString("noteId"))
            NotesAddEditScreen(navController = navController, noteId = noteId)
        }

        composable(route = Screens.Data.route) {
            DataScreen(navController = navController, exportDbUtil)
        }

    }
}