package com.example.spanishwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MatchingGameViewModelFactory(
        private val repository: WordRepository,
        private val scoreManager: ScoreManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchingGameViewModel::class.java)) {
            return MatchingGameViewModel(repository, scoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
