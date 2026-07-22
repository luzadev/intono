package com.notemusicali.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.notemusicali.audio.InstrumentPreset
import com.notemusicali.challenge.ChallengeTimeLimit
import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteSequence
import com.notemusicali.ui.challenge.ChallengeScreen
import com.notemusicali.ui.challenge.ChallengeSetupScreen
import com.notemusicali.ui.challenge.ChallengeViewModel
import com.notemusicali.ui.challenge.LeaderboardScreen
import com.notemusicali.ui.eartraining.EarTrainingScreen
import com.notemusicali.ui.exercises.ExercisesScreen
import com.notemusicali.ui.goals.DailyGoalScreen
import com.notemusicali.ui.guide.GuideScreen
import com.notemusicali.ui.history.HistoryScreen
import com.notemusicali.ui.home.HomeScreen
import com.notemusicali.ui.metronome.MetronomeScreen
import com.notemusicali.ui.practice.ManualInputScreen
import com.notemusicali.ui.practice.PracticeScreen
import com.notemusicali.ui.practice.PracticeSetupScreen
import com.notemusicali.ui.practice.PracticeViewModel
import com.notemusicali.ui.reference.ReferenceNoteScreen
import com.notemusicali.ui.scales.ScaleGeneratorScreen
import com.notemusicali.ui.scan.ScanScreen
import com.notemusicali.ui.scan.ScanViewModel
import com.notemusicali.ui.scores.ScoresScreen
import com.notemusicali.ui.stats.StatsScreen
import com.notemusicali.ui.tuner.TunerScreen

