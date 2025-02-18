package com.example.todoapp.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todoapp.data.daos.ItemDao
import com.example.todoapp.data.daos.NoteDao
import com.example.todoapp.data.models.room.Note
import com.example.todoapp.data.models.room.Item
import com.example.todoapp.workers.SeedDatabaseWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Note::class,
        Item::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ToDoDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun itemDao(): ItemDao

    companion object{
        @Volatile
        private var Instance: ToDoDatabase? = null

        fun getDatabase(context: Context): ToDoDatabase {
            return Instance ?: synchronized(this) {
                val database = Room.databaseBuilder(context, ToDoDatabase::class.java, "todo_db")
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val seedDatabaseWorker = SeedDatabaseWorker()
                                seedDatabaseWorker.seedDatabase(getDatabase(context))
                            }
                        }
                    })
                    .build()
                Instance = database
                database
            }
        }
    }
}
