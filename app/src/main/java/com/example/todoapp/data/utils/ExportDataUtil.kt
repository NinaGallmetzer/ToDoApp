package com.example.todoapp.data.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.example.todoapp.R
import java.io.File

class ExportDataUtil(var context: Context, db: String) {
    private var dbName: String = db
    private var dbFile: File = context.getDatabasePath(dbName).absoluteFile
    private var database: SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null)

    companion object;

    private fun generateCsvContent(tableName: String): String {
        val cursor = database.rawQuery("SELECT $tableName.* FROM $tableName", null)
        val stringBuilder = StringBuilder()
        val columnNames = cursor.columnNames
        stringBuilder.append(columnNames.joinToString(",")).append("\n")
        while (cursor.moveToNext()) {
            val rowValues = mutableListOf<String>()
            for (i in columnNames.indices) {
                when (cursor.getType(i)) {
                    Cursor.FIELD_TYPE_INTEGER -> rowValues.add(cursor.getInt(i).toString())
                    Cursor.FIELD_TYPE_FLOAT -> rowValues.add(cursor.getFloat(i).toString())
                    Cursor.FIELD_TYPE_STRING -> rowValues.add("\"${cursor.getString(i)}\"") // Account for "," in String > wrap in double quotes
                    Cursor.FIELD_TYPE_NULL -> rowValues.add("")  // Handle NULL values
                    else -> rowValues.add("")  // Default case
                }
            }
            stringBuilder.append(rowValues.joinToString(",")).append("\n")
        }
        cursor.close()
        return stringBuilder.toString()
    }

    private fun saveCsvToDownloads(tableName: String) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME,  "${tableName}_${Common().getFileTimeStamp()}.csv")
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.IS_PENDING, 1) // Mark file as pending (Android 10+)
        }
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri: Uri? = resolver.insert(collection, contentValues)
        val csvContent = generateCsvContent(tableName)

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(csvContent.toByteArray())
            }
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0) // Mark as complete
            resolver.update(uri, contentValues, null, null)
            Toast.makeText(context, context.getString(R.string.csvSuccessful), Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(context, context.getString(R.string.csvFailed), Toast.LENGTH_SHORT).show()
        }
    }

    fun exportTables() {
        val tables = ArrayList<String>()
        val cursor = database.rawQuery("select name from sqlite_master where type='table' order by name", null)
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()
        tables
            .filter { table ->
                table != "android_metadata" && table != "room_master_table"
            }
            .forEach { tableName ->
                saveCsvToDownloads(tableName)
            }
    }
}