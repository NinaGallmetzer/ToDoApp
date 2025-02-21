package com.example.todoapp.ui.screens.general

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.todoapp.R
import com.example.todoapp.supabase
import com.example.todoapp.ui.navigation.Screens
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val currentContext = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(value = false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.gradient_portrait),
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
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = stringResource(R.string.email)) },
                textStyle = TextStyle(fontSize = 18.sp),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(15.dp)
                    .height(60.dp)
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = MaterialTheme.shapes.large)
                    .border(width = 0.5.dp, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.onSurface),
                )
            TextField(
                value = password,
                onValueChange = { password = it },
                label = {Text(text = stringResource(R.string.password))},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(15.dp)
                    .height(60.dp)
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = MaterialTheme.shapes.large)
                    .border(width = 0.5.dp, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.onSurface),
                shape = MaterialTheme.shapes.large,
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    if (showPassword) {
                        IconButton(onClick = { showPassword = false }) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                tint = MaterialTheme.colorScheme.background,
                                contentDescription = "hide_password"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showPassword = true }) {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                tint = MaterialTheme.colorScheme.background,
                                contentDescription = "show_password"
                            )
                        }
                    }
                }
            )
            MainButton(
                text = stringResource(id = R.string.log_in),
                modifier = Modifier
                    .padding(top = 32.dp)
            ){
                coroutineScope.launch {
                    try {
                        supabase.auth.signInWith(Email) {
                            this.email = email
                            this.password = password
                        }
                        supabase.auth.currentSessionOrNull()
                            ?.let { supabase.auth.sessionManager.saveSession(it) }
                        navController.navigate(Screens.Notes.route)
                        Toast.makeText(currentContext, "Hello, ${email}!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(currentContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
