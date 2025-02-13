package com.example.todoapp.models.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.todoapp.models.enums.SyncType
import com.example.todoapp.models.supabase.SupabaseNote
import com.example.todoapp.utils.Common
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    indices = [Index(value = ["title"])]
)
data class Note (
    @PrimaryKey(autoGenerate = false)
    val noteId: String = UUID.randomUUID().toString(),
    var title: String = "",
    var content: String? = null,
    var createdAt: String = Common().getTimeStamp(),
    var updatedAt: String = Common().getTimeStamp(),
    var syncedAt: String? = null,
    var syncType: SyncType = SyncType.synced,
) {
    fun toSupabaseNote(): SupabaseNote = SupabaseNote(
        note_id = noteId,
        title = title,
        content = content,
        created_at = createdAt,
        updated_at = updatedAt,
        synced_at = syncedAt
    )
}