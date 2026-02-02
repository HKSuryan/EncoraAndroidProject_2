package com.example.takeanote1.ui.completed

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
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
import com.example.takeanote1.ui.auth.AuthViewModel
import com.example.takeanote1.ui.components.NoteCard
import com.example.takeanote1.ui.home.NotesViewModel
import com.example.takeanote1.ui.components.AppTopBar
import com.example.takeanote1.ui.components.FilterDialog
import com.example.takeanote1.ui.components.SortDialog
import com.example.takeanote1.ui.home.ViewType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompletedNotesScreen(
    viewModel: NotesViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onHistoryClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onLoginNavigate: () -> Unit,
    onEditNoteClick: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val pagedNotes = viewModel.completedNotesPaged.collectAsLazyPagingItems()
    val viewType by viewModel.viewType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showSearchField by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                AppTopBar(
                    title = "History (Completed)",
                    showBack = true,
                    onBackClick = onBack,
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
                        placeholder = { Text("Search completed notes...") },
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
        }
    ) { padding ->
        if (pagedNotes.itemCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No completed notes found.")
            }
        } else {
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
                                showCompleteButton = false,
                                showEditButton = false,
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
                                showCompleteButton = false,
                                showEditButton = false,
                                onDeleteClick = { viewModel.deleteNote(it.id) }
                            )
                        }
                    }
                }
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
