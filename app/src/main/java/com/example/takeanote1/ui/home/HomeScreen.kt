package com.example.takeanote1.ui.home

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.example.takeanote1.ui.components.SortDialog
import com.example.takeanote1.ui.components.FilterDialog
import com.example.takeanote1.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                    onHistoryClick = onHistoryClick,
                    onRemindersClick = onRemindersClick
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
            },
            currentTopic = viewModel.topicFilter.collectAsState().value,
            currentDateRange = viewModel.dateRangeFilter.collectAsState().value,
            onClearFilters = {
                viewModel.clearFilters()
                showFilterDialog = false
            }
        )
    }
}
