package com.example.todoapp.data.models.supabase

import com.example.todoapp.data.models.room.Note
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseNote(
    val note_id: String, // Matches Supabase's expectations
    val title: String,
    val content: String?,
    val created_at: String,
    val synced_at: String?
) {
    fun toRoomNote(): Note = Note(
        noteId = note_id,
        title = title,
        content = content,
        createdAt = created_at,
        syncedAt = synced_at
    )
}