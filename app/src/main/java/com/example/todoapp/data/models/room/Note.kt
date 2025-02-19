package com.example.todoapp.data.models.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.supabase.SupabaseNote
import com.example.todoapp.data.utils.TimeStampUtil
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
    var createdAt: String = TimeStampUtil().getSupabaseTimeStamp(),
    var updatedAt: String = TimeStampUtil().getSupabaseTimeStamp(),
    var syncedAt: String? = null,
    var syncType: SyncType = SyncType.add,
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