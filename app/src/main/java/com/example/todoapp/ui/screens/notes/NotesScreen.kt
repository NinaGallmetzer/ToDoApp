package com.example.todoapp.ui.screens.notes

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.todoapp.R
import com.example.todoapp.data.models.room.Note
import com.example.todoapp.data.utils.ExportDataUtil
import com.example.todoapp.supabase
import com.example.todoapp.ui.navigation.Screens
import com.example.todoapp.ui.screens.general.CommonAddFAB
import com.example.todoapp.ui.screens.general.showDialog
import com.example.todoapp.ui.screens.general.startSyncWorker
import com.example.todoapp.ui.viewmodels.InjectorUtils
import com.example.todoapp.ui.viewmodels.notes.NotesViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(
    navController: NavController,
    exportDataUtil: ExportDataUtil
) {
    Box {
        val image = R.drawable.gradient_portrait

        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth(),
            contentScale = ContentScale.FillBounds
        )
        Column {
            NotesAppBar(navController = navController, exportDataUtil = exportDataUtil, title =  stringResource(R.string.notes))
            NotesList(navController = navController)
        }
        CommonAddFAB(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            navController.navigate(Screens.NotesAddEdit.createRoute(noteId = ""))
        }
    }
}

@Composable
fun NotesAppBar(
    navController: NavController,
    exportDataUtil: ExportDataUtil,
    title: String
) {
    val currentContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ){
        var optionsState by remember { mutableStateOf(false) }

        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return", tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.clickable(onClick = {
                navController.popBackStack()
            }),
        )
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Column {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable(onClick = {
                    optionsState = !optionsState
                }),
            )
            DropdownMenu(
                expanded = optionsState,
                onDismissRequest = { optionsState = false },
            ) {
                DropdownMenuItem(
                    leadingIcon = { Icon(imageVector = Icons.Default.Sync, contentDescription = stringResource(R.string.add_note)) },
                    text = { Text("Sync") },
                    onClick = {
                        optionsState = false
                        coroutineScope.launch {
                            startSyncWorker(currentContext)
                            // TODO show result as Toast
                        }
                    },
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(imageVector = Icons.Default.Download, contentDescription = stringResource(R.string.add_note)) },
                    text = { Text("Download") },
                    onClick = {
                        optionsState = false
                        coroutineScope.launch {
                            exportDataUtil.exportTables()
                        }
                    },
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.add_note)) },
                    text = { Text("LogOut") },
                    onClick = {
                        optionsState = false
                        coroutineScope.launch {
                            try {
                                supabase.auth.clearSession()
                                navController.navigate(Screens.Login.route) {
                                    popUpTo(Screens.Login.route) {
                                        inclusive = true
                                    } // Remove all screens up to and including the specified destination from the back stack
                                    launchSingleTop =
                                        true // Ensure the new screen is launched as a single instance, and any other instance of the same screen is removed
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    currentContext,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun NotesList(
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    val currentContext = LocalContext.current

    val notesViewModel: NotesViewModel = viewModel(factory = InjectorUtils.provideNotesViewModelFactory(context = currentContext))
    val notes by notesViewModel.notes.collectAsState()
    var searchText by remember { mutableStateOf("") }

    val title = "${ stringResource(id = R.string.delete) } ${ stringResource(id = R.string.note) }"
    val message = stringResource(id = R.string.delete_this_note_message)
    val confirm = stringResource(id = R.string.delete)
    val cancel = stringResource(id = R.string.cancel)

    val filteredNotes =
        if (searchText.isNotBlank()) {
            notes.filter { note ->
                note.title.contains(searchText, ignoreCase = true) ||
                        note.title.contains(searchText, ignoreCase = true)
            }
        } else {
            notes
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { searchText = "" } // Dismiss the dropdown menu when clicked outside
    ) {
        Column {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text(stringResource(id = R.string.search))},
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(),
                )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                items(filteredNotes) { note ->
                    NoteRow(
                        navController = navController,
                        note = note,
                        onItemClick = {
                            coroutineScope.launch {
                                navController.navigate(Screens.Items.createRoute(noteId = note.noteId))
                            }
                        },
                        onItemLongClick = {
                            coroutineScope.launch {
                                navController.navigate(Screens.NotesAddEdit.createRoute(noteId = note.noteId))
                            }
                        },
                        onDeleteClick = {
                            coroutineScope.launch {
                                showDialog(
                                    context = currentContext,
                                    title = title,
                                    message = message,
                                    positive = confirm,
                                    negative = cancel,
                                    onPositiveClick = {
                                        coroutineScope.launch {
                                            notesViewModel.markDeletedInRoom(note)
                                        }
                                    },
                                    onNegativeClick = {}
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NoteRow(
    navController: NavController,
    note: Note = Note(),
    onItemClick: (Note) -> Unit = {},
    onItemLongClick: (Note) -> Unit = {},
    onDeleteClick: (Note) -> Unit = {},
) {
    var deleteState by remember { mutableStateOf(false) }
    var expandedState by remember { mutableStateOf(false) }

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(5.dp)
        .pointerInput(Unit) { // Detect both tap and long press
            detectTapGestures(
                onTap = { onItemClick(note) },
                onLongPress = { onItemLongClick(note) }
            )
        },
        shape = MaterialTheme.shapes.large,
    ) {
        Column (modifier = Modifier
            .padding(5.dp),
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
            ) {
                Column(modifier = Modifier
                    .padding(10.dp)
                ) {
                    Text(text = note.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface)
                }

                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Column {
                        Icon(
                            tint = MaterialTheme.colorScheme.onSurface,
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete_note),
                            modifier = Modifier
                                .clickable {
                                    deleteState = !deleteState
                                    onDeleteClick(note)
                                }
                        )
                    }
                }

                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Icon(
                        imageVector = if (expandedState) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(id = R.string.details),
                        modifier = Modifier.clickable(onClick = {
                            expandedState = !expandedState
                        })
                    )
                }
            }

            if(expandedState){
                Details(navController, note)
            }
        }
    }
}

@Composable
fun Details(
    navController: NavController,
    note: Note = Note(),
) {
    val noteDescriptionStyle = TextStyle(
        fontSize = MaterialTheme.typography.titleSmall.fontSize,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Normal,
    )
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .clickable {
                coroutineScope.launch {
                    navController.navigate(Screens.NotesAddEdit.createRoute(noteId = note.noteId))
                }
            }
            .padding(10.dp)
    ) {
        Spacer(modifier = Modifier.size(10.dp))
        Text("${stringResource(R.string.comment)}: ${note.content ?: ""}", style = noteDescriptionStyle)
    }
}
