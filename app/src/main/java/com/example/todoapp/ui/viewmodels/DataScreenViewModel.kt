package com.example.todoapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.utils.ExportDataUtil
import kotlinx.coroutines.launch

class DataScreenViewModel(
    private val exportDataUtil: ExportDataUtil
    ) : ViewModel(){

    fun downloadTables() {
        viewModelScope.launch {
            exportDataUtil.exportTables()
        }
    }
}