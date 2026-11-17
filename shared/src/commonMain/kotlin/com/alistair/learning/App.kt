package com.alistair.learning

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AlistairApp(
    profileStore: ChildProfileStore,
    audioSettingsStore: AudioSettingsStore,
    learningProgressStore: LearningProgressStore,
    onSoundEffect: (SoundEffect) -> Unit = {},
    onBackgroundMusicChanged: (Boolean) -> Unit = {},
    showAudioSettings: Boolean = false
) {
    val berry = Color(0xFF7253B7)
    val coral = Color(0xFFFF8A70)
    val mint = Color(0xFF62C9A5)
    val cream = Color(0xFFFFF9F0)
    var profile by remember(profileStore) { mutableStateOf(profileStore.load()) }
    var audioSettings by remember(audioSettingsStore) {
        mutableStateOf(audioSettingsStore.loadAudioSettings())
    }
    var learningProgress by remember(profile, learningProgressStore) {
        mutableStateOf(profile?.let(learningProgressStore::loadProgress) ?: LearningProgress())
    }
    var editingProfile by remember(profileStore) { mutableStateOf(profile == null) }
    var activeModule by remember { mutableStateOf<LearningModule?>(null) }
    var showingSettings by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var audioSaveError by remember { mutableStateOf<String?>(null) }
    var progressSaveError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(audioSettings.backgroundMusicEnabled) {
        onBackgroundMusicChanged(audioSettings.backgroundMusicEnabled)
    }

    val playSoundEffect: (SoundEffect) -> Unit = { effect ->
        if (audioSettings.soundEffectsEnabled) onSoundEffect(effect)
    }
    val updateAudioSettings: (AudioSettings) -> Unit = { updatedSettings ->
        if (audioSettingsStore.saveAudioSettings(updatedSettings)) {
            audioSettings = updatedSettings
            audioSaveError = null
        } else {
            audioSaveError = "Couldn't save the audio settings. Please try again."
        }
    }
    val saveCompletedStage: (LearningModule, Int) -> Unit = { module, stage ->
        val currentProfile = profile
        if (currentProfile == null) {
            progressSaveError = "Couldn't save progress because no profile is selected."
        } else {
            val updatedProgress = learningProgress.completeStage(module, stage)
            if (learningProgressStore.saveProgress(currentProfile, updatedProgress)) {
                learningProgress = updatedProgress
                progressSaveError = null
            } else {
                progressSaveError = "Couldn't save game progress. Please try again."
            }
        }
    }

    val saveProfile: (String, Int) -> Unit = { name, age ->
        val updatedProfile = ChildProfile(name.trim(), age)
        if (profileStore.save(updatedProfile)) {
            profile = updatedProfile
            editingProfile = false
            saveError = null
        } else {
            saveError = "Couldn't save the profile. Please try again."
        }
    }

    val screen = when {
        profile == null || editingProfile -> AppScreen.PROFILE
        activeModule != null -> AppScreen.ACTIVITY
        showingSettings -> AppScreen.SETTINGS
        else -> AppScreen.HOME
    }

    MaterialTheme {
        Surface(color = cream, modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInHorizontally { it / 12 }) togetherWith
                        (fadeOut(tween(140)) + slideOutHorizontally { -it / 12 })
                },
                label = "app-screen"
            ) { targetScreen ->
                when (targetScreen) {
                    AppScreen.PROFILE -> ProfileForm(
                        profile = profile,
                        saveError = saveError,
                        onSave = saveProfile,
                        onCancel = {
                            playSoundEffect(SoundEffect.TAP)
                            editingProfile = false
                            saveError = null
                        },
                        onSoundEffect = playSoundEffect,
                        berry = berry,
                        coral = coral,
                        mint = mint
                    )
                    AppScreen.ACTIVITY -> LearningActivityScreen(
                        module = activeModule ?: LearningModule.GAMES,
                        age = profile?.age ?: 5,
                        berry = berry,
                        onBack = { activeModule = null },
                        onSoundEffect = playSoundEffect,
                        progress = learningProgress,
                        onStageCompleted = saveCompletedStage
                    )
                    AppScreen.HOME -> LearningHome(
                        profile = requireNotNull(profile),
                        berry = berry,
                        coral = coral,
                        mint = mint,
                        progressSaveError = progressSaveError,
                        completedStageCount = learningProgress.totalCompletedStages,
                        onEditProfile = {
                            playSoundEffect(SoundEffect.TAP)
                            saveError = null
                            editingProfile = true
                        },
                        onOpenSettings = {
                            playSoundEffect(SoundEffect.TAP)
                            showingSettings = true
                        },
                        onOpenModule = {
                            activeModule = it
                        },
                        onSoundEffect = playSoundEffect
                    )
                    AppScreen.SETTINGS -> SettingsScreen(
                        audioSettings = audioSettings,
                        audioSaveError = audioSaveError,
                        showAudioSettings = showAudioSettings,
                        onAudioSettingsChange = updateAudioSettings,
                        onBack = {
                            playSoundEffect(SoundEffect.TAP)
                            showingSettings = false
                        },
                        onSoundEffect = playSoundEffect
                    )
                }
            }
        }
    }
}

