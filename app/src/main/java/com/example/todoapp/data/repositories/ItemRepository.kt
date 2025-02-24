package com.example.todoapp.data.repositories

import com.example.todoapp.data.daos.ItemDao
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Item
import com.example.todoapp.data.models.supabase.SupabaseItem
import com.example.todoapp.data.utils.TimeStampUtil
import com.example.todoapp.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.OffsetDateTime

class ItemRepository(private val itemDao: ItemDao) {

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: ItemRepository? = null

        fun getInstance(dao: ItemDao) =
            instance ?: synchronized(this) {
                instance ?: ItemRepository(dao).also { instance = it }
            }
    }

    fun getItemsByNoteId(noteId: String): Flow<List<Item>> {
        return itemDao.getItemsByNoteId(noteId)
    }
    fun getItemById(itemId: String): Flow<Item> {
        return itemDao.getItemById(itemId)
    }
    private suspend fun getCheckedItemsOfNote(noteId: String): List<Item> {
        return itemDao.getCheckedItemsOfNote(noteId)
    }

    suspend fun addToRoom(item: Item) {
        item.updatedAt = TimeStampUtil().getSupabaseTimeStamp()
        item.syncType = SyncType.add
        itemDao.add(item)
    }

    suspend fun updateInRoom(item: Item) {
        item.updatedAt = TimeStampUtil().getSupabaseTimeStamp()
        if (item.syncType == SyncType.add) {
            itemDao.update(item)
        } else {
            item.syncType = SyncType.update
            itemDao.update(item)
        }
    }

    suspend fun markDeletedInRoom(item: Item) {
        item.updatedAt = TimeStampUtil().getSupabaseTimeStamp()
        item.syncType = SyncType.delete
        itemDao.update(item)
    }
    suspend fun markCheckedAsDeleteInRoom(noteId: String) {
        val checkedItems = getCheckedItemsOfNote(noteId)
        checkedItems.forEach { item ->
            markDeletedInRoom(item)
        }
    }

    suspend fun fetchSupabaseItems(): List<SupabaseItem> {
        val items = supabase.from("item").select()
            .decodeList<SupabaseItem>()
        return items
    }

    suspend fun fetchRoomItems(): List<Item> {
        val items = itemDao.getAllItemsAsList()
        return items
    }

    suspend fun mergeItems(
        roomItems: List<Item>,
        supabaseItems: List<SupabaseItem>,
        lastSyncTime: Instant
    ): List<Item> {
        val roomItemsMap = roomItems
            .associateBy { it.itemId }
        val supabaseItemsMap = supabaseItems
            .map { it.toRoomItem(SyncType.newFromSupabase) }
            .associateBy { it.itemId }

        val mergedItems = mutableListOf<Item>()

        // Merge logic
        val allItemIds = roomItemsMap.keys + supabaseItemsMap.keys

        for (itemId in allItemIds) {
            val roomItem = roomItemsMap[itemId]
            val supabaseItem = supabaseItemsMap[itemId]

            val mergedItem = when {
                roomItem != null && supabaseItem != null -> {
                    val updateRoom = OffsetDateTime.parse(roomItem.updatedAt).toInstant()
                    val updateSupabase = OffsetDateTime.parse(supabaseItem.updatedAt).toInstant()
                    if (updateRoom.isAfter(lastSyncTime)) {
                        if (updateSupabase.isAfter(lastSyncTime)) {
                            if (updateRoom.isAfter(updateSupabase)) {
                                roomItem
                            } else {
                                supabaseItem
                            }
                        } else {
                            roomItem
                        }
                    } else { // if updateRoom isBefore
                        if (updateSupabase.isAfter(lastSyncTime)) {
                            supabaseItem
                        } else {
                            if (updateSupabase.isAfter(updateSupabase)) {
                                roomItem
                            } else {
                                supabaseItem
                            }
                        }
                    }
                }
                roomItem != null -> {
                    val updateRoom = OffsetDateTime.parse(roomItem.updatedAt).toInstant()
                    if (updateRoom.isAfter(lastSyncTime)) {
                        if (roomItem.syncType == SyncType.add) {
                            roomItem
                        } else if (roomItem.syncType == SyncType.update) {
                            roomItem.syncType = SyncType.add
                            roomItem
                        } else if (roomItem.syncType == SyncType.delete) {
                            itemDao.delete(roomItem)
                            null
                        } else {
                            null
                        }
                    } else {
                        itemDao.delete(roomItem)
                        null
                    }

                }
                supabaseItem != null -> supabaseItem
                else -> null
            }

            mergedItem?.let { mergedItems.add(it) }
        }
        return mergedItems
    }

    suspend fun updateBothDatabases(itemsToSync: List<Item>, newSyncTime: String) {
        itemsToSync.forEach { itemToSync ->
            when (itemToSync.syncType) {
                SyncType.add -> addToSupabaseAndUpdateInRoom(itemToSync, newSyncTime)
                SyncType.update -> updateInSupabaseAndRoom(itemToSync, newSyncTime)
                SyncType.delete -> deleteFromSupabaseAndRoom(itemToSync)
                SyncType.synced -> {}
                SyncType.newFromSupabase -> addFromSupabaseToRoom(itemToSync, newSyncTime)
            }
        }
    }

    private suspend fun addToSupabaseAndUpdateInRoom(item: Item, newSyncTime: String) {
        item.syncedAt = newSyncTime
        item.syncType = SyncType.synced
        supabase.from("item").insert(item.toSupabaseItem())
        itemDao.update(item)
    }

    private suspend fun updateInSupabaseAndRoom(item: Item, newSyncTime: String) {
        item.syncedAt = newSyncTime
        item.syncType = SyncType.synced
        supabase.from("item").update(
            {
                set("title", item.title)
                set("checked", item.checked)
                set("created_at", item.createdAt)
                set("updated_at", item.updatedAt)
                set("synced_at", item.syncedAt)
            }
        ) {
            filter {
                eq("item_id", item.itemId)
            }
        }
        itemDao.update(item)
    }

    private suspend fun deleteFromSupabaseAndRoom(item: Item) {
        supabase.from("item").delete {
            filter {
                eq("item_id", item.itemId)
            }
        }
        itemDao.delete(item)
    }

    private suspend fun addFromSupabaseToRoom(item: Item, newSyncTime: String) {
        item.syncedAt = newSyncTime
        item.syncType = SyncType.synced
        itemDao.add(item)
    }
}