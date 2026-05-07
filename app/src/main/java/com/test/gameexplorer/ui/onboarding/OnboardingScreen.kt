package com.test.gameexplorer.ui.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val windowSizeClass = LocalWindowSizeClass.current

    // Local state to manage the visual refresh indicator
    var isManualRefreshing by remember { mutableStateOf(false) }

    // Sync the refresh indicator with the loading state from the ViewModel
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isManualRefreshing = false
        }
    }

    // Auto-fetch if we're online and don't have genres yet (e.g., first run or retry)
    LaunchedEffect(uiState.isOnline) {
        if (uiState.isOnline && (uiState.error != null || uiState.genres.isEmpty())) {
            viewModel.loadGenres()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.initializing_profile),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Only show the "Start Mission" button once we've successfully loaded genres
            if (uiState.genres.isNotEmpty()) {
                BottomMissionBar(
                    isReady = uiState.selectedGenres.isNotEmpty(),
                    onClick = {
                        viewModel.completeOnboarding()
                        onOnboardingComplete()
                    }
                )
            }
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
                        text = stringResource(R.string.select_combat_zones),
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

@Composable
private fun BottomMissionBar(
    isReady: Boolean,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(60.dp),
            enabled = isReady,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            )
        ) {
            Text(
                text = stringResource(R.string.start_mission),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            )
        }
    }
}
