package com.example.todoapp.ui.screens.notes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.todoapp.R
import com.example.todoapp.ui.navigation.Screens
import com.example.todoapp.ui.screens.general.CommonAppBar
import com.example.todoapp.ui.screens.general.CustomOutlinedTextField
import com.example.todoapp.ui.viewmodels.InjectorUtils
import com.example.todoapp.ui.viewmodels.notes.NoteAddEditViewModel
import kotlinx.coroutines.launch

@Composable
fun NoteAddEditScreen(
    navController: NavController,
    noteId: String
) {
    val noteAddEditViewModel: NoteAddEditViewModel = viewModel(factory = InjectorUtils.provideNoteAddEditViewModelFactory(LocalContext.current, noteId = noteId))
    val coroutineScope = rememberCoroutineScope()
    var saveButtonState by remember { mutableStateOf(false) }

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
            CommonAppBar(
                title = stringResource(if (noteId == "") R.string.add_note else R.string.edit_note),
                navController = navController
            )
            Box (modifier = Modifier
                .padding(10.dp)
                .background(
                    color = MaterialTheme.colorScheme.inversePrimary,
                    shape = MaterialTheme.shapes.large
                ),
            ) {
                Column(modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CustomOutlinedTextField(
                        new = (noteId == ""),
                        startValue = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        textValue = noteAddEditViewModel.note.title,
                        textLabel = stringResource(R.string.title) + " *",
                        keyboardType = KeyboardType.Text,
                        onValueChange = {
                            noteAddEditViewModel.updateViewNote(
                                noteAddEditViewModel.note.copy(
                                    title = it)
                            )
                        }
                    )

                    CustomOutlinedTextField(
                        new = (noteId == ""),
                        startValue = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        textValue = noteAddEditViewModel.note.content,
                        textLabel = stringResource(R.string.comment),
                        keyboardType = KeyboardType.Text,
                        onValueChange = {
                            noteAddEditViewModel.updateViewNote(
                                noteAddEditViewModel.note.copy(
                                    content = it
                                )
                            )
                        }
                    )

                    saveButtonState = noteAddEditViewModel.note.title.isNotBlank()

                    Button(
                        enabled = saveButtonState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        onClick = {
                            coroutineScope.launch {
                                noteAddEditViewModel.saveNote()
                                navController.navigate(Screens.Notes.route)
                            }
                        }) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

}
