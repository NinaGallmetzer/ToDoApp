package com.example.todoapp.ui.viewmodels.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.models.room.Item
import com.example.todoapp.data.repositories.ItemRepository
import kotlinx.coroutines.launch

class ItemsAddEditViewModel(
    private val itemRepository: ItemRepository,
    private val noteId: String = "",
    private val itemId: String = ""
): ViewModel() {

    var item by mutableStateOf(Item())
        private set

    init {
        viewModelScope.launch {
            if (itemId == "") {
                item = Item(noteId = noteId)
            } else {
                itemRepository.getItemById(itemId).collect { item ->
                    this@ItemsAddEditViewModel.item = item
                }
            }
        }
    }

    fun updateViewItem(item: Item) {
        this.item = item
    }

    suspend fun saveItem(){
        if(itemId == "") {
            itemRepository.addToRoom(item)
        } else {
            itemRepository.updateInRoom(item)
        }
    }

    suspend fun updateInRoom(item: Item) {
        itemRepository.updateInRoom(item)
    }

    fun markDeletedInRoom(item: Item) {
        viewModelScope.launch {
            itemRepository.markDeletedInRoom(item)
        }
    }


}