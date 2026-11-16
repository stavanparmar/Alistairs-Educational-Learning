package com.alistair.learning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class LearningModule {
    GAMES,
    MATHS,
    GENERAL_KNOWLEDGE,
    DRAWING,
    COLOURS,
    BUDDY
}

data class ActivityQuestion(
    val prompt: String,
    val options: List<String>,
    val answer: String
)

fun mathsQuestionsForAge(age: Int): List<ActivityQuestion> {
    return when (ageBandFor(age)) {
        AgeBand.EARLY -> listOf(
            ActivityQuestion("How many stars? ⭐ ⭐ ⭐", listOf("2", "3", "4"), "3"),
            ActivityQuestion("What comes after 4?", listOf("3", "5", "6"), "5"),
            ActivityQuestion("Which number is smaller?", listOf("2", "5", "8"), "2"),
            ActivityQuestion("How many apples? 🍎 🍎", listOf("1", "2", "3"), "2")
        )
        AgeBand.FOUNDATIONS -> listOf(
            ActivityQuestion("What is 2 + 3?", listOf("4", "5", "6"), "5"),
            ActivityQuestion("What is 8 - 3?", listOf("4", "5", "6"), "5"),
            ActivityQuestion("What number comes after 19?", listOf("18", "20", "21"), "20"),
            ActivityQuestion("What is 4 + 4?", listOf("6", "8", "9"), "8")
        )
        AgeBand.DEVELOPING -> listOf(
            ActivityQuestion("What is 12 + 7?", listOf("18", "19", "20"), "19"),
            ActivityQuestion("What is 20 - 6?", listOf("12", "14", "16"), "14"),
            ActivityQuestion("What is 5 × 3?", listOf("8", "15", "20"), "15"),
            ActivityQuestion("What is 24 ÷ 4?", listOf("4", "6", "8"), "6")
        )
        AgeBand.ADVANCING -> listOf(
            ActivityQuestion("What is 9 × 7?", listOf("56", "63", "72"), "63"),
            ActivityQuestion("What is 144 ÷ 12?", listOf("11", "12", "13"), "12"),
            ActivityQuestion("What is 35 + 48?", listOf("73", "83", "93"), "83"),
            ActivityQuestion("What is 100 - 37?", listOf("53", "63", "73"), "63")
        )
    }
}

fun generalKnowledgeQuestions(): List<ActivityQuestion> = listOf(
    ActivityQuestion("Which planet is our home?", listOf("Mars", "Earth", "Venus"), "Earth"),
    ActivityQuestion("How many legs does a spider have?", listOf("6", "8", "10"), "8"),
    ActivityQuestion("Which star lights our daytime sky?", listOf("The Sun", "The Moon", "Polaris"), "The Sun"),
    ActivityQuestion("What do caterpillars turn into?", listOf("Frogs", "Butterflies", "Birds"), "Butterflies"),
    ActivityQuestion("Which animal is known for its black and white stripes?", listOf("Zebra", "Tiger", "Giraffe"), "Zebra")
)

fun colourQuestions(): List<ActivityQuestion> = listOf(
    ActivityQuestion("Find the red colour.", listOf("Blue", "Red", "Yellow"), "Red"),
    ActivityQuestion("Find the blue colour.", listOf("Green", "Blue", "Pink"), "Blue"),
    ActivityQuestion("Find the yellow colour.", listOf("Yellow", "Purple", "Orange"), "Yellow"),
    ActivityQuestion("Find the green colour.", listOf("Pink", "Orange", "Green"), "Green")
)

@Composable
fun LearningActivityScreen(
    module: LearningModule,
    age: Int,
    berry: Color,
    onBack: () -> Unit
) {
    when (module) {
        LearningModule.GAMES -> MemoryMatchActivity(berry, onBack)
        LearningModule.MATHS -> QuizActivity("Maths", mathsQuestionsForAge(age), berry, onBack)
        LearningModule.GENERAL_KNOWLEDGE ->
            QuizActivity("General Knowledge", generalKnowledgeQuestions(), berry, onBack)
        LearningModule.COLOURS -> QuizActivity("Colour Match", colourQuestions(), berry, onBack, showColourSwatches = true)
        LearningModule.DRAWING -> DrawingActivity(berry, onBack)
        LearningModule.BUDDY -> BuddyActivity(berry, onBack)
    }
}

@Composable
private fun ActivityPage(
    title: String,
    berry: Color,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Text("← Back to learning")
        }
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = berry)
        content()
    }
}

