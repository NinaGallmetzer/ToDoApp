package com.example.todoapp.data.models.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.supabase.SupabaseItem
import com.example.todoapp.data.utils.Common
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    indices = [Index(value = ["noteId"])],
    foreignKeys = [
        ForeignKey(entity = Note::class, parentColumns = ["noteId"], childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)]
)
data class Item (
    @PrimaryKey(autoGenerate = false)
    val itemId: String = UUID.randomUUID().toString(),
    val noteId: String = "",
    var title: String = "",
    var checked: Boolean = false,
    var createdAt: String = Common().getSupabaseTimeStamp(),
    var syncedAt: String? = null,
    var syncType: SyncType = SyncType.add,
) {
    fun toSupabaseItem(): SupabaseItem = SupabaseItem(
        item_id = itemId,
        note_id = noteId,
        title = title,
        checked = checked,
        created_at = createdAt,
        synced_at = syncedAt
    )
}