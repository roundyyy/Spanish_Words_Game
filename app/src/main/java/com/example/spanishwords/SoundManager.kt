package com.example.spanishwords

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0
    private var completeSoundId: Int = 0
    private var isLoaded = false

    init {
        try {
            // Configure audio attributes for consistent playback
            val audioAttributes =
                    AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()

            // Create SoundPool - better for short sound effects than MediaPlayer
            soundPool =
                    SoundPool.Builder()
                            .setMaxStreams(2) // Allow 2 concurrent sounds
                            .setAudioAttributes(audioAttributes)
                            .build()

            soundPool?.let { pool ->
                // Load sounds
                clickSoundId = pool.load(context.assets.openFd("click.mp3"), 1)
                completeSoundId = pool.load(context.assets.openFd("complete.mp3"), 1)

                // Set load complete listener
                pool.setOnLoadCompleteListener { _, _, status ->
                    if (status == 0) { // Success
                        isLoaded = true
                        Log.d("SoundManager", "Sounds loaded successfully")
                    } else {
                        Log.e("SoundManager", "Failed to load sounds, status: $status")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error initializing sound system", e)
        }
    }

    fun playClickSound() {
        try {
            if (isLoaded && clickSoundId > 0) {
                soundPool?.play(
                        clickSoundId,
                        1.0f, // left volume
                        1.0f, // right volume
                        1, // priority
                        0, // loop (0 = no loop)
                        1.0f // rate (1.0 = normal speed)
                )
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing click sound", e)
        }
    }

    fun playCompleteSound() {
        try {
            if (isLoaded && completeSoundId > 0) {
                soundPool?.play(
                        completeSoundId,
                        1.0f, // left volume
                        1.0f, // right volume
                        1, // priority
                        0, // loop (0 = no loop)
                        1.0f // rate (1.0 = normal speed)
                )
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing complete sound", e)
        }
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            isLoaded = false
            clickSoundId = 0
            completeSoundId = 0
        } catch (e: Exception) {
            Log.e("SoundManager", "Error releasing sound resources", e)
        }
    }
}
