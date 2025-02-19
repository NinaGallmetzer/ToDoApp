package com.example.todoapp.data.repositories

import android.content.Context
import android.util.Log
import com.example.todoapp.data.daos.ItemDao
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Item
import com.example.todoapp.data.models.supabase.SupabaseItem
import com.example.todoapp.data.utils.Common
import com.example.todoapp.supabase
import com.example.todoapp.workers.NetworkChecker
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

class ItemRepository(private val itemDao: ItemDao, context: Context) {

    private val networkChecker = NetworkChecker(context)

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: ItemRepository? = null

        fun getInstance(dao: ItemDao, context: Context) =
            instance ?: synchronized(this) {
                instance ?: ItemRepository(dao, context = context).also { instance = it }
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
        item.updatedAt = Common().getSupabaseTimeStamp()
        item.syncType = SyncType.add
        itemDao.add(item)
    }

    suspend fun updateInRoom(item: Item) {
        if (item.syncType == SyncType.add) {
            itemDao.update(item)
        } else {
            item.syncType = SyncType.update
            itemDao.update(item)
        }
    }

    suspend fun markDeletedInRoom(item: Item) {
        item.updatedAt = Common().getSupabaseTimeStamp()
        item.syncType = SyncType.delete
        itemDao.update(item)
    }
    suspend fun markCheckedAsDeleteInRoom(noteId: String) {
        val checkedItems = getCheckedItemsOfNote(noteId)
        checkedItems.forEach { item ->
            markDeletedInRoom(item)
        }
    }

    private suspend fun fetchItemsSupabase(): List<SupabaseItem> {
        val items = supabase.from("item").select()
            .decodeList<SupabaseItem>()
        return items
    }

    private fun fetchItemsRoom(): List<Item> {
        val items = itemDao.getAllItemsAsList()
        return items
    }

    private suspend fun mergeItems(context: Context, roomItems: List<Item>, supabaseItems: List<SupabaseItem>): List<Item> {
        val lastSync = Common().getLastSyncTime(context)
        Log.d("itemsSync", "lastSync: $lastSync")
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
                    if (updateRoom.isAfter(lastSync)) {
                        if (updateSupabase.isAfter(lastSync)) {
                            if (updateRoom.isAfter(updateSupabase)) {
                                roomItem
                            } else {
                                supabaseItem
                            }
                        } else {
                            roomItem
                        }
                    } else { // if updateRoom isBefore
                        if (updateSupabase.isAfter(lastSync)) {
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
                    if (updateRoom.isAfter(lastSync)) {
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

    suspend fun syncItems(context: Context) {
        if (networkChecker.isConnected()) {
            Log.d("itemsSync","starting Sync")
            val supabaseItems = fetchItemsSupabase()
            Log.d("itemsSync", "supabaseItems: $supabaseItems")
            val roomItems = fetchItemsRoom()
            Log.d("itemsSync", "roomItems: $roomItems")
            val itemsToSync = mergeItems(context, roomItems, supabaseItems)
            Log.d("itemsSync", "mergedItems: $itemsToSync")
            val syncTime = Common().getSupabaseTimeStamp()
            updateBothDatabases(itemsToSync, syncTime)
            Common().saveLastSyncTime(context)
        }
    }

    private suspend fun updateBothDatabases(itemsToSync: List<Item>, syncTime: String) {
        itemsToSync.forEach { itemToSync ->
            when (itemToSync.syncType) {
                SyncType.add -> addToSupabaseAndUpdateInRoom(itemToSync, syncTime)
                SyncType.update -> updateInSupabaseAndRoom(itemToSync, syncTime)
                SyncType.delete -> deleteFromSupabaseAndRoom(itemToSync)
                SyncType.synced -> {}
                SyncType.newFromSupabase -> addFromSupabaseToRoom(itemToSync, syncTime)
            }
        }
    }

    private suspend fun addToSupabaseAndUpdateInRoom(item: Item, syncTime: String) {
        item.syncedAt = syncTime
        item.syncType = SyncType.synced
        itemDao.update(item)
        supabase.from("item").insert(item.toSupabaseItem())
    }

    private suspend fun updateInSupabaseAndRoom(item: Item, syncTime: String) {
        item.syncedAt = syncTime
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

    private suspend fun addFromSupabaseToRoom(item: Item, syncTime: String) {
        item.syncedAt = syncTime
        item.syncType = SyncType.synced
        itemDao.add(item)

    }
}