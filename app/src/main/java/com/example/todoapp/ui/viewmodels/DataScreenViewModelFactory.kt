package com.example.todoapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.data.utils.ExportDbUtil

@Suppress("UNCHECKED_CAST")
class DataScreenViewModelFactory(private val exportDbUtil: ExportDbUtil): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DataScreenViewModel::class.java)) {
            return DataScreenViewModel(exportDbUtil) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}