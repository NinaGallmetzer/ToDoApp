package com.example.todoapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.data.utils.ExportDbUtil
import com.example.todoapp.data.utils.ExporterListener
import com.example.todoapp.ui.navigation.SetupNavGraph
import com.example.todoapp.ui.theme.ToDoAppTheme
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

val supabase = createSupabaseClient(
    supabaseUrl = "https://qrydvphzggjxzvqnnsaf.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFyeWR2cGh6Z2dqeHp2cW5uc2FmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkzNTQyMzgsImV4cCI6MjA1NDkzMDIzOH0._mxqbRgxP_mpWZiuJ7ByMsOLZmC6YNe8MENlZdC8umY"
) {
    install(Auth) {
        flowType = FlowType.PKCE
        scheme = "app"
        host = "supabase.com"
    }
    install(Postgrest)
    install(Realtime)
}

class MainActivity : ComponentActivity(), ExporterListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val exportDbUtil = ExportDbUtil(
            this,
            "todo_db",
            "/Download/",
            this)

        var navController: NavHostController
        setContent {
            ToDoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    supabase.auth.currentSessionOrNull()
                    navController = rememberNavController()
                    SetupNavGraph(navController, exportDbUtil)
                }
            }
        }
    }

    override fun fail(message: String, exception: String) {
        println("Export failed. Message: $message, Exception: $exception")
        mToast(this, getString(R.string.csvFailed))
    }

    override fun success(s: String) {
        println("DB Successfully exported as csv")
        mToast(this, getString(R.string.csvSuccessful))
    }

    private fun mToast(context: Context, txt : String){
        Toast.makeText(context, txt , Toast.LENGTH_SHORT).show()
    }

}
