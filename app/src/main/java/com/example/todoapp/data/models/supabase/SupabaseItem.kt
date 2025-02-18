package com.example.todoapp.data.models.supabase

import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Item
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseItem(
    val item_id: String,
    val note_id: String,
    val title: String,
    val checked: Boolean,
    val created_at: String,
    val synced_at: String?
) {
    fun toRoomItem(syncType: SyncType): Item = Item(
        itemId = item_id,
        noteId = note_id,
        title = title,
        checked = checked,
        createdAt = created_at,
        syncedAt = synced_at,
        syncType = syncType
    )
}