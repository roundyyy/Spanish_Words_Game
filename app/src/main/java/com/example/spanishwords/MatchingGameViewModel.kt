package com.example.spanishwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameState(
        val spanishWords: List<WordPair> = emptyList(),
        val englishWords: List<WordPair> = emptyList(),
        val selectedSpanish: WordPair? = null,
        val selectedEnglish: WordPair? = null,
        val matchedPairs: Set<Int> = emptySet(),
        val isLoading: Boolean = true,
        val gameCompleted: Boolean = false,
        val mnemonicVisibleForId: Int? = null,
        val totalAttempts: Int = 0,
        val correctFirstAttempts: Int = 0,
        val mistakesMadeForWords: Set<Int> = emptySet(),
        val currentStreak: Int = 0,
        val highScoreStreak: Int = 0,
        val sessionTimeSeconds: Long = 0,
        val isSwapped: Boolean = false
) {
    val accuracyPercentage: Float
        get() =
                if (totalAttempts == 0) 100f
                else (correctFirstAttempts.toFloat() / totalAttempts.toFloat()) * 100f
}

class MatchingGameViewModel(
        private val repository: WordRepository,
        private val scoreManager: ScoreManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Session-wide accuracy tracking
    private var sessionTotalAttempts = 0
    private var sessionCorrectFirstAttempts = 0

    // Streak tracking
    private var currentStreak = 0
    private var highScoreStreak = scoreManager.getBestStreak()

    // Used words tracking
    private val usedWordIds = mutableSetOf<Int>()
    private val mnemonicViewedWords = mutableSetOf<Int>()

    // Session timer tracking
    private var sessionStartTime = System.currentTimeMillis()
    private var timerUpdateJob: kotlinx.coroutines.Job? = null

    // Callback for round completion
    private var onRoundCompleted: (() -> Unit)? = null

    // Swap setting
    private var isSwapped = scoreManager.getSwapColumns()

    init {
        loadNewRound()
        startTimer()
    }

    fun setOnRoundCompletedCallback(callback: () -> Unit) {
        onRoundCompleted = callback
    }

    fun loadNewRound() {
        viewModelScope.launch {
            _gameState.value = _gameState.value.copy(isLoading = true)

            val wordPairs = repository.getRandomWordPairs(6, usedWordIds)
            val shuffledEnglish = wordPairs.shuffled()

            _gameState.value =
                    GameState(
                            spanishWords = wordPairs,
                            englishWords = shuffledEnglish,
                            selectedSpanish = null,
                            selectedEnglish = null,
                            matchedPairs = emptySet(),
                            isLoading = false,
                            gameCompleted = false,
                            mnemonicVisibleForId = null,
                            totalAttempts = sessionTotalAttempts,
                            correctFirstAttempts = sessionCorrectFirstAttempts,
                            mistakesMadeForWords = emptySet(),
                            currentStreak = currentStreak,
                            highScoreStreak = highScoreStreak,
                            sessionTimeSeconds =
                                    (System.currentTimeMillis() - sessionStartTime) / 1000,
                            isSwapped = isSwapped
                    )
        }
    }

    fun selectSpanishWord(wordPair: WordPair) {
        val currentState = _gameState.value
        if (currentState.matchedPairs.contains(wordPair.id)) return

        if (currentState.isSwapped) {
            // When swapped, Spanish is the second selection
            val selectedEnglish = currentState.selectedEnglish
            if (selectedEnglish != null) {
                checkMatch(selectedEnglish, wordPair)
            } else {
                _gameState.value =
                        currentState.copy(
                                selectedSpanish = wordPair,
                                selectedEnglish = null,
                                mnemonicVisibleForId = null
                        )
            }
        } else {
            // When not swapped, Spanish is the first selection
            _gameState.value =
                    currentState.copy(
                            selectedSpanish = wordPair,
                            selectedEnglish = null,
                            mnemonicVisibleForId = null
                    )
        }
    }

    fun selectEnglishWord(wordPair: WordPair) {
        val currentState = _gameState.value
        if (currentState.matchedPairs.contains(wordPair.id)) return

        if (currentState.isSwapped) {
            // When swapped, English is the first selection
            _gameState.value =
                    currentState.copy(
                            selectedEnglish = wordPair,
                            selectedSpanish = null,
                            mnemonicVisibleForId = null
                    )
        } else {
            // When not swapped, English is the second selection
            val selectedSpanish = currentState.selectedSpanish
            if (selectedSpanish != null) {
                checkMatch(selectedSpanish, wordPair)
            } else {
                _gameState.value =
                        currentState.copy(
                                selectedEnglish = wordPair,
                                selectedSpanish = null,
                                mnemonicVisibleForId = null
                        )
            }
        }
    }

    private fun checkMatch(firstWord: WordPair, secondWord: WordPair) {
        val currentState = _gameState.value
        if (firstWord.id == secondWord.id) {
            // Correct match
            sessionTotalAttempts++
            if (!currentState.mistakesMadeForWords.contains(secondWord.id)) {
                sessionCorrectFirstAttempts++
                currentStreak++
                if (currentStreak > highScoreStreak) {
                    highScoreStreak = currentStreak
                    scoreManager.saveBestStreak(highScoreStreak)
                }
            }

            // Add to used words only if mnemonic wasn't viewed
            if (!mnemonicViewedWords.contains(secondWord.id)) {
                usedWordIds.add(secondWord.id)
            }

            val newMatchedPairs = currentState.matchedPairs + secondWord.id
            val gameCompleted = newMatchedPairs.size == currentState.spanishWords.size

            _gameState.value =
                    currentState.copy(
                            selectedSpanish = null,
                            selectedEnglish = null,
                            matchedPairs = newMatchedPairs,
                            gameCompleted = gameCompleted,
                            mnemonicVisibleForId = null,
                            totalAttempts = sessionTotalAttempts,
                            correctFirstAttempts = sessionCorrectFirstAttempts,
                            currentStreak = currentStreak,
                            highScoreStreak = highScoreStreak,
                            sessionTimeSeconds =
                                    (System.currentTimeMillis() - sessionStartTime) / 1000
                    )

            if (gameCompleted) {
                // Trigger complete sound
                onRoundCompleted?.invoke()
                // Auto-load new round after a short delay
                viewModelScope.launch {
                    kotlinx.coroutines.delay(1500)
                    loadNewRound()
                }
            }
        } else {
            // Wrong match - reset streak and track mistake
            currentStreak = 0
            val newMistakesMade = currentState.mistakesMadeForWords + firstWord.id
            _gameState.value =
                    currentState.copy(
                            selectedEnglish = if (currentState.isSwapped) null else secondWord,
                            selectedSpanish = if (currentState.isSwapped) secondWord else null,
                            mistakesMadeForWords = newMistakesMade,
                            currentStreak = currentStreak,
                            sessionTimeSeconds =
                                    (System.currentTimeMillis() - sessionStartTime) / 1000
                    )
            viewModelScope.launch {
                kotlinx.coroutines.delay(800)
                _gameState.value =
                        _gameState.value.copy(selectedSpanish = null, selectedEnglish = null)
            }
        }
    }

    fun showMnemonic(wordPairId: Int) {
        mnemonicViewedWords.add(wordPairId)
        _gameState.value = _gameState.value.copy(mnemonicVisibleForId = wordPairId)
    }

    fun hideMnemonic() {
        _gameState.value = _gameState.value.copy(mnemonicVisibleForId = null)
    }

    fun resetSession() {
        sessionTotalAttempts = 0
        sessionCorrectFirstAttempts = 0
        currentStreak = 0
        // Don't reset highScoreStreak - it should persist across sessions
        usedWordIds.clear()
        mnemonicViewedWords.clear()
        // Reset session timer
        sessionStartTime = System.currentTimeMillis()
        loadNewRound()
    }

    fun toggleSwapColumns() {
        isSwapped = !isSwapped
        scoreManager.saveSwapColumns(isSwapped)
        _gameState.value = _gameState.value.copy(isSwapped = isSwapped)
    }

    private fun startTimer() {
        timerUpdateJob?.cancel()
        timerUpdateJob =
                viewModelScope.launch {
                    while (true) {
                        delay(1000) // Update every second
                        val currentState = _gameState.value
                        _gameState.value =
                                currentState.copy(
                                        sessionTimeSeconds =
                                                (System.currentTimeMillis() - sessionStartTime) /
                                                        1000
                                )
                    }
                }
    }

    override fun onCleared() {
        super.onCleared()
        timerUpdateJob?.cancel()
    }
}
