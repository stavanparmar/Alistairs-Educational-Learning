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

@Composable
fun AlistairApp(profileStore: ChildProfileStore) {
    val berry = Color(0xFF7253B7)
    val coral = Color(0xFFFF8A70)
    val mint = Color(0xFF62C9A5)
    val cream = Color(0xFFFFF9F0)
    var profile by remember(profileStore) { mutableStateOf(profileStore.load()) }
    var editingProfile by remember(profileStore) { mutableStateOf(profile == null) }
    var activeModule by remember { mutableStateOf<LearningModule?>(null) }
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
            } else if (activeModule != null) {
                LearningActivityScreen(
                    module = requireNotNull(activeModule),
                    age = requireNotNull(profile).age,
                    berry = berry,
                    onBack = { activeModule = null }
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
                    onOpenModule = { activeModule = it }
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
    onOpenModule: (LearningModule) -> Unit
) {
    val subjects = listOf(
        LearningModule.GAMES to ("🧩 Games" to "Picture matching and memory"),
        LearningModule.MATHS to ("🔢 Maths" to "Solve number challenges"),
        LearningModule.GENERAL_KNOWLEDGE to ("🌍 General Knowledge" to "Explore fun facts"),
        LearningModule.DRAWING to ("🎨 Drawing" to "Draw with your finger"),
        LearningModule.COLOURS to ("🌈 Colours" to "Find the matching colours"),
        LearningModule.BUDDY to ("🧠 Learning Buddy" to "Tap a question for an offline fun fact")
    )
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hello, ${profile.displayName}! 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = berry)
        Text("Your learning adventure • Age ${profile.age}", color = MaterialTheme.colorScheme.secondary)
        TextButton(onClick = onEditProfile) { Text("Edit profile") }
        subjects.forEachIndexed { index, (module, item) ->
            val color = listOf(berry, coral, mint)[index % 3]
            Card(
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.14f)),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth().clickable { onOpenModule(module) }
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(item.first, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = berry)
                    Text(item.second)
                }
            }
        }
        Text(
            "Choose any activity to start learning and playing.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
