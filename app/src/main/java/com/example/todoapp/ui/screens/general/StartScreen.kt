package com.example.todoapp.ui.screens.general

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.todoapp.R
import com.example.todoapp.supabase
import com.example.todoapp.ui.navigation.Screens
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun StartScreen(
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val image = if (isPortrait) {
        R.drawable.gradient_portrait
    } else {
        R.drawable.background_landscape
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth(),
            contentScale = ContentScale.FillBounds
        )

        Column(modifier = Modifier
            .align(Alignment.Center)
            .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainButton(
                text = "Start",
            ){
                coroutineScope.launch {
                    val userSession = supabase.auth.currentSessionOrNull()
                    if (userSession != null) {
                        navController.navigate(Screens.Notes.route)
                    } else {
                        navController.navigate(Screens.Login.route)
                    }
                }
            }
        }
    }
}

