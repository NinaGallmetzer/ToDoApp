package com.example.todoapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.data.utils.ExportDataUtil

@Suppress("UNCHECKED_CAST")
class DataScreenViewModelFactory(private val exportDataUtil: ExportDataUtil): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DataScreenViewModel::class.java)) {
            return DataScreenViewModel(exportDataUtil) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}