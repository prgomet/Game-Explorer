package com.test.gameexplorer.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.test.gameexplorer.R
import com.test.gameexplorer.ui.GenreSelectionGrid
import com.test.gameexplorer.util.LocalWindowSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val windowSizeClass = LocalWindowSizeClass.current

    // Visual refresh state for the pull-to-refresh component
    var isManualRefreshing by remember { mutableStateOf(false) }

    // Sync refresh indicator with ViewModel loading state
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isManualRefreshing = false
        }
    }

    // Auto-retry fetch if we go back online and had an error previously
    LaunchedEffect(uiState.isOnline) {
        if (uiState.isOnline && (uiState.error != null || uiState.genres.isEmpty())) {
            viewModel.loadGenres()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.command_center),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isManualRefreshing,
            onRefresh = {
                isManualRefreshing = true
                viewModel.loadGenres()
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            GenreSelectionGrid(
                genres = uiState.genres,
                selectedGenres = uiState.selectedGenres,
                isLoading = uiState.isLoading,
                error = uiState.error,
                windowSizeClass = windowSizeClass,
                onToggle = { viewModel.toggleGenre(it) },
                onRetry = { viewModel.loadGenres() },
                header = {
                    Text(
                        text = stringResource(R.string.genre_configuration),
                        modifier = Modifier.padding(bottom = 24.dp, top = 8.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            )
        }
    }
}
