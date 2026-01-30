package com.example.takeanote1.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    showBack: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    onSortClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    onViewTypeClick: (() -> Unit)? = null,
    isGridView: Boolean = false,
    onLogoutClick: (() -> Unit)? = null,
    onSwitchAccountClick: (() -> Unit)? = null,
    onHistoryClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = { onBackClick?.invoke() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            actions()

            if (onViewTypeClick != null) {
                IconButton(onClick = { onViewTypeClick.invoke() }) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                        contentDescription = "Toggle View Type"
                    )
                }
            }

            val hasMenuActions = onSearchClick != null || onSortClick != null ||
                    onFilterClick != null || onHistoryClick != null ||
                    onSwitchAccountClick != null || onLogoutClick != null

            if (hasMenuActions) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (onSearchClick != null) {
                            DropdownMenuItem(
                                text = { Text("Search") },
                                onClick = {
                                    showMenu = false
                                    onSearchClick.invoke()
                                },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                            )
                        }
                        if (onSortClick != null) {
                            DropdownMenuItem(
                                text = { Text("Sort") },
                                onClick = {
                                    showMenu = false
                                    onSortClick.invoke()
                                },
                                leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null) }
                            )
                        }
                        if (onFilterClick != null) {
                            DropdownMenuItem(
                                text = { Text("Filter") },
                                onClick = {
                                    showMenu = false
                                    onFilterClick.invoke()
                                },
                                leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) }
                            )
                        }

                        if ((onSearchClick != null || onSortClick != null || onFilterClick != null) &&
                            (onHistoryClick != null || onSwitchAccountClick != null || onLogoutClick != null)
                        ) {
                            HorizontalDivider()
                        }

                        if (onHistoryClick != null) {
                            DropdownMenuItem(
                                text = { Text("Past Notes") },
                                onClick = {
                                    showMenu = false
                                    onHistoryClick.invoke()
                                }
                            )
                        }
                        if (onSwitchAccountClick != null) {
                            DropdownMenuItem(
                                text = { Text("Switch Account") },
                                onClick = {
                                    showMenu = false
                                    onSwitchAccountClick.invoke()
                                }
                            )
                        }
                        if (onLogoutClick != null) {
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    showMenu = false
                                    onLogoutClick.invoke()
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}
