package com.example.todoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todoapp.data.utils.ExportDataUtil
import com.example.todoapp.ui.screens.general.LoginScreen
import com.example.todoapp.ui.screens.general.StartScreen
import com.example.todoapp.ui.screens.items.ItemsScreen
import com.example.todoapp.ui.screens.notes.NotesAddEditScreen
import com.example.todoapp.ui.screens.notes.NotesScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    exportDataUtil: ExportDataUtil
) {
    NavHost(navController = navController, startDestination = Screens.Start.route) {
        composable(route = Screens.Start.route) {
            StartScreen(navController)
        }

        composable(route = Screens.Login.route) {
            LoginScreen(navController)
        }

        composable(route = Screens.Notes.route) {
            NotesScreen(navController = navController, exportDataUtil)
        }
        composable(route = Screens.NotesAddEdit.route) { backStackEntry ->
            val noteId = requireNotNull(backStackEntry.arguments?.getString("noteId"))
            NotesAddEditScreen(navController = navController, noteId = noteId)
        }

        composable(route = Screens.Items.route) { backStackEntry ->
            val noteId = requireNotNull(backStackEntry.arguments?.getString("noteId"))
            ItemsScreen(navController = navController, noteId = noteId)
        }

    }
}