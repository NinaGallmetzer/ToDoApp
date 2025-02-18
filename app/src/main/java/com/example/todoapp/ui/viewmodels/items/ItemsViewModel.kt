package com.example.todoapp.ui.viewmodels.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Item
import com.example.todoapp.data.repositories.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItemsViewModel(private val itemRepository: ItemRepository, private val noteId: String) : ViewModel() {

    private val _items = MutableStateFlow(listOf<Item>())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            itemRepository.getItemsByNoteId(noteId).collect{ items ->
                _items.value = items.filter { it.syncType != SyncType.delete }
            }
        }
    }

    suspend fun addToRoom(item: Item) {
        itemRepository.addToRoom(item)
    }

    fun updateInRoom(item: Item) {
        viewModelScope.launch {
            itemRepository.updateInRoom(item)
        }
    }

    suspend fun markDeletedInRoom(item: Item) {
        itemRepository.markDeletedInRoom(item)
    }

    suspend fun markCheckedDeletedInRoom(noteId: String) {
        itemRepository.markCheckedAsDeleteInRoom(noteId)
    }

}