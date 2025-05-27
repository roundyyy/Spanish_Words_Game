package com.example.spanishwords

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spanishwords.ui.theme.*

@Composable
fun MatchingGameScreen() {
        val context = LocalContext.current
        val repository = remember { WordRepository(context) }
        val scoreManager = remember { ScoreManager(context) }
        val viewModelFactory = remember { MatchingGameViewModelFactory(repository, scoreManager) }
        val viewModel: MatchingGameViewModel = viewModel(factory = viewModelFactory)
        val gameState by viewModel.gameState.collectAsState()

        // Sound manager
        val soundManager = remember { SoundManager(context) }

        // Set up complete sound callback
        LaunchedEffect(viewModel) {
                viewModel.setOnRoundCompletedCallback { soundManager.playCompleteSound() }
        }

        // Clean up sound manager when composable is disposed
        DisposableEffect(Unit) { onDispose { soundManager.release() } }

        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(16.dp)
        ) {
                if (gameState.isLoading) {
                        CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                        )
                } else {
                        Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                                // Top spacing for notch/camera
                                Spacer(modifier = Modifier.height(16.dp))

                                // Progress indicator at the top
                                if (gameState.gameCompleted) {
                                        Text(
                                                text = "Great job! Loading new words...",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color =
                                                        if (MaterialTheme.colorScheme.background ==
                                                                        DarkBackground
                                                        )
                                                                DarkSuccess
                                                        else LightSuccess,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                } else {
                                        LinearProgressIndicator(
                                                progress = {
                                                        gameState.matchedPairs.size.toFloat() /
                                                                gameState.spanishWords.size
                                                                        .toFloat()
                                                },
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .height(8.dp)
                                                                .clip(RoundedCornerShape(4.dp)),
                                                color =
                                                        if (MaterialTheme.colorScheme.background ==
                                                                        DarkBackground
                                                        )
                                                                DarkSuccess
                                                        else LightSuccess,
                                                trackColor =
                                                        if (MaterialTheme.colorScheme.background ==
                                                                        DarkBackground
                                                        )
                                                                DarkProgressTrack
                                                        else LightProgressTrack
                                        )
                                }

                                // Title
                                Text(
                                        text = "Match the pairs",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                )

                                // Stats Row: Accuracy, Current Streak, High Score, Session Timer
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                        // Session Timer
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                        text = "Session Time",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onBackground.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                                Text(
                                                        text =
                                                                formatTime(
                                                                        gameState.sessionTimeSeconds
                                                                ),
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onBackground
                                                )
                                        }

                                        // Accuracy
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                        text = "Accuracy",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onBackground.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                                Text(
                                                        text =
                                                                "${String.format("%.1f", gameState.accuracyPercentage)}%",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color =
                                                                if (gameState.accuracyPercentage >=
                                                                                80
                                                                ) {
                                                                        if (MaterialTheme
                                                                                        .colorScheme
                                                                                        .background ==
                                                                                        DarkBackground
                                                                        )
                                                                                DarkSuccess
                                                                        else LightSuccess
                                                                } else {
                                                                        MaterialTheme.colorScheme
                                                                                .onBackground
                                                                }
                                                )
                                        }

                                        // Current Streak
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                        text = "Current Streak",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onBackground.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                                Text(
                                                        text = "${gameState.currentStreak}",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onBackground
                                                )
                                        }

                                        // High Score
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                        text = "Best Streak",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onBackground.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                                Text(
                                                        text = "${gameState.highScoreStreak}",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color =
                                                                if (MaterialTheme.colorScheme
                                                                                .background ==
                                                                                DarkBackground
                                                                )
                                                                        DarkSuccess
                                                                else LightSuccess
                                                )
                                        }
                                }

                                // Language headers with swap button
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        // Left language label
                                        Text(
                                                text =
                                                        if (gameState.isSwapped) "English"
                                                        else "Spanish",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(1f)
                                        )

                                        // Swap button
                                        IconButton(
                                                onClick = {
                                                        soundManager.playClickSound()
                                                        viewModel.toggleSwapColumns()
                                                },
                                                modifier = Modifier.size(48.dp)
                                        ) {
                                                Icon(
                                                        painter =
                                                                painterResource(
                                                                        id = R.drawable.ic_swap
                                                                ),
                                                        contentDescription = "Swap columns",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(24.dp)
                                                )
                                        }

                                        // Right language label
                                        Text(
                                                text =
                                                        if (gameState.isSwapped) "Spanish"
                                                        else "English",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(1f)
                                        )
                                }

                                // Game Area
                                Row(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                        // Left column (Spanish or English depending on swap state)
                                        Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                if (gameState.isSwapped) {
                                                        // Show English words on left when swapped
                                                        gameState.englishWords.forEach { wordPair ->
                                                                WordCard(
                                                                        word = wordPair.english,
                                                                        isSelected =
                                                                                gameState
                                                                                        .selectedEnglish
                                                                                        ?.id ==
                                                                                        wordPair.id,
                                                                        isMatched =
                                                                                gameState
                                                                                        .matchedPairs
                                                                                        .contains(
                                                                                                wordPair.id
                                                                                        ),
                                                                        onClick = {
                                                                                soundManager
                                                                                        .playClickSound()
                                                                                viewModel
                                                                                        .selectEnglishWord(
                                                                                                wordPair
                                                                                        )
                                                                        }
                                                                )
                                                        }
                                                } else {
                                                        // Show Spanish words on left when not
                                                        // swapped
                                                        gameState.spanishWords.forEach { wordPair ->
                                                                SpanishWordCard(
                                                                        wordPair = wordPair,
                                                                        isSelected =
                                                                                gameState
                                                                                        .selectedSpanish
                                                                                        ?.id ==
                                                                                        wordPair.id,
                                                                        isMatched =
                                                                                gameState
                                                                                        .matchedPairs
                                                                                        .contains(
                                                                                                wordPair.id
                                                                                        ),
                                                                        showMnemonic =
                                                                                gameState
                                                                                        .mnemonicVisibleForId ==
                                                                                        wordPair.id,
                                                                        onClick = {
                                                                                soundManager
                                                                                        .playClickSound()
                                                                                viewModel
                                                                                        .selectSpanishWord(
                                                                                                wordPair
                                                                                        )
                                                                        },
                                                                        onLongPress = {
                                                                                viewModel
                                                                                        .showMnemonic(
                                                                                                wordPair.id
                                                                                        )
                                                                        }
                                                                )
                                                        }
                                                }
                                        }

                                        // Right column (English or Spanish depending on swap state)
                                        Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                if (gameState.isSwapped) {
                                                        // Show Spanish words on right when swapped
                                                        gameState.spanishWords.forEach { wordPair ->
                                                                SpanishWordCard(
                                                                        wordPair = wordPair,
                                                                        isSelected =
                                                                                gameState
                                                                                        .selectedSpanish
                                                                                        ?.id ==
                                                                                        wordPair.id,
                                                                        isMatched =
                                                                                gameState
                                                                                        .matchedPairs
                                                                                        .contains(
                                                                                                wordPair.id
                                                                                        ),
                                                                        showMnemonic =
                                                                                gameState
                                                                                        .mnemonicVisibleForId ==
                                                                                        wordPair.id,
                                                                        onClick = {
                                                                                soundManager
                                                                                        .playClickSound()
                                                                                viewModel
                                                                                        .selectSpanishWord(
                                                                                                wordPair
                                                                                        )
                                                                        },
                                                                        onLongPress = {
                                                                                viewModel
                                                                                        .showMnemonic(
                                                                                                wordPair.id
                                                                                        )
                                                                        }
                                                                )
                                                        }
                                                } else {
                                                        // Show English words on right when not
                                                        // swapped
                                                        gameState.englishWords.forEach { wordPair ->
                                                                WordCard(
                                                                        word = wordPair.english,
                                                                        isSelected =
                                                                                gameState
                                                                                        .selectedEnglish
                                                                                        ?.id ==
                                                                                        wordPair.id,
                                                                        isMatched =
                                                                                gameState
                                                                                        .matchedPairs
                                                                                        .contains(
                                                                                                wordPair.id
                                                                                        ),
                                                                        onClick = {
                                                                                soundManager
                                                                                        .playClickSound()
                                                                                viewModel
                                                                                        .selectEnglishWord(
                                                                                                wordPair
                                                                                        )
                                                                        }
                                                                )
                                                        }
                                                }
                                        }
                                }

                                // Instruction text
                                Text(
                                        text =
                                                "💡 Tip: Press and hold a Spanish word to see its mnemonic",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color =
                                                MaterialTheme.colorScheme.onBackground.copy(
                                                        alpha = 0.7f
                                                ),
                                        textAlign = TextAlign.Center,
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(top = 12.dp, bottom = 8.dp)
                                )

                                // Exit and Reset buttons centered under the words
                                Row(
                                        modifier =
                                                Modifier.align(Alignment.CenterHorizontally)
                                                        .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                        Button(
                                                onClick = { viewModel.resetSession() },
                                                modifier = Modifier.width(120.dp).height(48.dp),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        if (MaterialTheme
                                                                                        .colorScheme
                                                                                        .background ==
                                                                                        DarkBackground
                                                                        )
                                                                                DarkSuccess
                                                                        else LightSuccess
                                                        ),
                                                shape = RoundedCornerShape(24.dp)
                                        ) {
                                                Text(
                                                        text = "Reset",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color.White
                                                )
                                        }

                                        Button(
                                                onClick = { (context as? Activity)?.finish() },
                                                modifier = Modifier.width(120.dp).height(48.dp),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        ),
                                                shape = RoundedCornerShape(24.dp)
                                        ) {
                                                Text(
                                                        text = "Exit",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color.White
                                                )
                                        }
                                }
                        }
                }
        }
}