object Routes {
    const val HOME = "home"
    const val TUNER = "tuner"
    const val PRACTICE_SETUP = "practice_setup"
    const val MANUAL_INPUT = "manual_input"
    const val SCAN = "scan"
    const val PRACTICE = "practice"
    const val EXERCISES = "exercises"
    const val HISTORY = "history"
    const val SCORES = "scores"
    const val METRONOME = "metronome"
    const val REFERENCE_NOTE = "reference_note"
    const val SCALES = "scales"
    const val EAR_TRAINING = "ear_training"
    const val CHALLENGE_SETUP = "challenge_setup"
    const val CHALLENGE = "challenge"
    const val LEADERBOARD = "leaderboard"
    const val STATS = "stats"
    const val DAILY_GOALS = "daily_goals"
    const val GUIDE = "guide"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    var currentSequence by remember { mutableStateOf<NoteSequence?>(null) }
    var currentInstrument by remember { mutableStateOf(InstrumentPreset.VIOLINO) }
    var challengeTimeLimit by remember { mutableStateOf(ChallengeTimeLimit.MEDIUM) }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToTuner = { navController.navigate(Routes.TUNER) },
                onNavigateToPractice = { navController.navigate(Routes.PRACTICE_SETUP) },
                onNavigateToExercises = { navController.navigate(Routes.EXERCISES) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToScores = { navController.navigate(Routes.SCORES) },
                onNavigateToMetronome = { navController.navigate(Routes.METRONOME) },
                onNavigateToReference = { navController.navigate(Routes.REFERENCE_NOTE) },
                onNavigateToScales = { navController.navigate(Routes.SCALES) },
                onNavigateToEarTraining = { navController.navigate(Routes.EAR_TRAINING) },
                onNavigateToChallenge = { navController.navigate(Routes.CHALLENGE_SETUP) },
                onNavigateToLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                onNavigateToStats = { navController.navigate(Routes.STATS) },
                onNavigateToDailyGoals = { navController.navigate(Routes.DAILY_GOALS) },
                onNavigateToGuide = { navController.navigate(Routes.GUIDE) },
            )
        }

        composable(Routes.TUNER) {
            TunerScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.METRONOME) {
            MetronomeScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.REFERENCE_NOTE) {
            ReferenceNoteScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SCALES) {
            ScaleGeneratorScreen(
                onBack = { navController.popBackStack() },
                onPractice = { sequence ->
                    currentSequence = sequence
                    currentInstrument = InstrumentPreset.VIOLINO
                    navController.navigate(Routes.PRACTICE) {
                        popUpTo(Routes.SCALES)
                    }
                },
            )
        }

        composable(Routes.EAR_TRAINING) {
            EarTrainingScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.CHALLENGE_SETUP) {
            ChallengeSetupScreen(
                onBack = { navController.popBackStack() },
                onStart = { sequence, timeLimit ->
                    currentSequence = sequence
                    challengeTimeLimit = timeLimit
                    navController.navigate(Routes.CHALLENGE) {
                        popUpTo(Routes.CHALLENGE_SETUP)
                    }
                },
            )
        }

        composable(Routes.CHALLENGE) {
            val challengeViewModel: ChallengeViewModel = viewModel { ChallengeViewModel() }
            LaunchedEffect(currentSequence, challengeTimeLimit) {
                currentSequence?.let { seq ->
                    challengeViewModel.setSequence(seq)
                    challengeViewModel.setTimeLimit(challengeTimeLimit)
                    challengeViewModel.start()
                }
            }
            ChallengeScreen(
                onBack = { navController.popBackStack(Routes.HOME, inclusive = false) },
                viewModel = challengeViewModel,
            )
        }

        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.DAILY_GOALS) {
            DailyGoalScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.GUIDE) {
            GuideScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PRACTICE_SETUP) {
            PracticeSetupScreen(
                onBack = { navController.popBackStack() },
                onManualInput = { navController.navigate(Routes.MANUAL_INPUT) },
                onScan = { navController.navigate(Routes.SCAN) },
                onMusicXmlLoaded = { sequence ->
                    currentSequence = sequence
                    navController.navigate(Routes.PRACTICE)
                },
            )
        }

        composable(Routes.MANUAL_INPUT) {
            ManualInputScreen(
                onBack = { navController.popBackStack() },
                onSequenceReady = { sequence, instrument ->
                    currentSequence = sequence
                    currentInstrument = instrument
                    navController.navigate(Routes.PRACTICE)
                },
            )
        }

        composable(Routes.SCAN) {
            val scanViewModel: ScanViewModel = viewModel { ScanViewModel() }
            ScanScreen(
                onBack = { navController.popBackStack() },
                onSequenceCaptured = { sequence, instrument ->
                    currentSequence = sequence
                    currentInstrument = instrument
                    navController.navigate(Routes.PRACTICE) {
                        popUpTo(Routes.PRACTICE_SETUP)
                    }
                },
                viewModel = scanViewModel,
            )
        }

        composable(Routes.EXERCISES) {
            ExercisesScreen(
                onBack = { navController.popBackStack() },
                onExerciseSelected = { sequence ->
                    currentSequence = sequence
                    currentInstrument = InstrumentPreset.VIOLINO
                    navController.navigate(Routes.PRACTICE) {
                        popUpTo(Routes.EXERCISES)
                    }
                },
            )
        }

        composable(Routes.SCORES) {
            ScoresScreen(
                onBack = { navController.popBackStack() },
                onScoreSelected = { sequence ->
                    currentSequence = sequence
                    currentInstrument = InstrumentPreset.PIANOFORTE
                    navController.navigate(Routes.PRACTICE) {
                        popUpTo(Routes.SCORES)
                    }
                },
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onReplay = { session ->
                    val notes = session.noteMidiNumbers.map { MusicalNote.fromMidi(it) }
                    currentSequence = NoteSequence(
                        name = session.sequenceName,
                        notes = notes,
                        beats = session.beats,
                        beatType = session.beatType,
                    )
                    currentInstrument = InstrumentPreset.entries.firstOrNull {
                        it.displayName == session.instrumentName
                    } ?: InstrumentPreset.VIOLINO
                    navController.navigate(Routes.PRACTICE) {
                        popUpTo(Routes.HISTORY)
                    }
                },
            )
        }

        composable(Routes.PRACTICE) {
            val practiceViewModel: PracticeViewModel = viewModel { PracticeViewModel() }
            LaunchedEffect(currentSequence, currentInstrument) {
                practiceViewModel.selectInstrument(currentInstrument)
                currentSequence?.let { seq ->
                    practiceViewModel.setSequence(seq)
                }
                practiceViewModel.startListening()
            }
            PracticeScreen(
                onBack = { navController.popBackStack() },
                viewModel = practiceViewModel,
                onFinished = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
            )
        }
    }
}
