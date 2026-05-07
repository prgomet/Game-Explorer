package com.test.gameexplorer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.gameexplorer.data.GameRepository
import com.test.gameexplorer.data.model.Genre
import com.test.gameexplorer.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val genres: List<Genre> = emptyList(),
    val selectedGenres: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOnline: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: GameRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    private val _isLoading = MutableStateFlow(value = false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        _genres,
        repository.getSelectedGenres(),
        _isLoading,
        _error,
        networkMonitor.isOnline
    ) { genres, selectedGenres, isLoading, error, isOnline ->
        SettingsUiState(
            genres = genres,
            selectedGenres = selectedGenres,
            isLoading = isLoading,
            error = error,
            isOnline = isOnline
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    init {
        loadGenres()
    }

    fun loadGenres() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _genres.value = repository.getGenres()
            } catch (e: Exception) {
                _error.value = e.message ?: "UNKNOWN CONNECTION ERROR"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleGenre(genre: Genre) {
        viewModelScope.launch {
            val current = uiState.value.selectedGenres.toMutableSet()
            val genreId = genre.id.toString()
            if (current.contains(genreId)) {
                current.remove(genreId)
            } else {
                current.add(genreId)
            }
            repository.saveSelectedGenres(current)
        }
    }
}
