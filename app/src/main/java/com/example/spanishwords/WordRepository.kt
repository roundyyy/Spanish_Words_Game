package com.example.spanishwords

import android.content.Context
import java.io.IOException

class WordRepository(private val context: Context) {
    private var allWordPairs: List<WordPair> = emptyList()

    suspend fun loadWordPairs(): List<WordPair> {
        if (allWordPairs.isEmpty()) {
            allWordPairs =
                    try {
                        context.assets.open("enes.txt").bufferedReader().use { reader ->
                            reader.readLines()
                                    .filter { it.isNotBlank() }
                                    .mapIndexed { index, line ->
                                        val parts = line.split(",", limit = 3)
                                        if (parts.size == 3) {
                                            WordPair(
                                                    english = parts[0].trim(),
                                                    spanish = parts[1].trim(),
                                                    mnemonic = parts[2].trim(),
                                                    id = index
                                            )
                                        } else null
                                    }
                                    .filterNotNull()
                        }
                    } catch (e: IOException) {
                        emptyList()
                    }
        }
        return allWordPairs
    }

    suspend fun getRandomWordPairs(count: Int = 6, usedWordIds: MutableSet<Int>): List<WordPair> {
        val allPairs = loadWordPairs()

        // If all words have been used, reset the used set
        if (usedWordIds.size >= allPairs.size) {
            usedWordIds.clear()
        }

        // Get available words (not used yet)
        val availableWords = allPairs.filter { !usedWordIds.contains(it.id) }

        return if (availableWords.size >= count) {
            availableWords.shuffled().take(count)
        } else {
            // If not enough unused words, fill with remaining available words
            availableWords.shuffled()
        }
    }
}
