package com.example.todoapp.ui.screens.general

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.todoapp.R
import com.example.todoapp.data.models.room.Note
import com.example.todoapp.ui.viewmodels.InjectorUtils
import com.example.todoapp.ui.viewmodels.notes.NotesViewModel
import com.example.todoapp.workers.SyncWorker

@Composable
fun CommonAppBar(
    title: String = stringResource(R.string.add_note),
    navController: NavController
) {
    Box(modifier =  Modifier
        .fillMaxWidth(),
    ) {
        Row(modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Return",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable(onClick = {
                        navController.popBackStack()
                    }),
            )
            Text(
                modifier = Modifier
                    .weight(0.5f)
                    .align(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MainButton(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    shape: RoundedCornerShape = RoundedCornerShape(32.dp),
    padding: PaddingValues = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(elevation = 16.dp, shape = shape)
            .border(width = 0.5.dp, shape = shape, color = MaterialTheme.colorScheme.onSurface),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        contentPadding = padding,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = " $text",
                style = textStyle,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun CustomOutlinedTextField(
    new: Boolean,
    startValue: Any?,
    modifier: Modifier,
    textValue: String?,
    textLabel: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    var value by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(startValue) {
        value = startValue
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        OutlinedTextField(
            value =
            if (new) {
                if (value == null) "" else (textValue ?: "")
            } else {
                textValue ?: ""
            },
            onValueChange = {
                value = it
                onValueChange(it)
            },
            label = {
                Text(text = textLabel)
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            textStyle = TextStyle(fontSize = 18.sp),
            shape = MaterialTheme.shapes.medium,
        )
    }
}

@Composable
fun CustomTextField(
    new: Boolean,
    startValue: Any?,
    modifier: Modifier,
    textValue: String?,
    textLabel: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    var value by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(startValue) {
        value = startValue
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        TextField(
            value =
            if (new) {
                if (value == null) "" else (textValue ?: "")
            } else {
                textValue ?: ""
            },
            onValueChange = {
                value = it
                onValueChange(it)
            },
            label = {
                Text(text = textLabel)
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            textStyle = TextStyle(fontSize = 18.sp),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedIndicatorColor = MaterialTheme.colorScheme.background,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                focusedLabelColor = MaterialTheme.colorScheme.background,
                unfocusedLabelColor = MaterialTheme.colorScheme.background,
                cursorColor = MaterialTheme.colorScheme.background
            )
        )
    }
}

@Composable
fun CustomChoiceDialog(
    dialogTitle: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        title = { Text(dialogTitle) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirmClick) {
                Text(confirmText)
            }
        },
        dismissButton = {
            Button(onClick = onDismissClick) {
                Text(dismissText)
            }
        }
    )
}

@Composable
fun CustomEditDialog(
    dialogTitle: String,
    text: String,
    confirmText: String,
    dismissText: String,
    onConfirmClick: (String) -> Unit,
    onDismissClick: () -> Unit,
) {
    var title by remember { mutableStateOf(text) }

    AlertDialog(
        onDismissRequest = onDismissClick,
        title = { Text(dialogTitle) },
        text = {
            TextField(
                value = title,
                onValueChange = { title = it},
                placeholder = { Text(stringResource(R.string.title)) }
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirmClick(title)
            }) {
                Text(confirmText)
            }
        },
        dismissButton = {
            Button(onClick = {
                onDismissClick()
            }) {
                Text(dismissText)
            }
        }
    )
}

@Composable
fun CommonAddFAB(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = { onClick() },
        shape = CircleShape,
    ) {
        Icon(Icons.Filled.Add, "Floating action button.")
    }
}

@Composable
fun noteIdToNote(noteId: String): Note {
    val currentContext = LocalContext.current
    val notesViewModel: NotesViewModel = viewModel(factory = InjectorUtils.provideNotesViewModelFactory(
        context = currentContext))
    return notesViewModel.getNoteById(noteId).collectAsState(Note()).value
}

fun startSyncWorker(context: Context) {
    val workRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java).build()

    WorkManager.getInstance(context.applicationContext)
        .enqueueUniqueWork("UniqueSyncWorker", ExistingWorkPolicy.KEEP, workRequest)
}