private enum class AppScreen {
    PROFILE,
    HOME,
    ACTIVITY,
    SETTINGS
}

@Composable
private fun ProfileForm(
    profile: ChildProfile?,
    saveError: String?,
    onSave: (String, Int) -> Unit,
    onCancel: () -> Unit,
    onSoundEffect: (SoundEffect) -> Unit,
    berry: Color,
    coral: Color,
    mint: Color
) {
    var childName by remember(profile) { mutableStateOf(profile?.displayName.orEmpty()) }
    var age by remember(profile) { mutableIntStateOf(profile?.age ?: 5) }

    Box(modifier = Modifier.fillMaxSize()) {
        WelcomeSkyBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(32.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🌈  ⭐  🦋", fontSize = 42.sp)
            Text(
                if (profile == null) "Welcome, little learner!" else "Update your profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = berry
            )
            Text("Alistair’s Educational Learning", color = berry)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = childName,
                onValueChange = { updatedName ->
                    val addedCharacter = updatedName.length > childName.length
                    childName = updatedName
                    if (addedCharacter) onSoundEffect(SoundEffect.TYPE)
                },
                label = { Text("What's your name?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(
                visible = childName.isNotBlank(),
                enter = fadeIn(tween(220)) + expandVertically(),
                exit = fadeOut(tween(160)) + shrinkVertically()
            ) {
                val badgeScale by animateFloatAsState(
                    targetValue = if (childName.length % 2 == 1) 1.12f else 1f,
                    animationSpec = tween(160),
                    label = "name-entry-bounce"
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .scale(badgeScale)
                            .background(
                                Brush.linearGradient(listOf(coral, berry)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = childName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                            transitionSpec = {
                                fadeIn(tween(140)) togetherWith fadeOut(tween(100))
                            },
                            label = "name-avatar-letter"
                        ) { initial ->
                            Text(
                                initial,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            )
                        }
                    }
                    Spacer(Modifier.size(10.dp))
                    AnimatedContent(
                        targetState = childName.trim(),
                        transitionSpec = {
                            fadeIn(tween(150)) togetherWith fadeOut(tween(100))
                        },
                        label = "name-welcome-message"
                    ) { name ->
                        Text(
                            "Hi, $name! Ready to play?",
                            color = berry,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("How old are you?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (3..10).toList().chunked(4).forEach { ages ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ages.forEach { value ->
                            FilterChip(
                                selected = age == value,
                                onClick = {
                                    onSoundEffect(SoundEffect.TAP)
                                    age = value
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = listOf(berry, coral, mint)[value % 3],
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFFFF0C2)
                                ),
                                label = { Text("$value") }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSoundEffect(SoundEffect.TAP)
                    onSave(childName, age)
                },
                enabled = childName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = berry),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text(
                    if (profile == null) "Let's learn and play!" else "Save profile",
                    fontWeight = FontWeight.Bold
                )
            }
            if (saveError != null) {
                Text(saveError, color = MaterialTheme.colorScheme.error)
            }
            if (profile != null) {
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
            Text(
                "No account needed • Designed for offline learning",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun WelcomeSkyBackground() {
    val skyMotion = rememberInfiniteTransition(label = "welcome-sky")
    val planeProgress by skyMotion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "plane-flight"
    )
    val birdProgress by skyMotion.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bird-flight"
    )
    val fishProgress by skyMotion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fish-swim"
    )
    val firstStarAlpha by skyMotion.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "first-star-blink"
    )
    val secondStarAlpha by skyMotion.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, delayMillis = 350),
            repeatMode = RepeatMode.Reverse
        ),
        label = "second-star-blink"
    )

    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF69C5F2), Color(0xFFBDEBFF), Color(0xFFFFE6B7))
            ),
            size = size
        )
        val unit = (size.width / 400f).coerceIn(0.65f, 1.25f)

        drawCircle(
            color = Color(0xFFFFF2A6),
            radius = 30f * unit,
            center = Offset(size.width * 0.83f, size.height * 0.14f)
        )

        val starPoints = listOf(
            Offset(0.09f, 0.12f), Offset(0.24f, 0.24f), Offset(0.78f, 0.08f),
            Offset(0.91f, 0.30f), Offset(0.64f, 0.36f), Offset(0.16f, 0.42f)
        )
        starPoints.forEachIndexed { index, point ->
            val alpha = when (index % 2) {
                0 -> firstStarAlpha
                else -> secondStarAlpha
            }
            val x = size.width * point.x
            val y = size.height * point.y
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(x - 5f * unit, y),
                end = Offset(x + 5f * unit, y),
                strokeWidth = 2f * unit,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(x, y - 5f * unit),
                end = Offset(x, y + 5f * unit),
                strokeWidth = 2f * unit,
                cap = StrokeCap.Round
            )
        }

        val cloudColor = Color.White.copy(alpha = 0.8f)
        listOf(0.16f to 0.20f, 0.54f to 0.12f, 0.90f to 0.43f).forEach { (x, y) ->
            val center = Offset(size.width * x, size.height * y)
            drawCircle(cloudColor, 20f * unit, center + Offset(-18f * unit, 5f * unit))
            drawCircle(cloudColor, 27f * unit, center)
            drawCircle(cloudColor, 18f * unit, center + Offset(23f * unit, 7f * unit))
            drawRoundRect(
                cloudColor,
                topLeft = center + Offset(-34f * unit, 5f * unit),
                size = Size(69f * unit, 19f * unit),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * unit)
            )
        }

        val planeX = planeProgress * (size.width + 170f * unit) - 155f * unit
        val planeY = size.height * 0.28f + sin(planeProgress * 2f * PI).toFloat() * 14f * unit
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(planeX - 35f * unit, planeY + 18f * unit),
            end = Offset(planeX - 5f * unit, planeY + 18f * unit),
            strokeWidth = 2f * unit,
            cap = StrokeCap.Round
        )
        withTransform({
            translate(left = planeX, top = planeY)
            scale(unit, unit, pivot = Offset.Zero)
            rotate(degrees = -4f, pivot = Offset(48f, 20f))
        }) {
            val fuselage = Path().apply {
                moveTo(4f, 19f)
                quadraticTo(24f, 11f, 51f, 15f)
                lineTo(88f, 17f)
                quadraticTo(96f, 18f, 88f, 22f)
                lineTo(51f, 25f)
                quadraticTo(24f, 29f, 4f, 21f)
                close()
            }
            drawPath(fuselage, Color(0xFFF9FCFF))
            val wing = Path().apply {
                moveTo(40f, 18f)
                lineTo(58f, 1f)
                lineTo(68f, 2f)
                lineTo(59f, 19f)
                lineTo(67f, 30f)
                lineTo(58f, 31f)
                close()
            }
            drawPath(wing, Color(0xFF2C83C5))
            val tail = Path().apply {
                moveTo(16f, 18f)
                lineTo(7f, 7f)
                lineTo(17f, 9f)
                lineTo(28f, 17f)
                close()
            }
            drawPath(tail, Color(0xFF2C83C5))
            drawRect(Color(0xFFFF805C), Offset(30f, 15f), Size(14f, 9f))
            drawCircle(Color(0xFF397EAC), 2f, Offset(75f, 19f))
            drawLine(
                Color(0xFF4F5962),
                Offset(94f, 11f),
                Offset(94f, 27f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        val birdX = (1f - birdProgress) * (size.width + 90f * unit) - 45f * unit
        val birdY = size.height * 0.38f
        repeat(3) { index ->
            val x = birdX - index * 18f * unit
            val y = birdY + sin((planeProgress * 2f * PI) + index).toFloat() * 5f * unit
            drawLine(Color(0xFF556575), Offset(x - 7f * unit, y + 3f * unit), Offset(x, y), 2f * unit)
            drawLine(Color(0xFF556575), Offset(x, y), Offset(x + 7f * unit, y + 3f * unit), 2f * unit)
        }

        val groundY = size.height - 55f * unit
        drawOval(
            color = Color(0xFF62C98B),
            topLeft = Offset(-size.width * 0.12f, groundY - 38f * unit),
            size = Size(size.width * 0.8f, 150f * unit)
        )
        drawOval(
            color = Color(0xFF4CB77A),
            topLeft = Offset(size.width * 0.48f, groundY - 20f * unit),
            size = Size(size.width * 0.74f, 130f * unit)
        )
        drawOval(
            color = Color(0xFF57BFE7),
            topLeft = Offset(size.width * 0.66f, size.height - 104f * unit),
            size = Size(110f * unit, 43f * unit)
        )
        val fishCenter = Offset(
            x = size.width * 0.72f + fishProgress * 30f * unit,
            y = size.height - 84f * unit
        )
        drawOval(
            Color(0xFFFFD166),
            Offset(fishCenter.x - 13f * unit, fishCenter.y - 7f * unit),
            Size(27f * unit, 14f * unit)
        )
        val fishTail = Path().apply {
            moveTo(fishCenter.x - 10f * unit, fishCenter.y)
            lineTo(fishCenter.x - 21f * unit, fishCenter.y - 8f * unit)
            lineTo(fishCenter.x - 21f * unit, fishCenter.y + 8f * unit)
            close()
        }
        drawPath(fishTail, Color(0xFFFF8A70))
        drawCircle(Color(0xFF37474F), 1.5f * unit, fishCenter + Offset(7f * unit, -1f * unit))

        val dinoX = 44f * unit
        val dinoY = size.height - 99f * unit
        drawRoundRect(
            Color(0xFF4D9A54),
            Offset(dinoX, dinoY + 19f * unit),
            Size(74f * unit, 33f * unit),
            androidx.compose.ui.geometry.CornerRadius(17f * unit)
        )
        drawRoundRect(
            Color(0xFF4D9A54),
            Offset(dinoX + 48f * unit, dinoY - 2f * unit),
            Size(25f * unit, 38f * unit),
            androidx.compose.ui.geometry.CornerRadius(12f * unit)
        )
        drawCircle(Color(0xFF63B964), 18f * unit, Offset(dinoX + 76f * unit, dinoY + 4f * unit))
        val dinoTail = Path().apply {
            moveTo(dinoX + 4f * unit, dinoY + 30f * unit)
            lineTo(dinoX - 22f * unit, dinoY + 16f * unit)
            lineTo(dinoX - 4f * unit, dinoY + 43f * unit)
            close()
        }
        drawPath(dinoTail, Color(0xFF4D9A54))
        repeat(3) { index ->
            val legX = dinoX + (16f + index * 24f) * unit
            drawRoundRect(
                Color(0xFF3D8247),
                Offset(legX, dinoY + 43f * unit),
                Size(10f * unit, 16f * unit),
                androidx.compose.ui.geometry.CornerRadius(5f * unit)
            )
        }
        repeat(4) { index ->
            val spike = Path().apply {
                val x = dinoX + (28f + index * 14f) * unit
                moveTo(x, dinoY + 23f * unit)
                lineTo(x + 6f * unit, dinoY + 10f * unit)
                lineTo(x + 12f * unit, dinoY + 23f * unit)
                close()
            }
            drawPath(spike, Color(0xFFFFC857))
        }
        drawCircle(Color.White, 4f * unit, Offset(dinoX + 82f * unit, dinoY))
        drawCircle(Color(0xFF263238), 1.8f * unit, Offset(dinoX + 83f * unit, dinoY))
    }
}

@Composable
private fun LearningHome(
    profile: ChildProfile,
    berry: Color,
    coral: Color,
    mint: Color,
    progressSaveError: String?,
    completedStageCount: Int,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenModule: (LearningModule) -> Unit,
    onSoundEffect: (SoundEffect) -> Unit
) {
    val subjects = listOf(
        LearningModule.GAMES to ("Picture Match" to "Find matching pairs"),
        LearningModule.MATHS to ("Maths" to "Solve number challenges"),
        LearningModule.GENERAL_KNOWLEDGE to ("Explore & Learn" to "Discover fun facts"),
        LearningModule.DRAWING to ("Drawing" to "Draw with your finger"),
        LearningModule.COLOURS to ("Colour Match" to "Find the matching colours"),
        LearningModule.BUDDY to ("Learning Buddy" to "Explore a new offline fun fact"),
        LearningModule.WORD_GAMES to ("Word Games" to "Unscramble and spell words"),
        LearningModule.NUMBER_PUZZLES to ("Number Puzzles" to "Find the missing number"),
        LearningModule.MAZE to ("Star Maze" to "Find your way to the star"),
        LearningModule.PATTERNS to ("Patterns & Shapes" to "Choose what comes next"),
        LearningModule.COLORING to ("Coloring Studio" to "Color original picture scenes"),
        LearningModule.CAR_DRIVING to ("Car Driving" to "Steer around the road obstacles")
    )
    val selectionScope = rememberCoroutineScope()
    var selectedModule by remember { mutableStateOf<LearningModule?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Hello, ${profile.displayName}! 👋",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = berry
                )
                Text(
                    "Your learning adventure • Age ${profile.age}",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onOpenSettings) {
                Text("⚙️", fontSize = 28.sp)
            }
        }
        AdventureBanner(
            name = profile.displayName,
            berry = berry,
            modifier = Modifier.fillMaxWidth()
        )
        Text("$completedStageCount stages completed", color = MaterialTheme.colorScheme.secondary)
        TextButton(onClick = onEditProfile) { Text("Edit profile") }
        if (progressSaveError != null) {
            Text(progressSaveError, color = MaterialTheme.colorScheme.error)
        }
        subjects.forEachIndexed { index, (module, item) ->
            val color = listOf(berry, coral, mint)[index % 3]
            val isSelected = selectedModule == module
            val cardScale by animateFloatAsState(
                targetValue = if (isSelected) 1.04f else 1f,
                animationSpec = tween(150),
                label = "game-card-selection"
            )
            val cardColor by animateColorAsState(
                targetValue = color.copy(alpha = if (isSelected) 0.3f else 0.14f),
                animationSpec = tween(150),
                label = "game-card-color"
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(cardScale)
                    .animateContentSize()
                    .clickable(enabled = selectedModule == null) {
                        selectedModule = module
                        onSoundEffect(SoundEffect.SELECT)
                        selectionScope.launch {
                            delay(150)
                            onOpenModule(module)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ModuleArtwork(module = module, tint = color)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.first,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = berry
                        )
                        Text(item.second, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (isSelected) Text("✨", fontSize = 24.sp)
                }
            }
        }
        Text(
            "Choose any activity to start learning and playing.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun AdventureBanner(
    name: String,
    berry: Color,
    modifier: Modifier = Modifier
) {
    val motion = rememberInfiniteTransition(label = "home-adventure-art")
    val planeProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "home-plane"
    )
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD8F3FF))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(170.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val scaleX = size.width / 400f
                val scaleY = size.height / 180f
                withTransform({
                    scale(scaleX, scaleY, pivot = Offset.Zero)
                }) {
                    drawRect(
                        Brush.verticalGradient(
                            listOf(Color(0xFF72CFF1), Color(0xFFDAF5FF), Color(0xFFFFE6B7))
                        ),
                        size = Size(400f, 180f)
                    )
                    drawCircle(Color(0xFFFFD166), 22f, Offset(344f, 34f))
                    listOf(Offset(117f, 48f), Offset(274f, 79f)).forEach { cloud ->
                        val cloudFill = Color.White.copy(alpha = 0.88f)
                        drawCircle(cloudFill, 15f, cloud + Offset(-18f, 5f))
                        drawCircle(cloudFill, 21f, cloud)
                        drawCircle(cloudFill, 14f, cloud + Offset(20f, 7f))
                        drawRoundRect(
                            cloudFill,
                            cloud + Offset(-31f, 5f),
                            Size(62f, 18f),
                            androidx.compose.ui.geometry.CornerRadius(9f)
                        )
                    }

                    val planeX = 44f + planeProgress * 205f
                    val planeY = 62f + sin(planeProgress * 2f * PI).toFloat() * 8f
                    drawLine(
                        Color.White.copy(alpha = 0.7f),
                        Offset(planeX - 29f, planeY + 9f),
                        Offset(planeX - 4f, planeY + 9f),
                        2f,
                        StrokeCap.Round
                    )
                    val plane = Path().apply {
                        moveTo(planeX, planeY + 8f)
                        lineTo(planeX + 30f, planeY + 2f)
                        lineTo(planeX + 43f, planeY + 8f)
                        lineTo(planeX + 30f, planeY + 14f)
                        close()
                    }
                    drawPath(plane, Color(0xFF398BC4))
                    val wing = Path().apply {
                        moveTo(planeX + 15f, planeY + 7f)
                        lineTo(planeX + 25f, planeY - 5f)
                        lineTo(planeX + 31f, planeY + 8f)
                        close()
                    }
                    drawPath(wing, Color(0xFFFF805C))
                    drawLine(Color(0xFF263238), Offset(planeX + 44f, planeY + 2f), Offset(planeX + 44f, planeY + 14f), 2f)

                    listOf(0 to 0, 1 to 12, 2 to 25).forEach { (index, offset) ->
                        val birdX = 302f + offset + planeProgress * 20f
                        val birdY = 102f + index * 6f
                        drawLine(Color(0xFF4F5962), Offset(birdX - 6f, birdY + 3f), Offset(birdX, birdY), 2f)
                        drawLine(Color(0xFF4F5962), Offset(birdX, birdY), Offset(birdX + 6f, birdY + 3f), 2f)
                    }

                    val farHill = Path().apply {
                        moveTo(0f, 133f)
                        quadraticTo(93f, 100f, 181f, 133f)
                        quadraticTo(283f, 100f, 400f, 128f)
                        lineTo(400f, 180f)
                        lineTo(0f, 180f)
                        close()
                    }
                    drawPath(farHill, Color(0xFF9BD77E))
                    val nearHill = Path().apply {
                        moveTo(0f, 151f)
                        quadraticTo(124f, 123f, 231f, 152f)
                        quadraticTo(320f, 128f, 400f, 148f)
                        lineTo(400f, 180f)
                        lineTo(0f, 180f)
                        close()
                    }
                    drawPath(nearHill, Color(0xFF5EBE7E))
                    drawOval(Color(0xFF62BDE2), Offset(210f, 151f), Size(120f, 28f))

                    drawRoundRect(
                        Color(0xFF795B43),
                        Offset(350f, 119f),
                        Size(10f, 38f),
                        androidx.compose.ui.geometry.CornerRadius(4f)
                    )
                    listOf(Offset(355f, 106f), Offset(340f, 122f), Offset(370f, 122f)).forEach {
                        drawCircle(Color(0xFF3E995D), 18f, it)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .background(Color.White.copy(alpha = 0.86f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Let's explore, $name!",
                    color = berry,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Choose a game to begin.",
                    color = Color(0xFF36546B),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
internal fun ModuleArtwork(module: LearningModule, tint: Color, artSizeDp: Int = 66) {
    Canvas(Modifier.size(artSizeDp.dp)) {
        val unit = minOf(size.width, size.height) / 64f
        val ink = Color(0xFF455A64)
        withTransform({
            scale(unit, unit, pivot = Offset.Zero)
        }) {
            drawCircle(tint.copy(alpha = 0.16f), 29f, Offset(32f, 32f))
            when (module) {
                LearningModule.GAMES -> {
                    val colors = listOf(Color(0xFFFF8A70), Color(0xFFFFCA5C), Color(0xFF61C9A4), Color(0xFF65B9F2))
                    listOf(Offset(17f, 17f), Offset(35f, 17f), Offset(17f, 35f), Offset(35f, 35f))
                        .forEachIndexed { index, point ->
                            drawRoundRect(colors[index], point, Size(14f, 14f), androidx.compose.ui.geometry.CornerRadius(4f))
                            drawCircle(Color.White, 2.5f, point + Offset(7f, 7f))
                        }
                }
                LearningModule.MATHS, LearningModule.NUMBER_PUZZLES -> {
                    drawRoundRect(Color(0xFFFFC857), Offset(14f, 12f), Size(36f, 40f), androidx.compose.ui.geometry.CornerRadius(8f))
                    listOf(Offset(22f, 21f), Offset(38f, 21f), Offset(22f, 37f), Offset(38f, 37f))
                        .forEachIndexed { index, point ->
                            drawCircle(if (index == 3) Color(0xFFEF6C64) else Color.White, 5f, point)
                        }
                    drawLine(ink, Offset(16f, 30f), Offset(48f, 30f), 2f)
                }
                LearningModule.GENERAL_KNOWLEDGE -> {
                    drawCircle(Color(0xFF66B8E8), 22f, Offset(32f, 32f))
                    drawCircle(ink, 22f, Offset(32f, 32f), style = Stroke(2.5f))
                    drawOval(Color.Transparent, Offset(23f, 10f), Size(18f, 44f), style = Stroke(2f))
                    drawLine(ink, Offset(11f, 32f), Offset(53f, 32f), 2f)
                    drawArc(ink, 0f, 180f, false, Offset(12f, 21f), Size(40f, 22f), style = Stroke(2f))
                    drawArc(ink, 180f, 180f, false, Offset(12f, 21f), Size(40f, 22f), style = Stroke(2f))
                }
                LearningModule.DRAWING -> {
                    drawCircle(Color(0xFFFF8A70), 8f, Offset(22f, 30f))
                    drawCircle(Color(0xFFFFC857), 7f, Offset(35f, 23f))
                    drawCircle(Color(0xFF61C9A4), 7f, Offset(41f, 38f))
                    drawCircle(Color(0xFF65B9F2), 6f, Offset(24f, 44f))
                    val brush = Path().apply {
                        moveTo(36f, 49f)
                        lineTo(50f, 13f)
                        lineTo(54f, 16f)
                        lineTo(42f, 51f)
                        close()
                    }
                    drawPath(brush, Color(0xFF7253B7))
                }
                LearningModule.COLOURS -> {
                    val rainbow = listOf(Color(0xFFEF6C64), Color(0xFFFFC857), Color(0xFF61C9A4), Color(0xFF65B9F2))
                    rainbow.forEachIndexed { index, color ->
                        drawArc(
                            color,
                            190f,
                            160f,
                            false,
                            Offset(12f + index * 4f, 12f + index * 4f),
                            Size(40f - index * 8f, 40f - index * 8f),
                            style = Stroke(5f)
                        )
                    }
                }
                LearningModule.BUDDY -> {
                    drawCircle(Color(0xFFFFD166), 22f, Offset(32f, 31f))
                    drawCircle(ink, 22f, Offset(32f, 31f), style = Stroke(2f))
                    drawCircle(ink, 2f, Offset(25f, 28f))
                    drawCircle(ink, 2f, Offset(39f, 28f))
                    val smile = Path().apply {
                        moveTo(24f, 37f)
                        quadraticTo(32f, 45f, 40f, 37f)
                    }
                    drawPath(smile, ink, style = Stroke(2.5f, cap = StrokeCap.Round))
                    drawCircle(Color(0xFFFFC857), 5f, Offset(47f, 15f))
                }
                LearningModule.WORD_GAMES -> {
                    listOf(Color(0xFFFF8A70), Color(0xFF65B9F2), Color(0xFF61C9A4))
                        .forEachIndexed { index, color ->
                            val x = 10f + index * 16f
                            drawRoundRect(color, Offset(x, 21f), Size(14f, 23f), androidx.compose.ui.geometry.CornerRadius(4f))
                            drawCircle(Color.White, 2f, Offset(x + 7f, 32f))
                        }
                    drawLine(ink, Offset(13f, 48f), Offset(51f, 48f), 3f, StrokeCap.Round)
                }
                LearningModule.MAZE -> {
                    drawRoundRect(Color(0xFF7253B7), Offset(12f, 12f), Size(40f, 40f), androidx.compose.ui.geometry.CornerRadius(7f))
                    val path = Path().apply {
                        moveTo(20f, 20f)
                        lineTo(43f, 20f)
                        lineTo(43f, 28f)
                        lineTo(28f, 28f)
                        lineTo(28f, 43f)
                        lineTo(43f, 43f)
                    }
                    drawPath(path, Color.White, style = Stroke(4f, cap = StrokeCap.Round))
                    drawCircle(Color(0xFFFFD166), 4f, Offset(43f, 43f))
                }
                LearningModule.PATTERNS -> {
                    drawCircle(Color(0xFF65B9F2), 8f, Offset(19f, 22f))
                    drawRect(Color(0xFFFFC857), Offset(37f, 14f), Size(15f, 15f))
                    val triangle = Path().apply {
                        moveTo(20f, 50f)
                        lineTo(30f, 32f)
                        lineTo(40f, 50f)
                        close()
                    }
                    drawPath(triangle, Color(0xFFEF6C64))
                }
                LearningModule.COLORING -> {
                    drawOval(Color(0xFF61C9A4), Offset(15f, 28f), Size(31f, 18f))
                    drawCircle(Color(0xFF61C9A4), 10f, Offset(42f, 25f))
                    val tail = Path().apply {
                        moveTo(17f, 35f)
                        lineTo(8f, 28f)
                        lineTo(12f, 42f)
                        close()
                    }
                    drawPath(tail, Color(0xFF61C9A4))
                    repeat(3) { index ->
                        val x = 23f + index * 8f
                        val spike = Path().apply {
                            moveTo(x, 30f)
                            lineTo(x + 4f, 22f)
                            lineTo(x + 8f, 30f)
                            close()
                        }
                        drawPath(spike, Color(0xFFFFC857))
                    }
                    drawCircle(Color.White, 2f, Offset(45f, 23f))
                }
                LearningModule.CAR_DRIVING -> {
                    drawRoundRect(Color(0xFF546E7A), Offset(17f, 8f), Size(30f, 48f), androidx.compose.ui.geometry.CornerRadius(8f))
                    drawLine(Color.White, Offset(32f, 14f), Offset(32f, 22f), 3f, StrokeCap.Round)
                    drawLine(Color.White, Offset(32f, 42f), Offset(32f, 50f), 3f, StrokeCap.Round)
                    drawRoundRect(Color(0xFFEF6C64), Offset(21f, 25f), Size(22f, 17f), androidx.compose.ui.geometry.CornerRadius(5f))
                    drawRoundRect(Color(0xFFBFEAFF), Offset(25f, 27f), Size(14f, 6f), androidx.compose.ui.geometry.CornerRadius(3f))
                    drawCircle(ink, 2f, Offset(26f, 41f))
                    drawCircle(ink, 2f, Offset(38f, 41f))
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    audioSettings: AudioSettings,
    audioSaveError: String?,
    showAudioSettings: Boolean,
    onAudioSettingsChange: (AudioSettings) -> Unit,
    onBack: () -> Unit,
    onSoundEffect: (SoundEffect) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = {
                    onSoundEffect(SoundEffect.TAP)
                    onBack()
                }
            ) {
                Text("← Back")
            }
            Text(
                "Settings",
                modifier = Modifier.weight(1f),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7253B7)
            )
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF7253B7).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Audio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7253B7)
                )
                if (showAudioSettings) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sound effects", modifier = Modifier.weight(1f))
                        Switch(
                            checked = audioSettings.soundEffectsEnabled,
                            onCheckedChange = {
                                onAudioSettingsChange(audioSettings.copy(soundEffectsEnabled = it))
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Background music", modifier = Modifier.weight(1f))
                        Switch(
                            checked = audioSettings.backgroundMusicEnabled,
                            onCheckedChange = {
                                onAudioSettingsChange(audioSettings.copy(backgroundMusicEnabled = it))
                            }
                        )
                    }
                } else {
                    Text("Audio playback is not available on this platform yet.")
                }
                if (audioSaveError != null) {
                    Text(audioSaveError, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        Text(
            "Your audio choices are saved on this device.",
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
