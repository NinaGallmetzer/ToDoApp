package com.example.todoapp.ui.viewmodels.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.data.repositories.ItemRepository

@Suppress("UNCHECKED_CAST")
class ItemsViewModelFactory(private val itemRepository: ItemRepository, private val noteId: String): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemsViewModel::class.java)) {
            return ItemsViewModel(itemRepository = itemRepository, noteId = noteId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}