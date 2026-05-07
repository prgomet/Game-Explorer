package com.test.gameexplorer.ui

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.test.gameexplorer.data.model.Game
import com.test.gameexplorer.ui.details.GameDetailsScreen
import com.test.gameexplorer.ui.gamelist.GameListScreen
import com.test.gameexplorer.ui.gamelist.GameListViewModel
import com.test.gameexplorer.ui.onboarding.OnboardingScreen
import com.test.gameexplorer.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object GameList : Screen("game_list")
    object Details : Screen("details")
    object Settings : Screen("settings")
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val gameListViewModel: GameListViewModel = hiltViewModel()
    val uiState by gameListViewModel.uiState.collectAsState()

    val startDestination = if (uiState.isOnboardingCompleted) Screen.GameList.route else Screen.Onboarding.route

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(Screen.GameList.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.GameList.route) {
                GameListScreen(
                    onGameClick = { game ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("game", game)
                        navController.navigate(Screen.Details.route)
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable
                )
            }
            composable(Screen.Details.route) {
                val game = remember {
                    navController.previousBackStackEntry?.savedStateHandle?.get<Game>("game")
                }
                game?.let {
                    GameDetailsScreen(
                        game = it,
                        onBackClick = { navController.popBackStack() },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
