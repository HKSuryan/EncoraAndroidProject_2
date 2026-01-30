package com.example.takeanote1.ui.home

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.takeanote1.ui.auth.AuthUiState
import com.example.takeanote1.ui.components.NoteCard
import com.example.takeanote1.ui.components.AppTopBar
import com.example.takeanote1.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NotesViewModel,
    authViewModel: AuthViewModel,
    onAddNoteClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onEditNoteClick: (String) -> Unit,
    onLoginNavigate: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authState by authViewModel.uiState.collectAsState()
    
    val pagedNotes = viewModel.notesPaged.collectAsLazyPagingItems()
    val viewType by viewModel.viewType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showSearchField by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            AuthUiState.LoggedOut -> onLoginNavigate()
            AuthUiState.SwitchAccountRequired -> onLoginNavigate()
            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                AppTopBar(
                    title = "My Notes",
                    onSearchClick = { showSearchField = !showSearchField },
                    onSortClick = { showSortDialog = true },
                    onFilterClick = { showFilterDialog = true },
                    onViewTypeClick = { viewModel.toggleViewType() },
                    isGridView = viewType == ViewType.GRID,
                    onLogoutClick = { authViewModel.logoutKeepAccount() },
                    onSwitchAccountClick = { activity?.let { authViewModel.switchAccount(it) } },
                    onHistoryClick = onHistoryClick
                )
                if (showSearchField) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search notes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { 
                                viewModel.setSearchQuery("")
                                showSearchField = false 
                            }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        },
                        singleLine = true
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNoteClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { padding ->
        if (viewType == ViewType.LIST) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(
                    count = pagedNotes.itemCount,
                    key = pagedNotes.itemKey { it.id },
                    contentType = pagedNotes.itemContentType { "note" }
                ) { index ->
                    val note = pagedNotes[index]
                    note?.let {
                        NoteCard(
                            note = it,
                            onCompleteClick = { viewModel.markAsCompleted(it.id) },
                            onEditClick = { onEditNoteClick(it.id) },
                            onDeleteClick = { viewModel.deleteNote(it.id) }
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(
                    count = pagedNotes.itemCount,
                    key = pagedNotes.itemKey { it.id },
                    contentType = pagedNotes.itemContentType { "note" }
                ) { index ->
                    val note = pagedNotes[index]
                    note?.let {
                        NoteCard(
                            note = it,
                            onCompleteClick = { viewModel.markAsCompleted(it.id) },
                            onEditClick = { onEditNoteClick(it.id) },
                            onDeleteClick = { viewModel.deleteNote(it.id) }
                        )
                    }
                }
            }
        }

        if (pagedNotes.itemCount == 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No notes found")
            }
        }
    }

    if (showSortDialog) {
        SortDialog(
            onDismiss = { showSortDialog = false },
            onSortSelected = { field, order ->
                viewModel.setSort(field, order)
                showSortDialog = false
            }
        )
    }

    if (showFilterDialog) {
        FilterDialog(
            onDismiss = { showFilterDialog = false },
            onTopicSelected = { topic ->
                viewModel.setTopicFilter(topic)
                showFilterDialog = false
            },
            onDateRangeSelected = { start, end ->
                viewModel.setDateRangeFilter(start, end)
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun SortDialog(onDismiss: () -> Unit, onSortSelected: (String, String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort By") },
        text = {
            Column {
                TextButton(onClick = { onSortSelected("createdAt", "DESC") }) {
                    Text("Date (Newest First)")
                }
                TextButton(onClick = { onSortSelected("createdAt", "ASC") }) {
                    Text("Date (Oldest First)")
                }
                TextButton(onClick = { onSortSelected("title", "ASC") }) {
                    Text("Title (A-Z)")
                }
                TextButton(onClick = { onSortSelected("title", "DESC") }) {
                    Text("Title (Z-A)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    onDismiss: () -> Unit, 
    onTopicSelected: (String?) -> Unit,
    onDateRangeSelected: (Long?, Long?) -> Unit
) {
    val topics = listOf("All", "Work", "Personal", "Study", "Ideas", "Other")
    val state = rememberDateRangePickerState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Notes") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("By Topic:", style = MaterialTheme.typography.labelLarge)
                topics.forEach { topic ->
                    TextButton(onClick = { onTopicSelected(topic) }) {
                        Text(topic)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("By Date Range:", style = MaterialTheme.typography.labelLarge)
                DateRangePicker(
                    state = state,
                    modifier = Modifier.height(400.dp),
                    title = null,
                    headline = null,
                    showModeToggle = false
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDateRangeSelected(state.selectedStartDateMillis, state.selectedEndDateMillis)
            }) {
                Text("Apply Date Filter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
