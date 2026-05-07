package com.test.gameexplorer.ui.details

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.test.gameexplorer.R
import com.test.gameexplorer.data.model.Game
import com.test.gameexplorer.ui.GamerLoadingIndicator
import com.test.gameexplorer.util.DateFormatter
import com.test.gameexplorer.util.LocalWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun GameDetailsScreen(
    game: Game,
    onBackClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: GameDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val windowSizeClass = LocalWindowSizeClass.current

    // Trigger details fetch when the game ID changes or screen is first composed
    LaunchedEffect(game.id) {
        viewModel.fetchGameDetails(game.id)
    }

    // Determine if we should use a two-column layout based on the window width
    val isExpanded = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            game.name.uppercase(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
        val context = LocalContext.current
        val imageRequest = remember(game.backgroundImage) {
            ImageRequest.Builder(context)
                .data(game.backgroundImage)
                .crossfade(true)
                .build()
        }

        if (isExpanded) {
            // Master-Detail style layout for large screens (tablets/foldables)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                with(sharedTransitionScope) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            // Unique key for shared element transition from the list screen
                            .sharedElement(
                                rememberSharedContentState(key = "image-${game.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    GameDetailsContent(
                        game = game,
                        uiState = uiState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            }
        } else {
            // Standard vertical layout for compact devices (phones)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                with(sharedTransitionScope) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .sharedElement(
                                rememberSharedContentState(key = "image-${game.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
                GameDetailsContent(
                    game = game,
                    uiState = uiState,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun GameDetailsContent(
    game: Game,
    uiState: GameDetailsUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {

    val ratingColor = when {
        game.rating >= 4.5 -> Color(0xFF00E676) // Bright Green
        game.rating >= 4.0 -> Color(0xFFCCFF00) // Lime
        game.rating >= 3.0 -> Color(0xFFFFD600) // Yellow
        else -> Color(0xFFFF5252) // Red
    }

    Column(modifier = Modifier.padding(16.dp)) {
        with(sharedTransitionScope) {
            Text(
                text = game.name.uppercase(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                // Shared element for title transition
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "title-${game.id}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Rating badge with dynamic color based on the value
            Surface(
                color = ratingColor,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = game.rating.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.released, DateFormatter.format(game.released)),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GamerLoadingIndicator()
            }
        } else if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            uiState.details?.let { details ->
                if (details.description.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.about),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = details.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (details.developers.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.developers),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = details.developers.joinToString(", ").uppercase(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (details.publishers.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.publishers),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = details.publishers.joinToString(", ").uppercase(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (details.platforms.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.platforms),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = details.platforms.joinToString(", ").uppercase(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = stringResource(R.string.genres),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    details.genres.sorted().forEach { genre ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(genre.uppercase()) },
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    }
}
