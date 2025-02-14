package com.example.todoapp.data.utils

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
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
            .forEach { table ->
                exportSingleTable(table)
            }
    }

    private fun exportSingleTable(tableName: String) {
        val file = File(exportDir, "${tableName}_${Common().getFileTimeStamp()}.csv")
        try {
            file.createNewFile()
            val csvWrite = CSVWriter(FileWriter(file))
            exportTable(tableName, csvWrite)
            csvWrite.close()
            exporterListener.success("$tableName successfully Exported")
        } catch (sqlEx: Exception) {
            exporterListener.fail("Export $tableName fail", sqlEx.message.toString())
        }
    }

    private fun exportTable(tableName: String, csvWrite: CSVWriter) {
        var curCSV: Cursor? = null
        curCSV = database!!.rawQuery("SELECT $tableName.* FROM $tableName", null)
        csvWrite.writeNext(curCSV!!.getColumnNames())
        val arrStr = Array(curCSV.columnCount) { "it = $it" }

        while (curCSV.moveToNext()) {
            //Which column you want to exprort
            for (i in 0..curCSV.columnCount) {
                if (curCSV.getType(i) == Cursor.FIELD_TYPE_INTEGER) {
                    arrStr[i] = curCSV.getInt(i).toString()
                } else if (curCSV.getType(i) == Cursor.FIELD_TYPE_STRING) {
                    arrStr[i] = (curCSV.getString(i))
                }
            }
            csvWrite.writeNext(arrStr)
        }
        curCSV.close()
    }
}