package com.test.gameexplorer.ui.gamelist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.test.gameexplorer.R
import com.test.gameexplorer.data.model.Game
import com.test.gameexplorer.ui.EmptyScreen
import com.test.gameexplorer.ui.ErrorItem
import com.test.gameexplorer.ui.ErrorScreen
import com.test.gameexplorer.ui.GamerLoadingIndicator
import com.test.gameexplorer.ui.LoadingScreen
import com.test.gameexplorer.ui.getColumns
import com.test.gameexplorer.util.LocalWindowSizeClass

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun GameListScreen(
    onGameClick: (Game) -> Unit,
    onSettingsClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: GameListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val games = viewModel.games.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val windowSizeClass = LocalWindowSizeClass.current
    val columns = windowSizeClass.getColumns()

    // Control the pull-to-refresh indicator state
    var isManualRefreshing by remember { mutableStateOf(false) }
    
    // Sync the manual refresh state with the paging load state
    LaunchedEffect(games.loadState.refresh) {
        if (games.loadState.refresh !is LoadState.Loading) {
            isManualRefreshing = false
        }
    }

    // Auto-refresh logic when recovering from an error or returning online
    LaunchedEffect(uiState.isOnline) {
        val hasNoData = games.itemCount == 0
        val isErrorState = games.loadState.refresh is LoadState.Error
        
        if (uiState.isOnline && (isErrorState || hasNoData)) {
            games.refresh()
        }
    }

    // Show a tactical notification when we're viewing cached data due to network loss
    val offlineMsg = stringResource(R.string.offline_mode)
    LaunchedEffect(games.loadState.refresh) {
        if (games.loadState.refresh is LoadState.Error && games.itemCount > 0) {
            snackbarHostState.showSnackbar(
                message = offlineMsg,
                duration = SnackbarDuration.Short,
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.app_name).uppercase(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isManualRefreshing,
            onRefresh = {
                isManualRefreshing = true
                games.refresh()
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        count = games.itemCount,
                        key = { index -> games.peek(index)?.id ?: index }
                    ) { index ->
                        games[index]?.let { game ->
                            GameItem(
                                game = game,
                                onClick = { onGameClick(game) },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    }

                    when (val state = games.loadState.append) {
                        is LoadState.Error -> {
                            item(span = { GridItemSpan(columns) }) {
                                ErrorItem(
                                    message = state.error.message ?: "Error loading more",
                                    onRetry = { games.retry() }
                                )
                            }
                        }

                        is LoadState.Loading -> {
                            item(span = { GridItemSpan(columns) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GamerLoadingIndicator()
                                }
                            }
                        }

                        else -> {}
                    }
                }

                AnimatedContent(
                    targetState = games.loadState.refresh,
                    label = "refreshState",
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    }
                ) { refreshState ->
                    when {
                        (refreshState is LoadState.Loading) && games.itemCount == 0 -> {
                            LoadingScreen()
                        }

                        (refreshState is LoadState.Error) && games.itemCount == 0 -> {
                            ErrorScreen(
                                message = stringResource(R.string.error_no_intel),
                                onRetry = { games.retry() }
                            )
                        }

                        games.itemCount == 0 && refreshState !is LoadState.Loading -> {
                            EmptyScreen(onActionClick = onSettingsClick)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun GameItem(
    game: Game,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current
    
    // Optimize image loading by crossfading and using a specific key for the game
    val imageRequest = remember(game.backgroundImage) {
        ImageRequest.Builder(context)
            .data(game.backgroundImage)
            .crossfade(true)
            .build()
    }

    // Dynamic rating colors to give instant visual feedback on quality
    val ratingColor = when {
        game.rating >= 4.5 -> Color(0xFF00E676) // Elite
        game.rating >= 4.0 -> Color(0xFFCCFF00) // Recommended
        game.rating >= 3.0 -> Color(0xFFFFD600) // Average
        else -> Color(0xFFFF5252) // Warning
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { onClick() }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = ratingColor.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(modifier = Modifier.height(280.dp)) {
            // Main background image with Shared Element support
            with(sharedTransitionScope) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedElement(
                            rememberSharedContentState(key = "image-${game.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            // A vertical gradient to ensure the text remains legible over light images
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 350f
                        )
                    )
            )

            // Game details overlay
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                with(sharedTransitionScope) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 2,
                        modifier = Modifier
                            .weight(1f)
                            .sharedElement(
                                rememberSharedContentState(key = "title-${game.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Rating Badge
                Surface(
                    color = ratingColor,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = game.rating.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }
}
