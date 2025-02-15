package com.example.todoapp.data.utils

interface ExportListener {
    fun success(message: String)

    fun fail(message: String, exception:String)
}