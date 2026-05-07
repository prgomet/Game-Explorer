package com.test.gameexplorer.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.gameexplorer.data.GameRepository
import com.test.gameexplorer.data.model.Genre
import com.test.gameexplorer.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val genres: List<Genre> = emptyList(),
    val selectedGenres: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOnline: Boolean = true,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: GameRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    private val _selectedGenres = MutableStateFlow<Set<String>>(emptySet())
    private val _isLoading = MutableStateFlow(value = false)
    private val _error = MutableStateFlow<String?>(null)

    // Combine multiple data sources into a single UI State for the view to consume.
    // This includes genres from the repository and real-time network connectivity status.
    val uiState: StateFlow<OnboardingUiState> = combine(
        _genres,
        _selectedGenres,
        _isLoading,
        _error,
        networkMonitor.isOnline
    ) { genres, selectedGenres, isLoading, error, isOnline ->
        OnboardingUiState(
            genres = genres,
            selectedGenres = selectedGenres,
            isLoading = isLoading,
            error = error,
            isOnline = isOnline
        )
    }.stateIn(
        scope = viewModelScope,
        // Keep the upstream flow active for 5 seconds after the last subscriber leaves
        // to handle configuration changes (like rotation) without restarting.
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OnboardingUiState()
    )

    init {
        loadGenres()
    }

    /**
     * Fetches the list of available game genres from the repository.
     */
    fun loadGenres() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val genres = repository.getGenres()
                if (genres.isEmpty()) {
                    _error.value = "No genres found. Mission failed."
                } else {
                    _genres.value = genres
                }
            } catch (_: java.io.IOException) {
                _error.value = "NETWORK ERROR: Comms link down. Check your connection."
            } catch (e: Exception) {
                _error.value = "UNKNOWN SYSTEM ERROR: ${e.localizedMessage ?: "Critical failure"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggles a genre's selection state.
     */
    fun toggleGenre(genre: Genre) {
        val current = _selectedGenres.value.toMutableSet()
        val genreId = genre.id.toString()
        if (current.contains(genreId)) {
            current.remove(genreId)
        } else {
            current.add(genreId)
        }
        _selectedGenres.value = current
    }

    /**
     * Persists the user's selections and flags onboarding as completed.
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            repository.saveSelectedGenres(_selectedGenres.value)
            repository.setOnboardingCompleted(true)
        }
    }
}
