package com.example.todoapp.ui.navigation

sealed class Screens(val route: String) {
    object Start: Screens(route = "startScreen")

    object Login: Screens(route = "loginScreen")

    object Notes: Screens(route = "notesScreen")

    object NotesAddEdit: Screens(route = "notesAddEditScreen/{noteId}") {
        fun createRoute(noteId: String) = "notesAddEditScreen/$noteId"
    }

    object Data: Screens(route = "dataScreen")

}