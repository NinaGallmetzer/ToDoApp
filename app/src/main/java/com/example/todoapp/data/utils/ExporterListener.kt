package com.example.todoapp.data.utils

interface ExporterListener {
    fun success(s: String)

    fun fail(message: String,exception:String)
}