@Composable
private fun QuizActivity(
    title: String,
    questions: List<ActivityQuestion>,
    berry: Color,
    onBack: () -> Unit,
    showColourSwatches: Boolean = false
) {
    var questionIndex by remember(title) { mutableIntStateOf(0) }
    var score by remember(title) { mutableIntStateOf(0) }
    var selectedAnswer by remember(title) { mutableStateOf<String?>(null) }
    val finished = questionIndex >= questions.size

    ActivityPage(title, berry, onBack) {
        if (finished) {
            Text("You scored $score out of ${questions.size}!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Great learning! Try again and see if you can beat your score.")
            Button(
                onClick = {
                    questionIndex = 0
                    score = 0
                    selectedAnswer = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = berry)
            ) {
                Text("Play again")
            }
        } else {
            val question = questions[questionIndex]
            Text("Question ${questionIndex + 1} of ${questions.size}", color = MaterialTheme.colorScheme.secondary)
            Text(question.prompt, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            question.options.forEach { option ->
                val answerColour = when {
                    selectedAnswer == null -> berry
                    option == question.answer -> Color(0xFF2E8B57)
                    option == selectedAnswer -> Color(0xFFCC5B55)
                    else -> berry.copy(alpha = 0.55f)
                }
                Button(
                    onClick = {
                        if (selectedAnswer == null) {
                            selectedAnswer = option
                            if (option == question.answer) score += 1
                        }
                    },
                    enabled = selectedAnswer == null,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = answerColour)
                ) {
                    if (showColourSwatches) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = colourForName(option),
                                shape = CircleShape,
                                modifier = Modifier.size(28.dp)
                            ) {}
                            Text(option)
                        }
                    } else {
                        Text(option)
                    }
                }
            }
            if (selectedAnswer != null) {
                Text(
                    if (selectedAnswer == question.answer) "That's right! ⭐"
                    else "Good try! The answer is ${question.answer}.",
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        questionIndex += 1
                        selectedAnswer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = berry)
                ) {
                    Text(if (questionIndex == questions.lastIndex) "See my score" else "Next question")
                }
            }
        }
    }
}

private fun colourForName(name: String): Color = when (name) {
    "Red" -> Color(0xFFE53935)
    "Blue" -> Color(0xFF2878D0)
    "Yellow" -> Color(0xFFFFD740)
    "Green" -> Color(0xFF43A047)
    "Pink" -> Color(0xFFEC6CA8)
    "Purple" -> Color(0xFF8E5AC7)
    else -> Color(0xFFFF8A45)
}

private data class DrawingStroke(val colour: Color, val points: List<Offset>)

private fun DrawScope.drawStroke(points: List<Offset>, colour: Color) {
    if (points.size == 1) {
        drawCircle(colour, radius = 8.dp.toPx(), center = points.first())
    } else {
        points.zipWithNext().forEach { (start, end) ->
            drawLine(colour, start, end, strokeWidth = 12.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun MemoryMatchActivity(berry: Color, onBack: () -> Unit) {
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

    ActivityPage("Picture Match", berry, onBack) {
        Text(
            if (game.isComplete) "You found every pair! Great job!"
            else "Turn over the picture cards and find all six pairs."
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
                        modifier = Modifier.weight(1f).height(90.dp).semantics {
                            contentDescription = if (isVisible) {
                                "Revealed picture ${card.symbol}"
                            } else {
                                "Face-down picture card ${card.id + 1}"
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVisible) berry.copy(alpha = 0.16f) else berry
                        )
                    ) {
                        Text(
                            if (isVisible) card.symbol else "🧩",
                            fontSize = 34.sp,
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

@Composable
private fun DrawingActivity(berry: Color, onBack: () -> Unit) {
    val palette = listOf(
        Color(0xFF7253B7),
        Color(0xFFFF6B6B),
        Color(0xFF2D9CDB),
        Color(0xFF27AE60),
        Color(0xFFFFB800)
    )
    var selectedColour by remember { mutableStateOf(palette.first()) }
    var strokes by remember { mutableStateOf<List<DrawingStroke>>(emptyList()) }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }

    ActivityPage("Draw a Picture", berry, onBack) {
        Text("Choose a colour, then draw with your finger.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            palette.forEach { colour ->
                Button(
                    onClick = { selectedColour = colour },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colour)
                ) {
                    if (selectedColour == colour) Text("✓", color = Color.White)
                }
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(Color.White, RoundedCornerShape(18.dp))
                .border(2.dp, berry.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                .pointerInput(selectedColour) {
                    detectDragGestures(
                        onDragStart = { currentStroke = listOf(it) },
                        onDrag = { change, _ ->
                            change.consume()
                            currentStroke = currentStroke + change.position
                        },
                        onDragEnd = {
                            if (currentStroke.isNotEmpty()) {
                                strokes = strokes + DrawingStroke(selectedColour, currentStroke)
                            }
                            currentStroke = emptyList()
                        },
                        onDragCancel = { currentStroke = emptyList() }
                    )
                }
        ) {
            strokes.forEach { stroke -> drawStroke(stroke.points, stroke.colour) }
            drawStroke(currentStroke, selectedColour)
        }
        Button(
            onClick = {
                strokes = emptyList()
                currentStroke = emptyList()
            },
            colors = ButtonDefaults.buttonColors(containerColor = berry)
        ) {
            Text("Clear drawing")
        }
    }
}

@Composable
private fun BuddyActivity(berry: Color, onBack: () -> Unit) {
    val facts = listOf(
        "Why is the sky blue?" to "Sunlight has many colours. Air scatters blue light more than most other colours.",
        "How do plants drink?" to "Roots take in water from the soil and carry it up through the plant.",
        "Why do we see the Moon?" to "The Moon reflects light from the Sun back toward Earth.",
        "What is a habitat?" to "A habitat is the natural home where an animal or plant finds what it needs."
    )
    var selectedFact by remember { mutableIntStateOf(-1) }

    ActivityPage("Learning Buddy", berry, onBack) {
        Text("Pick a question to discover an offline fun fact.")
        facts.forEachIndexed { index, (question, answer) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = berry.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Button(
                        onClick = { selectedFact = if (selectedFact == index) -1 else index },
                        colors = ButtonDefaults.buttonColors(containerColor = berry)
                    ) {
                        Text(question)
                    }
                    if (selectedFact == index) {
                        Text(answer, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}
