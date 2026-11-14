package com.alistair.learning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Berry = androidx.compose.ui.graphics.Color(0xFF7253B7)
private val Coral = androidx.compose.ui.graphics.Color(0xFFFF8A70)
private val Mint = androidx.compose.ui.graphics.Color(0xFF62C9A5)
private val Cream = androidx.compose.ui.graphics.Color(0xFFFFF9F0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AlistairApp() } }
    }
}

@Composable
fun AlistairApp() {
    var childName by remember { mutableStateOf("") }
    var age by remember { mutableIntStateOf(5) }
    var started by remember { mutableStateOf(false) }

    Surface(color = Cream, modifier = Modifier.fillMaxSize()) {
        if (!started) {
            ProfileWelcome(
                name = childName,
                onNameChange = { childName = it },
                age = age,
                onAgeChange = { age = it },
                onStart = { if (childName.isNotBlank()) started = true }
            )
        } else {
            LearningHome(name = childName.trim(), age = age)
        }
    }
}

@Composable
private fun ProfileWelcome(
    name: String,
    onNameChange: (String) -> Unit,
    age: Int,
    onAgeChange: (Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌈", fontSize = 54.sp)
        Text("Welcome, little learner!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Berry)
        Text("Alistair’s Educational Learning", color = Berry)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
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
                    onClick = { onAgeChange(value) },
                    label = { Text("$value") }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onStart,
            enabled = name.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Berry),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) { Text("Let's learn and play!", fontWeight = FontWeight.Bold) }
        Text("No account needed • Designed for offline learning", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
    }
}

@Composable
private fun LearningHome(name: String, age: Int) {
    val subjects = listOf(
        "🧩 Games" to "Memory, matching and puzzles",
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
        Text("Hello, $name! 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Berry)
        Text("Your learning adventure • Age $age", color = MaterialTheme.colorScheme.secondary)
        subjects.forEachIndexed { index, item ->
            val color = listOf(Berry, Coral, Mint)[index % 3]
            Card(
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.14f)),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(item.first, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Berry)
                    Text(item.second)
                }
            }
        }
        Text(
            "Starter preview: subject cards are navigation placeholders. Full activities, local profile persistence, RAG and on-device model integration are roadmap items.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