@Composable
fun SpanishWordCard(
        wordPair: WordPair,
        isSelected: Boolean,
        isMatched: Boolean,
        showMnemonic: Boolean,
        onClick: () -> Unit,
        onLongPress: () -> Unit
) {
        val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground

        val backgroundColor by
                animateColorAsState(
                        targetValue =
                                when {
                                        isMatched -> if (isDarkTheme) DarkSuccess else LightSuccess
                                        isSelected -> if (isDarkTheme) DarkPrimary else LightPrimary
                                        else -> MaterialTheme.colorScheme.surface
                                },
                        animationSpec = tween(durationMillis = 300),
                        label = "backgroundColor"
                )

        val textColor by
                animateColorAsState(
                        targetValue =
                                if (isMatched || isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurface,
                        animationSpec = tween(durationMillis = 300),
                        label = "textColor"
                )

        val borderColor =
                when {
                        isMatched -> if (isDarkTheme) DarkSuccess else LightSuccess
                        isSelected -> if (isDarkTheme) DarkPrimary else LightPrimary
                        else -> if (isDarkTheme) DarkBorder else LightBorder
                }

        Column {
                Card(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(56.dp)
                                        .pointerInput(wordPair.id) {
                                                detectTapGestures(
                                                        onTap = { if (!isMatched) onClick() },
                                                        onLongPress = {
                                                                if (!isMatched) onLongPress()
                                                        }
                                                )
                                        }
                                        .border(
                                                width = 2.dp,
                                                color = borderColor,
                                                shape = RoundedCornerShape(12.dp)
                                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        elevation =
                                CardDefaults.cardElevation(
                                        defaultElevation =
                                                if (isSelected || isMatched) 4.dp else 2.dp
                                )
                ) {
                        Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                        ) {
                                Text(
                                        text = wordPair.spanish,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                )
                        }
                }

                // Mnemonic display
                if (showMnemonic && !isMatched) {
                        Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        if (isDarkTheme) Color(0xFF3A3A3A)
                                                        else Color(0xFFFFF8E1)
                                        ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                                Text(
                                        text = wordPair.mnemonic,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        color =
                                                if (isDarkTheme) Color(0xFFFFCC80)
                                                else Color(0xFF5D4037),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(8.dp)
                                )
                        }
                }
        }
}

