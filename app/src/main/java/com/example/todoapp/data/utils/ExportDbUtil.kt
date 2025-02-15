package com.example.todoapp.data.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.provider.MediaStore
import com.opencsv.CSVWriter
import java.io.*
import java.util.*

class ExportDbUtil(context: Context, db: String, directoryName: String, private var exporterListener: ExporterListener) {
    var dbName: String
    var directoryName: String
    var context: Context
    lateinit var dbFile: File
    lateinit var database: SQLiteDatabase
    lateinit var tables: ArrayList<String>
    lateinit var exportDir: File

    companion object;

    init {
        this.context = context
        this.dbName = db
        this.directoryName = directoryName
        exportToCsv()
    }

    private fun exportToCsv() {
        dbFile = context.getDatabasePath(dbName).absoluteFile
        database = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        tables = getAllTables()
        exportDir = File(Environment.getExternalStorageDirectory().absolutePath + "/" + directoryName)
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
    }

    private fun getAllTables(): ArrayList<String> {
        val tables = ArrayList<String>()
        val cursor = database!!.rawQuery("select name from sqlite_master where type='table' order by name", null)
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()
        return tables
    }

    fun exportTables() {
        tables
            .filter { table ->
                table != "android_metadata" && table != "room_master_table"
            }
            .forEach { tableName ->
                saveCSVToSharedStorage(tableName)
            }
    }

    private fun saveCSVToSharedStorage(tableName: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${tableName}_${Common().getFileTimeStamp()}.csv")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS) // Save to Documents folder
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        uri?.let {
            try {
                resolver.openOutputStream(it)?.use { outputStream ->
                    val writer = OutputStreamWriter(outputStream)
                    val csvWrite = CSVWriter(writer)
                    createCSVForSingleTable(tableName, csvWrite)
                    csvWrite.close()
                    exporterListener.success("$tableName successfully Exported to shared storage.")
                }
            } catch (e: Exception) {
                exporterListener.fail("Export $tableName fail", e.message.toString())
            }
        } ?: run {
            exporterListener.fail("Failed to create file URI", "Error writing file")
        }
    }

    private fun saveCSVForSingleTable(tableName: String) {
        val file = File(exportDir, "${tableName}_${Common().getFileTimeStamp()}.csv")
        try {
            file.createNewFile()
            val csvWrite = CSVWriter(FileWriter(file))
            createCSVForSingleTable(tableName, csvWrite)
            csvWrite.close()
            exporterListener.success("$tableName successfully Exported")
        } catch (sqlEx: Exception) {
            exporterListener.fail("Export $tableName fail", sqlEx.message.toString())
        }
    }

    private fun createCSVForSingleTable(tableName: String, csvWrite: CSVWriter) {
        val cursor = database!!.rawQuery("SELECT $tableName.* FROM $tableName", null)
        csvWrite.writeNext(cursor!!.columnNames)
        val arrStr = Array(cursor.columnCount) { "it = $it" }

        while (cursor.moveToNext()) {
            //Which column you want to exprort
            for (i in 0..cursor.columnCount) {
                if (cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER) {
                    arrStr[i] = cursor.getInt(i).toString()
                } else if (cursor.getType(i) == Cursor.FIELD_TYPE_STRING) {
                    arrStr[i] = (cursor.getString(i))
                }
            }
            csvWrite.writeNext(arrStr)
        }
        cursor.close()
    }
}