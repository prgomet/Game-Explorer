package com.test.gameexplorer.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.gameexplorer.data.GameRepository
import com.test.gameexplorer.data.model.GameDetails
import com.test.gameexplorer.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameDetailsUiState(
    val details: GameDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOnline: Boolean = true,
)

@HiltViewModel
class GameDetailsViewModel @Inject constructor(
    private val repository: GameRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _details = MutableStateFlow<GameDetails?>(null)
    private val _isLoading = MutableStateFlow(value = false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GameDetailsUiState> = combine(
        _details,
        _isLoading,
        _error,
        networkMonitor.isOnline
    ) { details, isLoading, error, isOnline ->
        GameDetailsUiState(
            details = details,
            isLoading = isLoading,
            error = error,
            isOnline = isOnline
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameDetailsUiState()
    )

    fun fetchGameDetails(gameId: Int) {
        // Avoid redundant network calls if details for this game are already loaded
        if (_details.value?.id == gameId) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _details.value = repository.getGameDetails(gameId)
            } catch (_: java.io.IOException) {
                _error.value = "NETWORK ERROR: Signal lost. Reconvene later."
            } catch (e: Exception) {
                _error.value = "INTEL FETCH FAILED: ${e.localizedMessage ?: "Unknown Error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
