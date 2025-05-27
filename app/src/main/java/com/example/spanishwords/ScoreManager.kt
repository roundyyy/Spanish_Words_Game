package com.example.spanishwords

import android.content.Context
import android.content.SharedPreferences

class ScoreManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("spanish_words_scores", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_SWAP_COLUMNS = "swap_columns"
    }

    fun getBestStreak(): Int {
        return sharedPreferences.getInt(KEY_BEST_STREAK, 0)
    }

    fun saveBestStreak(streak: Int) {
        sharedPreferences.edit().putInt(KEY_BEST_STREAK, streak).apply()
    }

    fun getSwapColumns(): Boolean {
        return sharedPreferences.getBoolean(KEY_SWAP_COLUMNS, false)
    }

    fun saveSwapColumns(swapped: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SWAP_COLUMNS, swapped).apply()
    }
}
