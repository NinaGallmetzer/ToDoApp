package com.example.todoapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.utils.ExportDbUtil
import kotlinx.coroutines.launch

class DataScreenViewModel(
    private val exportDbUtil: ExportDbUtil
    ) : ViewModel(){

    fun downloadTables() {
        viewModelScope.launch {
            exportDbUtil.exportTables()
        }
    }
}