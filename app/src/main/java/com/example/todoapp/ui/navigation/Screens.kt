package com.example.todoapp.ui.navigation

sealed class Screens(val route: String) {
    object Start: Screens(route = "startScreen")

    object Login: Screens(route = "loginScreen")

    object Notes: Screens(route = "notesScreen")

    object Items: Screens(route = "itemsScreen/{noteId}") {
        fun createRoute(noteId: String) = "itemsScreen/$noteId"
    }

    object NoteAddEdit: Screens(route = "noteAddEditScreen/{noteId}") {
        fun createRoute(noteId: String) = "noteAddEditScreen/$noteId"
    }

}