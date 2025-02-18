package com.example.todoapp.data.repositories

import android.content.Context
import com.example.todoapp.data.daos.ItemDao
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Item
import com.example.todoapp.data.models.supabase.SupabaseItem
import com.example.todoapp.data.utils.Common
import com.example.todoapp.supabase
import com.example.todoapp.workers.NetworkChecker
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow

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

    suspend fun addToRoom(item: Item) {
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
        item.syncType = SyncType.delete
        itemDao.update(item)
    }

    private suspend fun fetchUnsyncedItemsSupabase(context: Context): List<SupabaseItem> {
        val lastFetch = Common().getLastFetchTime(context)
        val items = supabase.from("item")
            .select {
                filter {
                    or {
                        gt("synced_at", lastFetch)
                        exact("synced_at", null)
                    }
                }
            }
            .decodeList<SupabaseItem>()
        Common().saveLastFetchTime(context)
        return items
    }

    private suspend fun fetchUnsyncedItemsRoom(): List<Item> {
        return itemDao.getUnsyncedItems()
    }

    private fun mergeItems(roomItems: List<Item>, supabaseItems: List<SupabaseItem>): List<Item> {
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
                    // Conflict resolution: Prioritize roomItem if it has syncType update or delete
                    if (roomItem.syncType == SyncType.update || roomItem.syncType == SyncType.delete) {
                        roomItem
                    } else {
                        supabaseItem
                    }
                }
                roomItem != null -> roomItem
                supabaseItem != null -> supabaseItem
                else -> null
            }

            mergedItem?.let { mergedItems.add(it) }
        }
        return mergedItems
    }

    suspend fun syncItems(context: Context) {
        if (networkChecker.isConnected()) {
            val supabaseItems = fetchUnsyncedItemsSupabase(context)
            val roomItems = fetchUnsyncedItemsRoom()
            val itemsToSync = mergeItems(roomItems, supabaseItems)
            val syncTime = Common().getSupabaseTimeStamp()
            updateBothDatabases(itemsToSync, syncTime)
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

    private suspend fun addFromSupabaseToRoom(item: Item, syncTime: String) {
        item.syncedAt = syncTime
        item.syncType = SyncType.synced
        itemDao.add(item)

    }

    private suspend fun addToSupabaseAndUpdateInRoom(item: Item, syncTime: String) {
        item.syncedAt = syncTime
        item.syncType = SyncType.synced
        supabase.from("item").insert(item.toSupabaseItem())
        itemDao.update(item)
    }

    private suspend fun updateInSupabaseAndRoom(item: Item, syncTime: String) {
        item.syncedAt = syncTime
        item.syncType = SyncType.synced
        supabase.from("item").update(
            {
                set("title", item.title)
                set("checked", item.checked)
                set("created_at", item.createdAt)
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
}