package com.example.todoapp.ui.screens.items

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.todoapp.R
import com.example.todoapp.data.models.room.Item
import com.example.todoapp.supabase
import com.example.todoapp.ui.navigation.Screens
import com.example.todoapp.ui.screens.general.CommonAddFAB
import com.example.todoapp.ui.screens.general.CustomChoiceDialog
import com.example.todoapp.ui.screens.general.CustomEditDialog
import com.example.todoapp.ui.screens.general.noteIdToNote
import com.example.todoapp.ui.screens.general.startSyncWorker
import com.example.todoapp.ui.viewmodels.InjectorUtils
import com.example.todoapp.ui.viewmodels.items.ItemsViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun ItemsScreen(
    navController: NavController,
    noteId: String
) {
    Box {
        val image = R.drawable.gradient_portrait
        val itemsViewModel: ItemsViewModel = viewModel(factory = InjectorUtils.provideItemsViewModelFactory(LocalContext.current, noteId = noteId))
        var showAddItemDialog by remember { mutableStateOf(false) }

        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth(),
            contentScale = ContentScale.FillBounds
        )
        Column {
            ItemsAppBar(navController = navController, noteId = noteId)
            ItemsList(noteId = noteId)
        }
        CommonAddFAB(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        ) {
            showAddItemDialog = true
        }
        if (showAddItemDialog) {
            CustomEditDialog(
                dialogTitle = "Add Item",
                text = "",
                confirmText = stringResource(id = R.string.save),
                dismissText = stringResource(id = R.string.cancel),
                onConfirmClick = {
                    itemsViewModel.addToRoom(Item(noteId = noteId, title = it))
                    showAddItemDialog = false
                },
                onDismissClick = { showAddItemDialog = false }
            )
        }
    }
}

@Composable
fun ItemsAppBar(
    navController: NavController,
    noteId: String
) {
    val currentContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val itemsViewModel: ItemsViewModel = viewModel(factory = InjectorUtils.provideItemsViewModelFactory(context = currentContext, noteId = noteId))

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ){
        val currentNoteTitle = noteIdToNote(noteId).title
        var showDropdownMenu by remember { mutableStateOf(false) }
        var showDeleteCheckedItemsDialog by remember { mutableStateOf(false) }

        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return", tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.clickable(onClick = {
                navController.popBackStack()
            }),
        )
        Text(text = currentNoteTitle, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Column {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable(onClick = {
                    showDropdownMenu = !showDropdownMenu
                }),
            )
            DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = {
                    showDropdownMenu = false
                },
            ) {
                DropdownMenuItem(
                    leadingIcon = { Icon(imageVector = Icons.Default.Sync, contentDescription = stringResource(R.string.add_note)) },
                    text = { Text("sync") },
                    onClick = {
                        showDropdownMenu = false
                        coroutineScope.launch {
                            startSyncWorker(currentContext)
                        }
                    },
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.add_note)) },
                    text = { Text("Delete Checked Items") },
                    onClick = {
                        showDropdownMenu = false
                        showDeleteCheckedItemsDialog = true
                    },
                )
                DropdownMenuItem(
                    text = { Text("LogOut") },
                    leadingIcon = { Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.add_note)) },
                    onClick = {
                        showDropdownMenu = false
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
        if (showDeleteCheckedItemsDialog) {
            CustomChoiceDialog(
                dialogTitle = "${ stringResource(id = R.string.delete) } ${ stringResource(id = R.string.items) }",
                message = stringResource(id = R.string.delete_all_checked_items_message),
                confirmText = stringResource(id = R.string.delete),
                dismissText = stringResource(id = R.string.cancel),
                onConfirmClick = {
                    coroutineScope.launch {
                        itemsViewModel.markCheckedDeletedInRoom(noteId)
                    }
                    showDeleteCheckedItemsDialog = false
                },
                onDismissClick = {
                    showDeleteCheckedItemsDialog = false
                }
            )
        }
    }
}

@Composable
fun ItemsList(
    noteId: String
) {
    val coroutineScope = rememberCoroutineScope()
    val currentContext = LocalContext.current
    val itemsViewModel: ItemsViewModel = viewModel(factory = InjectorUtils.provideItemsViewModelFactory(context = currentContext, noteId = noteId))
    val items by itemsViewModel.items.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var itemToDelete by remember { mutableStateOf<Item?>(null) }

    val filteredItems =
        if (searchText.isNotBlank()) {
            items.filter { item ->
                item.title.contains(searchText, ignoreCase = true)
            }
        } else {
            items
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { searchText = "" } // Dismiss the dropdown menu when clicked outside
    ) {
        Column {
            // search field
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text(stringResource(id = R.string.search)) },
                modifier = Modifier.fillMaxWidth(),
            )
            // items
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(filteredItems) { item ->
                    ItemRow(
                        item = item,
                        onItemTap = {
                            coroutineScope.launch {
                                item.checked = !item.checked
                                itemsViewModel.updateInRoom(item)
                            }
                        },
                        onItemLongClick = {
                            itemToEdit = item
                        },
                        onDeleteClick = {
                            itemToDelete = item
                        }
                    )
                }
            }
            itemToEdit?.let { item ->
                CustomEditDialog(
                    dialogTitle = "Edit Item",
                    text = item.title,
                    confirmText = stringResource(id = R.string.save),
                    dismissText = stringResource(id = R.string.cancel),
                    onConfirmClick = {
                        item.title = it
                        itemsViewModel.updateInRoom(item)
                        itemToEdit = null
                    },
                    onDismissClick = { itemToEdit = null },
                )
            }
            itemToDelete?.let { item ->
                CustomChoiceDialog(
                    dialogTitle = "${ stringResource(id = R.string.delete) } ${ stringResource(id = R.string.item) }",
                    message = stringResource(id = R.string.delete_this_item_message),
                    confirmText = stringResource(id = R.string.delete),
                    dismissText = stringResource(id = R.string.cancel),
                    onConfirmClick = {
                        coroutineScope.launch {
                            itemsViewModel.markDeletedInRoom(item)
                        }
                        itemToDelete = null
                    },
                    onDismissClick = { itemToDelete = null }
                )
            }
        }
    }
}

@Composable
fun ItemRow(
    item: Item = Item(),
    onItemTap: (Item) -> Unit = {},
    onItemLongClick: (Item) -> Unit = {},
    onDeleteClick: (Item) -> Unit = {}
) {
    var deleteState by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(item.checked) }

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(1.dp)
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    isChecked = !isChecked
                    onItemTap(item)
                },
                onLongPress = { onItemLongClick(item) }
            )
        },
        shape = MaterialTheme.shapes.small,
    ) {
        Column (modifier = Modifier
            .padding(5.dp),
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(10.dp)
            ) {
                Text(text = item.title,
                    style = if (isChecked) {
                        MaterialTheme.typography.headlineSmall.copy(textDecoration = TextDecoration.LineThrough)
                    } else {
                        MaterialTheme.typography.headlineSmall
                    },
                    color = MaterialTheme.colorScheme.onSurface)
                Box(modifier = Modifier
                    .fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete_item),
                        modifier = Modifier
                            .clickable {
                                deleteState = !deleteState
                                onDeleteClick(item)
                            }
                    )
                }
            }
        }
    }
}
