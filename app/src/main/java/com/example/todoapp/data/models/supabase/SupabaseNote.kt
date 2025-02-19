package com.example.todoapp.data.models.supabase

import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Note
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseNote(
    val note_id: String,
    val title: String,
    val content: String?,
    val created_at: String,
    var updated_at: String,
    val synced_at: String?
) {
    fun toRoomNote(syncType: SyncType): Note = Note(
        noteId = note_id,
        title = title,
        content = content,
        createdAt = created_at,
        updatedAt = updated_at,
        syncedAt = synced_at,
        syncType = syncType
    )
}