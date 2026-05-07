package com.test.gameexplorer.ui.gamelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.test.gameexplorer.data.GameRepository
import com.test.gameexplorer.data.model.Game
import com.test.gameexplorer.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class GameListUiState(
    val isOnboardingCompleted: Boolean = true,
    val isOnline: Boolean = true,
)

@HiltViewModel
class GameListViewModel @Inject constructor(
    private val repository: GameRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val games: Flow<PagingData<Game>> = repository.getSelectedGenres()
        .distinctUntilChanged()
        .flatMapLatest { genres ->
            repository.getGames(genres)
        }
        .cachedIn(viewModelScope)

    val uiState: StateFlow<GameListUiState> = combine(
        repository.isOnboardingCompleted(),
        networkMonitor.isOnline
    ) { completed, isOnline ->
        GameListUiState(
            isOnboardingCompleted = completed,
            isOnline = isOnline
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameListUiState()
    )
}
