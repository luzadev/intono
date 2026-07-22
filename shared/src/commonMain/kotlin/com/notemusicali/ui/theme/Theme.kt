package com.notemusicali.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Remotion video-inspired palette
object AppColors {
    val bgDark = Color(0xFF0A0118)
    val bgPurple = Color(0xFF1A0533)
    val bgIndigo = Color(0xFF2D1B69)
    val accent = Color(0xFFFFD700)       // Gold
    val accentWarm = Color(0xFFFFA500)   // Orange-gold
    val accentRose = Color(0xFFFF6B9D)   // Rose
    val text = Color.White
    val textMuted = Color.White.copy(alpha = 0.6f)
    val cardBg = Color.White.copy(alpha = 0.06f)
    val cardBorder = Color.White.copy(alpha = 0.12f)
    val noteGlow = Color(0x66FFD700)     // Gold glow
}

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.accent,
    secondary = AppColors.accentRose,
    tertiary = Color(0xFF4ECDC4),
    background = AppColors.bgDark,
    surface = AppColors.bgPurple,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = AppColors.bgIndigo,
)

object CardGradients {
    // Home cards — glass-like with subtle colored tints on dark purple base
    val tuner = Brush.linearGradient(listOf(Color(0xFF1A1040), Color(0xFF2D1B69)))
    val practice = Brush.linearGradient(listOf(Color(0xFF0D1F2D), Color(0xFF1A0533)))
    val exercises = Brush.linearGradient(listOf(Color(0xFF2A1020), Color(0xFF1A0533)))
    val history = Brush.linearGradient(listOf(Color(0xFF1A0533), Color(0xFF0A0118)))
    val scores = Brush.linearGradient(listOf(Color(0xFF0D1A2D), Color(0xFF1A0533)))
    val scoreFile = Brush.linearGradient(listOf(Color(0xFF151025), Color(0xFF0A0118)))

    val metronome = Brush.linearGradient(listOf(Color(0xFF1E1035), Color(0xFF0A0118)))
    val reference = Brush.linearGradient(listOf(Color(0xFF0D1F33), Color(0xFF1A0533)))
    val scales = Brush.linearGradient(listOf(Color(0xFF101E15), Color(0xFF1A0533)))
    val earTraining = Brush.linearGradient(listOf(Color(0xFF2D1050), Color(0xFF1A0533)))
    val challenge = Brush.linearGradient(listOf(Color(0xFF2A0D1A), Color(0xFF1A0533)))
    val stats = Brush.linearGradient(listOf(Color(0xFF0D1533), Color(0xFF1A0533)))
    val goals = Brush.linearGradient(listOf(Color(0xFF2A1A05), Color(0xFF1A0533)))
    val leaderboard = Brush.linearGradient(listOf(Color(0xFF151040), Color(0xFF1A0533)))
    val guide = Brush.linearGradient(listOf(Color(0xFF151525), Color(0xFF0A0118)))

    // Practice setup
    val setup = Brush.linearGradient(listOf(Color(0xFF0D1F2D), Color(0xFF1A0533)))

    // Exercise levels — subtle colored tints
    val level1 = Brush.linearGradient(listOf(Color(0xFF0D2015), Color(0xFF1A0533)))
    val level2 = Brush.linearGradient(listOf(Color(0xFF0D1533), Color(0xFF1A0533)))
    val level3 = Brush.linearGradient(listOf(Color(0xFF2A1A05), Color(0xFF1A0533)))
    val level4 = Brush.linearGradient(listOf(Color(0xFF2A0D15), Color(0xFF1A0533)))

    fun forLevel(level: Int): Brush = when (level) {
        1 -> level1
        2 -> level2
        3 -> level3
        else -> level4
    }
}

// Accent colors for each card category (used for top accent lines and icon tints)
object CardAccents {
    val tuner = Color(0xFF4ECDC4)
    val metronome = Color(0xFFFFD700)
    val reference = Color(0xFF90CAF9)
    val practice = Color(0xFF4CAF50)
    val exercises = Color(0xFFFF6B9D)
    val scales = Color(0xFF66BB6A)
    val scores = Color(0xFF4DD0E1)
    val earTraining = Color(0xFFCE93D8)
    val challenge = Color(0xFFEF5350)
    val leaderboard = Color(0xFF7986CB)
    val stats = Color(0xFF42A5F5)
    val goals = Color(0xFFFFA726)
    val history = Color(0xFFAB47BC)
    val guide = Color(0xFF78909C)
}

object TunerColors {
    val inTune = Color(0xFF4CAF50)
    val outOfTune = Color(0xFFF44336)
    val silence = Color(0xFF424242)
    val inTuneBackground = Color(0xFF0D200D)
    val outOfTuneBackground = Color(0xFF2A0D0D)
    val silenceBackground = AppColors.bgDark
}

@Composable
fun NoteMusicaliTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}