@Composable
fun WordCard(word: String, isSelected: Boolean, isMatched: Boolean, onClick: () -> Unit) {
        val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground

        val backgroundColor by
                animateColorAsState(
                        targetValue =
                                when {
                                        isMatched -> if (isDarkTheme) DarkSuccess else LightSuccess
                                        isSelected -> if (isDarkTheme) DarkPrimary else LightPrimary
                                        else -> MaterialTheme.colorScheme.surface
                                },
                        animationSpec = tween(durationMillis = 300),
                        label = "backgroundColor"
                )

        val textColor by
                animateColorAsState(
                        targetValue =
                                if (isMatched || isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurface,
                        animationSpec = tween(durationMillis = 300),
                        label = "textColor"
                )

        val borderColor =
                when {
                        isMatched -> if (isDarkTheme) DarkSuccess else LightSuccess
                        isSelected -> if (isDarkTheme) DarkPrimary else LightPrimary
                        else -> if (isDarkTheme) DarkBorder else LightBorder
                }

        Card(
                modifier =
                        Modifier.fillMaxWidth()
                                .height(56.dp)
                                .clickable(enabled = !isMatched) { onClick() }
                                .border(
                                        width = 2.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(12.dp)
                                ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation =
                        CardDefaults.cardElevation(
                                defaultElevation = if (isSelected || isMatched) 4.dp else 2.dp
                        )
        ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                                text = word,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                        )
                }
        }
}

fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "${minutes}:${String.format("%02d", remainingSeconds)}"
}
