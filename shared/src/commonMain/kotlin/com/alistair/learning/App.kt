package com.alistair.learning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AlistairApp(profileStore: ChildProfileStore) {
    val berry = Color(0xFF7253B7)
    val coral = Color(0xFFFF8A70)
    val mint = Color(0xFF62C9A5)
    val cream = Color(0xFFFFF9F0)
    var profile by remember(profileStore) { mutableStateOf(profileStore.load()) }
    var editingProfile by remember(profileStore) { mutableStateOf(profile == null) }
    var showingMemoryGame by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

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

    MaterialTheme {
        Surface(color = cream, modifier = Modifier.fillMaxSize()) {
            if (profile == null || editingProfile) {
                ProfileForm(
                    profile = profile,
                    saveError = saveError,
                    onSave = saveProfile,
                    onCancel = {
                        editingProfile = false
                        saveError = null
                    },
                    berry = berry
                )
            } else if (showingMemoryGame) {
                MemoryMatchGame(
                    berry = berry,
                    onBack = { showingMemoryGame = false }
                )
            } else {
                LearningHome(
                    profile = requireNotNull(profile),
                    berry = berry,
                    coral = coral,
                    mint = mint,
                    onEditProfile = {
                        saveError = null
                        editingProfile = true
                    },
                    onPlayMemoryGame = { showingMemoryGame = true }
                )
            }
        }
    }
}

@Composable
private fun ProfileForm(
    profile: ChildProfile?,
    saveError: String?,
    onSave: (String, Int) -> Unit,
    onCancel: () -> Unit,
    berry: Color
) {
    var childName by remember(profile) { mutableStateOf(profile?.displayName.orEmpty()) }
    var age by remember(profile) { mutableIntStateOf(profile?.age ?: 5) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌈", fontSize = 54.sp)
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
            onValueChange = { childName = it },
            label = { Text("What's your name?") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Text("How old are you?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (3..10).forEach { value ->
                FilterChip(
                    selected = age == value,
                    onClick = { age = value },
                    label = { Text("$value") }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onSave(childName, age) },
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

@Composable
private fun LearningHome(
    profile: ChildProfile,
    berry: Color,
    coral: Color,
    mint: Color,
    onEditProfile: () -> Unit,
    onPlayMemoryGame: () -> Unit
) {
    val subjects = listOf(
        "🧩 Games" to "Tap to play Memory Match",
        "🔢 Maths" to "Numbers and problem solving",
        "🌍 General Knowledge" to "Discover the world",
        "🎨 Drawing" to "Create your own pictures",
        "🌈 Colours" to "Explore colours and patterns",
        "🧠 Learning Buddy" to "Local AI learning assistant"
    )
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hello, ${profile.displayName}! 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = berry)
        Text("Your learning adventure • Age ${profile.age}", color = MaterialTheme.colorScheme.secondary)
        TextButton(onClick = onEditProfile) { Text("Edit profile") }
        subjects.forEachIndexed { index, item ->
            val color = listOf(berry, coral, mint)[index % 3]
            Card(
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.14f)),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth().then(
                    if (index == 0) Modifier.clickable(onClick = onPlayMemoryGame) else Modifier
                )
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(item.first, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = berry)
                    Text(item.second)
                }
            }

        }
        Text(
            "Starter preview: subject cards are navigation placeholders. Full activities, local progress, RAG and on-device model integration are roadmap items.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MemoryMatchGame(berry: Color, onBack: () -> Unit) {
    val symbols = listOf("🐸", "🌟", "🍎", "🐠", "🚀", "🌈")
    var game by remember {
        mutableStateOf(newMemoryGame(symbols).let { it.copy(cards = it.cards.shuffled()) })
    }

    LaunchedEffect(game.firstCardId, game.secondCardId, game.turns) {
        if (game.hasMismatch) {
            delay(900)
            game = hideMismatchedMemoryCards(game)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Text("← Back to learning")
        }
        Text("Memory Match", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = berry)
        Text(
            if (game.isComplete) "You matched every pair! Great job!" else "Find all six matching pairs.",
            color = MaterialTheme.colorScheme.secondary
        )
        Text("Turns: ${game.turns}", fontWeight = FontWeight.Bold)

        game.cards.chunked(3).forEach { rowCards ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowCards.forEach { card ->
                    val isVisible = card.matched ||
                        card.id == game.firstCardId ||
                        card.id == game.secondCardId
                    Button(
                        onClick = { game = revealMemoryCard(game, card.id) },
                        enabled = !card.matched && !isVisible && !game.hasMismatch,
                        modifier = Modifier.weight(1f).height(90.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVisible) berry.copy(alpha = 0.16f) else berry
                        )
                    ) {
                        Text(
                            if (isVisible) card.symbol else "Tap",
                            fontSize = if (isVisible) 34.sp else 18.sp,
                            color = if (isVisible) berry else Color.White
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                game = newMemoryGame(symbols).let { newGame ->
                    newGame.copy(cards = newGame.cards.shuffled())
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = berry)
        ) {
            Text(if (game.isComplete) "Play again" else "Restart game")
        }
    }
}
