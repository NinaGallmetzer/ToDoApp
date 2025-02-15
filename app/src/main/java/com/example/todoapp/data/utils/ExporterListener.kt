package com.example.todoapp.data.utils

interface ExporterListener {
    fun success(message: String)

    fun fail(message: String, exception:String)
}