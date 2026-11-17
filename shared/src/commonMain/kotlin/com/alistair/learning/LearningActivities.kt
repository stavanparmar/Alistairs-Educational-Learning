package com.alistair.learning

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class LearningModule {
    GAMES,
    MATHS,
    GENERAL_KNOWLEDGE,
    DRAWING,
    COLOURS,
    BUDDY,
    WORD_GAMES,
    NUMBER_PUZZLES,
    MAZE,
    PATTERNS,
    COLORING,
    CAR_DRIVING
}

enum class SoundEffect {
    TAP,
    TYPE,
    SELECT,
    CORRECT,
    INCORRECT
}

data class ActivityQuestion(
    val prompt: String,
    val options: List<String>,
    val answer: String,
    val illustration: String = "✨"
)

internal fun questionsForStage(
    questions: List<ActivityQuestion>,
    stageNumber: Int
): List<ActivityQuestion> {
    require(questions.isNotEmpty()) { "A stage needs at least one question." }
    require(stageNumber > 0) { "Stage numbers must be positive." }

    val offset = (stageNumber - 1) % questions.size
    return (questions.drop(offset) + questions.take(offset))
        .map { question -> question.copy(options = question.options.shuffled()) }
}

@Composable
internal fun AnimatedLearningIllustration(emoji: String, fontSize: Int = 56) {
    val transition = rememberInfiniteTransition(label = "learning-illustration")
    val scale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "illustration-scale"
    )
    Text(emoji, fontSize = fontSize.sp, modifier = Modifier.scale(scale))
}

fun mathsQuestionsForAge(age: Int): List<ActivityQuestion> {
    return when (ageBandFor(age)) {
        AgeBand.EARLY -> listOf(
            ActivityQuestion("How many stars? ⭐ ⭐ ⭐", listOf("2", "3", "4"), "3", "⭐"),
            ActivityQuestion("What comes after 4?", listOf("3", "5", "6"), "5", "🔢"),
            ActivityQuestion("Which number is smaller?", listOf("2", "5", "8"), "2", "🔎"),
            ActivityQuestion("How many apples? 🍎 🍎", listOf("1", "2", "3"), "2", "🍎")
        )
        AgeBand.FOUNDATIONS -> listOf(
            ActivityQuestion("What is 2 + 3?", listOf("4", "5", "6"), "5", "➕"),
            ActivityQuestion("What is 8 - 3?", listOf("4", "5", "6"), "5", "➖"),
            ActivityQuestion("What number comes after 19?", listOf("18", "20", "21"), "20", "🔢"),
            ActivityQuestion("What is 4 + 4?", listOf("6", "8", "9"), "8", "🧮")
        )
        AgeBand.DEVELOPING -> listOf(
            ActivityQuestion("What is 12 + 7?", listOf("18", "19", "20"), "19", "➕"),
            ActivityQuestion("What is 20 - 6?", listOf("12", "14", "16"), "14", "➖"),
            ActivityQuestion("What is 5 × 3?", listOf("8", "15", "20"), "15", "✖️"),
            ActivityQuestion("What is 24 ÷ 4?", listOf("4", "6", "8"), "6", "➗")
        )
        AgeBand.ADVANCING -> listOf(
            ActivityQuestion("What is 9 × 7?", listOf("56", "63", "72"), "63", "✖️"),
            ActivityQuestion("What is 144 ÷ 12?", listOf("11", "12", "13"), "12", "➗"),
            ActivityQuestion("What is 35 + 48?", listOf("73", "83", "93"), "83", "➕"),
            ActivityQuestion("What is 100 - 37?", listOf("53", "63", "73"), "63", "➖")
        )
    }
}

fun generalKnowledgeQuestions(): List<ActivityQuestion> = listOf(
    ActivityQuestion("Which planet is our home?", listOf("Mars", "Earth", "Venus"), "Earth", "🌍"),
    ActivityQuestion("How many legs does a spider have?", listOf("6", "8", "10"), "8", "🕷️"),
    ActivityQuestion("Which star lights our daytime sky?", listOf("The Sun", "The Moon", "Polaris"), "The Sun", "☀️"),
    ActivityQuestion("What do caterpillars turn into?", listOf("Frogs", "Butterflies", "Birds"), "Butterflies", "🦋"),
    ActivityQuestion(
        "Which animal is known for its black and white stripes?",
        listOf("Zebra", "Tiger", "Giraffe"),
        "Zebra",
        "🦓"
    )
)

fun generalKnowledgeQuestionsForAge(age: Int): List<ActivityQuestion> =
    when (ageBandFor(age)) {
        AgeBand.EARLY -> listOf(
            ActivityQuestion("What sound does a cow make?", listOf("Moo", "Meow", "Baa"), "Moo", "🐄"),
            ActivityQuestion("How many legs does a dog have?", listOf("2", "4", "6"), "4", "🐕"),
            ActivityQuestion("Which one is a fruit?", listOf("Apple", "Shoe", "Chair"), "Apple", "🍎"),
            generalKnowledgeQuestions()[2]
        )
        AgeBand.FOUNDATIONS -> generalKnowledgeQuestions().take(4) +
            ActivityQuestion("Which animal gives us wool?", listOf("Sheep", "Fish", "Frog"), "Sheep", "🐑")
        AgeBand.DEVELOPING -> generalKnowledgeQuestions() + listOf(
            ActivityQuestion("Which planet is called the Red Planet?", listOf("Mars", "Earth", "Neptune"), "Mars", "🔴"),
            ActivityQuestion("What do plants take in from the air?", listOf("Oxygen", "Carbon dioxide", "Helium"), "Carbon dioxide", "🌿")
        )
        AgeBand.ADVANCING -> generalKnowledgeQuestions() + listOf(
            ActivityQuestion("Which planet is called the Red Planet?", listOf("Mars", "Earth", "Neptune"), "Mars", "🔴"),
            ActivityQuestion("What do plants take in from the air?", listOf("Oxygen", "Carbon dioxide", "Helium"), "Carbon dioxide", "🌿"),
            ActivityQuestion("What is the process of water changing to vapor called?", listOf("Evaporation", "Freezing", "Condensation"), "Evaporation", "💧")
        )
    }

fun colourQuestionsForAge(age: Int): List<ActivityQuestion> {
    val questions = listOf(
        ActivityQuestion("Find the red colour.", listOf("Blue", "Red", "Yellow"), "Red", "🔴"),
        ActivityQuestion("Find the blue colour.", listOf("Green", "Blue", "Pink"), "Blue", "🔵"),
        ActivityQuestion("Find the yellow colour.", listOf("Yellow", "Purple", "Orange"), "Yellow", "🟡"),
        ActivityQuestion("Find the green colour.", listOf("Pink", "Orange", "Green"), "Green", "🟢"),
        ActivityQuestion("Find the pink colour.", listOf("Pink", "Blue", "Yellow"), "Pink", "🩷"),
        ActivityQuestion("Find the purple colour.", listOf("Orange", "Purple", "Red"), "Purple", "🟣"),
        ActivityQuestion("Find the orange colour.", listOf("Green", "Orange", "Blue"), "Orange", "🟠")
    )
    return when (ageBandFor(age)) {
        AgeBand.EARLY -> questions.take(3)
        AgeBand.FOUNDATIONS -> questions.take(4)
        AgeBand.DEVELOPING, AgeBand.ADVANCING -> questions
    }
}

private fun wordGameQuestions(age: Int): List<ActivityQuestion> {
    val questions = listOf(
        ActivityQuestion("Unscramble TAC, a pet that meows.", listOf("CAT", "ACT", "COT"), "CAT", "🐱"),
        ActivityQuestion("Unscramble GOD, a pet that barks.", listOf("DOG", "GOD", "DIG"), "DOG", "🐶"),
        ActivityQuestion("Unscramble NUS, bright in the daytime sky.", listOf("SUN", "RUN", "BUS"), "SUN", "☀️"),
        ActivityQuestion("Unscramble KOOB, something you read.", listOf("BOOK", "LOOK", "BOOT"), "BOOK", "📚"),
        ActivityQuestion("Unscramble HSIF, an animal with fins.", listOf("FISH", "SHIP", "DISH"), "FISH", "🐟"),
        ActivityQuestion("Unscramble ELAPP, a fruit.", listOf("APPLE", "PAPER", "PLATE"), "APPLE", "🍎")
    )
    return when (ageBandFor(age)) {
        AgeBand.EARLY -> questions.take(3)
        AgeBand.FOUNDATIONS -> questions.take(5)
        AgeBand.DEVELOPING, AgeBand.ADVANCING -> questions
    }
}

private fun numberPuzzleQuestions(age: Int): List<ActivityQuestion> {
    val questions = listOf(
        ActivityQuestion("2, 4, 6, __", listOf("7", "8", "9"), "8", "🔢"),
        ActivityQuestion("5, 10, 15, __", listOf("18", "20", "25"), "20", "🧮"),
        ActivityQuestion("1, 2, 4, 8, __", listOf("12", "14", "16"), "16", "🚀"),
        ActivityQuestion("20, 18, 16, __", listOf("12", "14", "15"), "14", "⬇️"),
        ActivityQuestion("1, 3, 6, 10, __", listOf("13", "15", "16"), "15", "🔢"),
        ActivityQuestion("3, 6, 9, __", listOf("10", "12", "15"), "12", "🧮")
    )
    return when (ageBandFor(age)) {
        AgeBand.EARLY -> listOf(questions[0], questions[5])
        AgeBand.FOUNDATIONS -> questions.take(3) + questions[5]
        AgeBand.DEVELOPING -> questions
        AgeBand.ADVANCING -> questions.reversed()
    }
}

private fun patternQuestions(age: Int): List<ActivityQuestion> {
    val questions = listOf(
        ActivityQuestion("Circle, square, circle, square, __", listOf("Circle", "Square", "Triangle"), "Circle", "🔵"),
        ActivityQuestion("Red, blue, blue, red, blue, blue, __", listOf("Red", "Blue", "Green"), "Red", "🎨"),
        ActivityQuestion("Small, medium, large, small, medium, __", listOf("Small", "Medium", "Large"), "Large", "📏"),
        ActivityQuestion("Triangle, triangle, star, triangle, triangle, __", listOf("Circle", "Star", "Square"), "Star", "⭐"),
        ActivityQuestion("1, 2, 1, 2, 1, __", listOf("1", "2", "3"), "2", "🔁")
    )
    return when (ageBandFor(age)) {
        AgeBand.EARLY -> listOf(questions[0], questions[4])
        AgeBand.FOUNDATIONS -> listOf(questions[0], questions[1], questions[4])
        AgeBand.DEVELOPING, AgeBand.ADVANCING -> questions
    }
}

@Composable
fun LearningActivityScreen(
    module: LearningModule,
    age: Int,
    berry: Color,
    onBack: () -> Unit,
    onSoundEffect: (SoundEffect) -> Unit,
    progress: LearningProgress,
    onStageCompleted: (LearningModule, Int) -> Unit
) {
    val initialStage = progress.nextStage(module)
    when (module) {
        LearningModule.GAMES -> MemoryMatchActivity(
            age, initialStage, berry, onBack, onSoundEffect, onStageCompleted
        )
        LearningModule.MATHS -> QuizActivity(
            LearningModule.MATHS, "Maths", mathsQuestionsForAge(age), initialStage,
            berry, onBack, onSoundEffect, onStageCompleted
        )
        LearningModule.GENERAL_KNOWLEDGE -> QuizActivity(
            LearningModule.GENERAL_KNOWLEDGE, "General Knowledge",
            generalKnowledgeQuestionsForAge(age), initialStage,
            berry, onBack, onSoundEffect, onStageCompleted
        )
        LearningModule.COLOURS ->
            QuizActivity(
                LearningModule.COLOURS,
                "Colour Match",
                colourQuestionsForAge(age),
                initialStage,
                berry,
                onBack,
                onSoundEffect,
                onStageCompleted,
                showColourSwatches = true
            )
        LearningModule.DRAWING -> DrawingActivity(berry, onBack, onSoundEffect)
        LearningModule.BUDDY ->
            BuddyActivity(
                age, initialStage, berry, onBack, onSoundEffect, onStageCompleted
            )
        LearningModule.WORD_GAMES -> QuizActivity(
            LearningModule.WORD_GAMES, "Word Scramble", wordGameQuestions(age), initialStage,
            berry, onBack, onSoundEffect, onStageCompleted
        )
        LearningModule.NUMBER_PUZZLES -> QuizActivity(
            LearningModule.NUMBER_PUZZLES, "Number Puzzles", numberPuzzleQuestions(age), initialStage,
            berry, onBack, onSoundEffect, onStageCompleted
        )
        LearningModule.PATTERNS -> QuizActivity(
            LearningModule.PATTERNS, "Patterns & Shapes", patternQuestions(age), initialStage,
            berry, onBack, onSoundEffect, onStageCompleted
        )
        LearningModule.MAZE ->
            MazeActivity(age, initialStage, berry, onBack, onSoundEffect, onStageCompleted)
        LearningModule.COLORING -> ColoringActivity(berry, onBack, onSoundEffect)
        LearningModule.CAR_DRIVING ->
            CarDrivingActivity(age, initialStage, berry, onBack, onSoundEffect, onStageCompleted)
    }
}

@Composable
private fun ActivityPage(
    title: String,
    berry: Color,
    onBack: () -> Unit,
    onSoundEffect: (SoundEffect) -> Unit,
    content: @Composable () -> Unit
) {
    val module = when (title) {
        "Picture Match" -> LearningModule.GAMES
        "Maths" -> LearningModule.MATHS
        "General Knowledge" -> LearningModule.GENERAL_KNOWLEDGE
        "Colour Match" -> LearningModule.COLOURS
        "Learning Buddy" -> LearningModule.BUDDY
        "Word Scramble" -> LearningModule.WORD_GAMES
        "Number Puzzles" -> LearningModule.NUMBER_PUZZLES
        "Patterns & Shapes" -> LearningModule.PATTERNS
        "Star Maze" -> LearningModule.MAZE
        "Coloring Studio" -> LearningModule.COLORING
        "Car Driving" -> LearningModule.CAR_DRIVING
        else -> LearningModule.GAMES
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = {
                onSoundEffect(SoundEffect.TAP)
                onBack()
            },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("← Back to learning")
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = berry.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Canvas(Modifier.size(width = 116.dp, height = 84.dp)) {
                    drawColoringScene(coloringPreviewFor(module), emptyMap())
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = berry)
                    Text(
                        when (module) {
                            LearningModule.GAMES -> "Picture matching adventure"
                            LearningModule.MATHS, LearningModule.NUMBER_PUZZLES -> "Explore and solve"
                            LearningModule.GENERAL_KNOWLEDGE, LearningModule.BUDDY -> "Discover something new"
                            LearningModule.DRAWING, LearningModule.COLORING -> "Create your own picture"
                            LearningModule.COLOURS, LearningModule.PATTERNS -> "Look closely and play"
                            LearningModule.WORD_GAMES -> "Find the hidden word"
                            LearningModule.MAZE -> "Follow the path to the goal"
                            LearningModule.CAR_DRIVING -> "Take the scenic route"
                        },
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        content()
    }
}

private fun coloringPreviewFor(module: LearningModule): ColoringScene {
    val themeIndex = when (module) {
        LearningModule.GAMES -> 0
        LearningModule.MATHS -> 5
        LearningModule.GENERAL_KNOWLEDGE -> 2
        LearningModule.DRAWING -> 3
        LearningModule.COLOURS -> 3
        LearningModule.BUDDY -> 4
        LearningModule.WORD_GAMES -> 7
        LearningModule.NUMBER_PUZZLES -> 6
        LearningModule.MAZE -> 8
        LearningModule.PATTERNS -> 9
        LearningModule.COLORING -> 1
        LearningModule.CAR_DRIVING -> 2
    }
    return coloringScenes[themeIndex * ColoringTheme.DINO.titles.size]
}

@Composable
private fun QuizActivity(
    module: LearningModule,
    title: String,
    questions: List<ActivityQuestion>,
    initialStage: Int,
    berry: Color,
    onBack: () -> Unit,
    onSoundEffect: (SoundEffect) -> Unit,
    onStageCompleted: (LearningModule, Int) -> Unit,
    showColourSwatches: Boolean = false
) {
    var questionIndex by remember(title) { mutableIntStateOf(0) }
    var score by remember(title) { mutableIntStateOf(0) }
    var selectedAnswer by remember(title) { mutableStateOf<String?>(null) }
    var stage by remember(title) { mutableIntStateOf(initialStage) }
    val questionPool by remember(title) { mutableStateOf(questions.shuffled()) }
    var roundQuestions by remember(title) {
        mutableStateOf(questionsForStage(questionPool, stage))
    }
    val finished = questionIndex >= roundQuestions.size

    ActivityPage(title, berry, onBack, onSoundEffect) {
        AnimatedContent(
            targetState = questionIndex to finished,
            transitionSpec = {
                (fadeIn(tween(180)) + slideInVertically { it / 10 }) togetherWith
                    (fadeOut(tween(120)) + slideOutVertically { -it / 10 })
            },
            label = "quiz-question"
        ) { (currentQuestionIndex, isFinished) ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isFinished) {
                    Text(
                        "Stage $stage complete! You scored $score out of ${roundQuestions.size}.",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Great learning! A new challenge is ready.")
                    Button(
                        onClick = {
                            onSoundEffect(SoundEffect.TAP)
                            stage += 1
                            questionIndex = 0
                            score = 0
                            selectedAnswer = null
                            roundQuestions = questionsForStage(questionPool, stage)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = berry)
                    ) {
                        Text("Next stage")
                    }
                } else {
                    val question = roundQuestions[currentQuestionIndex]
                    Text(
                        "Stage $stage • Question ${currentQuestionIndex + 1} of ${roundQuestions.size}",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    AnimatedLearningIllustration(question.illustration, fontSize = 68)
                    Text(question.prompt, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    question.options.forEach { option ->
                        val answerColour = when {
                            selectedAnswer == null -> berry
                            option == selectedAnswer && option == question.answer -> Color(0xFF2E8B57)
                            option == selectedAnswer -> Color(0xFFCC5B55)
                            else -> berry.copy(alpha = 0.55f)
                        }
                        val animatedAnswerColour by animateColorAsState(
                            targetValue = answerColour,
                            animationSpec = tween(220),
                            label = "answer-feedback"
                        )
                        Button(
                            onClick = {
                                if (selectedAnswer == null) {
                                    selectedAnswer = option
                                    if (option == question.answer) {
                                        score += 1
                                        onSoundEffect(SoundEffect.CORRECT)
                                    } else {
                                        onSoundEffect(SoundEffect.INCORRECT)
                                    }
                                }
                            },
                            enabled = selectedAnswer == null,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = animatedAnswerColour,
                                disabledContainerColor = animatedAnswerColour,
                                disabledContentColor = Color.White
                            )
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
                        val correct = selectedAnswer == question.answer
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(180)) + expandVertically()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AnimatedLearningIllustration(
                                    if (correct) "🌟" else "💪",
                                    fontSize = 42
                                )
                                Text(
                                    if (correct) "That's right! ⭐" else "Not quite. Keep practicing!",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Button(
                            onClick = {
                                onSoundEffect(SoundEffect.TAP)
                                if (currentQuestionIndex == roundQuestions.lastIndex) {
                                    onStageCompleted(module, stage)
                                }
                                questionIndex += 1
                                selectedAnswer = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = berry)
                        ) {
                            Text(
                                if (currentQuestionIndex == roundQuestions.lastIndex) {
                                    "See my score"
                                } else {
                                    "Next question"
                                }
                            )
                        }
                    }
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
private fun MemoryMatchActivity(
    age: Int,
    initialStage: Int,
    berry: Color,
    onBack: () -> Unit,
    onSoundEffect: (SoundEffect) -> Unit,
    onStageCompleted: (LearningModule, Int) -> Unit
) {
    val symbols = listOf("🐸", "🌟", "🍎", "🐠", "🚀", "🌈", "🦋", "🐢", "🍉", "🦕", "🌻", "🐙")
    val cardBackColors = listOf(
        Color(0xFFE76F51),
        Color(0xFFF4A261),
        Color(0xFFE9C46A),
        Color(0xFF2A9D8F),
        Color(0xFF56C596),
        Color(0xFF48CAE4),
        Color(0xFF4D96FF),
        Color(0xFF7B68EE),
        Color(0xFF9B5DE5),
        Color(0xFFF15BB5),
        Color(0xFFFF7096),
        Color(0xFF8D79D6)
    )
    var stage by remember { mutableIntStateOf(initialStage) }
    val pairCount = memoryPairCountFor(age, stage).coerceAtMost(symbols.size)
    var tileColors by remember { mutableStateOf(cardBackColors.shuffled()) }
    var game by remember {
        mutableStateOf(
            newMemoryGame(symbols.take(pairCount)).let { it.copy(cards = it.cards.shuffled()) }
        )
    }

    LaunchedEffect(game.firstCardId, game.secondCardId, game.turns) {
        if (game.hasMismatch) {
            delay(900)
            game = hideMismatchedMemoryCards(game)
        }
    }

    ActivityPage("Picture Match", berry, onBack, onSoundEffect) {
        Text(
            if (game.isComplete) "Stage $stage complete! You found every pair!"
            else "Stage $stage • Find all $pairCount pairs."
        )
        Text("Turns: ${game.turns}", fontWeight = FontWeight.Bold)
        game.cards.chunked(3).forEach { rowCards ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowCards.forEach { card ->
                    val isVisible = card.matched ||
                        card.id == game.firstCardId ||
                        card.id == game.secondCardId
                    val tileColor = tileColors[card.id % tileColors.size]
                    Button(
                        onClick = {
                            val previousMatches = game.cards.count { it.matched }
                            val updatedGame = revealMemoryCard(game, card.id)
                            if (!game.isComplete && updatedGame.isComplete) {
                                onStageCompleted(LearningModule.GAMES, stage)
                            }
                            game = updatedGame
                            val newMatches = updatedGame.cards.count { it.matched }
                            onSoundEffect(
                                if (newMatches > previousMatches) SoundEffect.CORRECT else SoundEffect.TAP
                            )
                        },
                        enabled = !card.matched && !isVisible && !game.hasMismatch,
                        modifier = Modifier.weight(1f).height(90.dp).animateContentSize().semantics {
                            contentDescription = if (isVisible) {
                                "Revealed picture ${card.symbol}"
                            } else {
                                "Face-down picture card ${card.id + 1}"
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tileColor,
                            disabledContainerColor = tileColor
                        )
                    ) {
                        AnimatedContent(
                            targetState = isVisible,
                            transitionSpec = {
                                (fadeIn(tween(160)) + slideInVertically { it / 3 }) togetherWith
                                    (fadeOut(tween(100)) + slideOutVertically { -it / 3 })
                            },
                            label = "memory-card"
                        ) { revealed ->
                            if (revealed) {
                                MemoryPictureIllustration(card.symbol)
                            } else {
                                Text("🧩", fontSize = 34.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        Button(
            onClick = {
                onSoundEffect(SoundEffect.TAP)
                if (game.isComplete) stage += 1
                val nextPairCount = memoryPairCountFor(age, stage).coerceAtMost(symbols.size)
                tileColors = cardBackColors.shuffled()
                game = newMemoryGame(symbols.take(nextPairCount)).let { newGame ->
                    newGame.copy(cards = newGame.cards.shuffled())
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = berry)
        ) {
            Text(if (game.isComplete) "Next stage" else "Restart stage")
        }
    }
}

@Composable
private fun MemoryPictureIllustration(symbol: String) {
    Canvas(Modifier.size(62.dp)) {
        val unit = minOf(size.width, size.height) / 100f
        withTransform({ scale(unit, unit, pivot = Offset.Zero) }) {
            drawMemoryPicture(symbol)
        }
    }
}

private fun DrawScope.drawMemoryPicture(symbol: String) {
    val ink = Color(0xFF28323A)
    fun outlined(path: Path, fill: Color) {
        drawPath(path, fill)
        drawPath(path, ink, style = Stroke(2.5f))
    }
    when (symbol) {
        "🐸" -> {
            drawOval(Color(0xFF67BE68), Offset(20f, 47f), androidx.compose.ui.geometry.Size(62f, 38f))
            drawOval(ink, Offset(20f, 47f), androidx.compose.ui.geometry.Size(62f, 38f), style = Stroke(2.5f))
            drawCircle(Color(0xFF75CD72), 26f, Offset(51f, 43f))
            drawCircle(ink, 26f, Offset(51f, 43f), style = Stroke(2.5f))
            drawCircle(Color.White, 9f, Offset(38f, 26f))
            drawCircle(Color.White, 9f, Offset(63f, 26f))
            drawCircle(ink, 3f, Offset(39f, 27f))
            drawCircle(ink, 3f, Offset(64f, 27f))
            drawLine(ink, Offset(40f, 54f), Offset(62f, 54f), 2f, StrokeCap.Round)
            drawLine(ink, Offset(29f, 81f), Offset(18f, 89f), 4f, StrokeCap.Round)
            drawLine(ink, Offset(72f, 81f), Offset(84f, 89f), 4f, StrokeCap.Round)
        }
        "🌟" -> drawColoringStar(ink, 50f, 50f, 39f)
        "🍎" -> {
            val apple = Path().apply {
                moveTo(50f, 35f)
                cubicTo(23f, 14f, 13f, 47f, 27f, 73f)
                cubicTo(39f, 96f, 49f, 82f, 52f, 83f)
                cubicTo(74f, 93f, 90f, 62f, 82f, 40f)
                cubicTo(74f, 19f, 58f, 31f, 50f, 35f)
                close()
            }
            outlined(apple, Color(0xFFE85D5D))
            drawLine(ink, Offset(50f, 35f), Offset(54f, 20f), 4f, StrokeCap.Round)
            drawColoringLeaf(ink, Color(0xFF72C981), 54f, 26f, 67f, 18f, 70f, 29f)
        }
        "🐠" -> {
            val tail = Path().apply {
                moveTo(30f, 50f)
                lineTo(9f, 31f)
                lineTo(9f, 69f)
                close()
            }
            outlined(tail, Color(0xFFFF9C57))
            drawOval(Color(0xFFFFD166), Offset(24f, 30f), androidx.compose.ui.geometry.Size(66f, 41f))
            drawOval(ink, Offset(24f, 30f), androidx.compose.ui.geometry.Size(66f, 41f), style = Stroke(2.5f))
            drawCircle(Color.White, 7f, Offset(75f, 43f))
            drawCircle(ink, 2.5f, Offset(77f, 43f))
            repeat(3) { index -> drawCircle(Color(0xFFE79D43), 3f, Offset(43f + index * 10f, 54f)) }
            val fin = Path().apply { moveTo(49f, 32f); lineTo(63f, 16f); lineTo(70f, 34f); close() }
            outlined(fin, Color(0xFFFF9C57))
        }
        "🚀" -> {
            val rocket = Path().apply {
                moveTo(50f, 8f)
                cubicTo(24f, 28f, 30f, 62f, 36f, 72f)
                lineTo(64f, 72f)
                cubicTo(70f, 62f, 76f, 28f, 50f, 8f)
                close()
            }
            outlined(rocket, Color(0xFFF4F6F8))
            drawCircle(Color(0xFF64B5E5), 10f, Offset(50f, 43f))
            drawCircle(ink, 10f, Offset(50f, 43f), style = Stroke(2f))
            val fins = listOf(
                Path().apply { moveTo(36f, 56f); lineTo(20f, 76f); lineTo(39f, 70f); close() },
                Path().apply { moveTo(64f, 56f); lineTo(80f, 76f); lineTo(61f, 70f); close() }
            )
            fins.forEach { outlined(it, Color(0xFFE85D5D)) }
            val flame = Path().apply { moveTo(41f, 73f); quadraticTo(50f, 96f, 59f, 73f); close() }
            outlined(flame, Color(0xFFFFC857))
        }
        "🌈" -> {
            listOf(Color(0xFFE85D5D), Color(0xFFFFAA55), Color(0xFFFFD166), Color(0xFF67BE68), Color(0xFF65B9F2))
                .forEachIndexed { index, color ->
                    drawArc(
                        color,
                        190f,
                        160f,
                        false,
                        Offset(11f + index * 5f, 15f + index * 5f),
                        androidx.compose.ui.geometry.Size(78f - index * 10f, 78f - index * 10f),
                        style = Stroke(5f)
                    )
                }
            drawColoringCloud(ink, Color.White, Offset(22f, 70f))
            drawColoringCloud(ink, Color.White, Offset(78f, 70f))
        }
        "🦋" -> {
            listOf(
                Offset(28f, 37f) to Color(0xFFAF80DF),
                Offset(28f, 66f) to Color(0xFFF48AB7),
                Offset(72f, 37f) to Color(0xFFAF80DF),
                Offset(72f, 66f) to Color(0xFFF48AB7)
            ).forEach { (center, color) ->
                drawCircle(color, 19f, center)
                drawCircle(ink, 19f, center, style = Stroke(2.2f))
            }
            drawLine(ink, Offset(50f, 34f), Offset(50f, 71f), 5f, StrokeCap.Round)
            drawLine(ink, Offset(49f, 36f), Offset(39f, 21f), 2f)
            drawLine(ink, Offset(51f, 36f), Offset(61f, 21f), 2f)
        }
        "🐢" -> {
            drawOval(Color(0xFF79C66F), Offset(15f, 47f), androidx.compose.ui.geometry.Size(70f, 34f))
            drawOval(ink, Offset(15f, 47f), androidx.compose.ui.geometry.Size(70f, 34f), style = Stroke(2.5f))
            drawOval(Color(0xFFB88C58), Offset(25f, 32f), androidx.compose.ui.geometry.Size(48f, 34f))
            drawOval(ink, Offset(25f, 32f), androidx.compose.ui.geometry.Size(48f, 34f), style = Stroke(2.5f))
            drawArc(ink, 180f, 180f, false, Offset(33f, 33f), androidx.compose.ui.geometry.Size(32f, 32f), style = Stroke(2f))
            drawCircle(Color(0xFF79C66F), 13f, Offset(84f, 54f))
            drawCircle(ink, 13f, Offset(84f, 54f), style = Stroke(2.5f))
            drawCircle(ink, 2f, Offset(89f, 51f))
            listOf(Offset(30f, 77f), Offset(68f, 77f)).forEach { drawCircle(Color(0xFF79C66F), 7f, it) }
        }
        "🍉" -> {
            val slice = Path().apply {
                moveTo(10f, 35f)
                quadraticTo(50f, 100f, 90f, 35f)
                close()
            }
            outlined(slice, Color(0xFF67BE68))
            val flesh = Path().apply {
                moveTo(18f, 40f)
                quadraticTo(50f, 83f, 82f, 40f)
                close()
            }
            outlined(flesh, Color(0xFFE96C72))
            listOf(Offset(37f, 49f), Offset(50f, 58f), Offset(63f, 49f)).forEach {
                drawOval(ink, it, androidx.compose.ui.geometry.Size(3f, 6f))
            }
        }
        "🦕" -> {
            val tail = Path().apply { moveTo(31f, 58f); lineTo(10f, 44f); lineTo(20f, 69f); close() }
            outlined(tail, Color(0xFF78B971))
            drawOval(Color(0xFF78B971), Offset(20f, 43f), androidx.compose.ui.geometry.Size(53f, 34f))
            drawOval(ink, Offset(20f, 43f), androidx.compose.ui.geometry.Size(53f, 34f), style = Stroke(2.5f))
            drawRoundRect(Color(0xFF78B971), Offset(57f, 24f), androidx.compose.ui.geometry.Size(15f, 31f), androidx.compose.ui.geometry.CornerRadius(7f))
            drawCircle(Color(0xFF78B971), 14f, Offset(70f, 25f))
            drawCircle(ink, 2f, Offset(75f, 22f))
            repeat(3) { index ->
                val x = 33f + index * 12f
                val spike = Path().apply { moveTo(x, 47f); lineTo(x + 5f, 36f); lineTo(x + 10f, 47f); close() }
                outlined(spike, Color(0xFFFFD166))
            }
        }
        "🌻" -> {
            repeat(8) { index ->
                val positions = listOf(
                    Offset(50f, 18f), Offset(70f, 26f), Offset(80f, 46f), Offset(70f, 67f),
                    Offset(50f, 75f), Offset(30f, 67f), Offset(20f, 46f), Offset(30f, 26f)
                )
                val center = positions[index]
                drawOval(Color(0xFFFFC857), Offset(center.x - 8f, center.y - 12f), androidx.compose.ui.geometry.Size(16f, 24f))
                drawOval(ink, Offset(center.x - 8f, center.y - 12f), androidx.compose.ui.geometry.Size(16f, 24f), style = Stroke(1.7f))
            }
            drawCircle(Color(0xFF8D6338), 18f, Offset(50f, 46f))
            drawCircle(ink, 18f, Offset(50f, 46f), style = Stroke(2.5f))
            repeat(7) { index -> drawCircle(Color(0xFFFFE6A0), 2f, Offset(39f + index * 3f, 40f + index % 3 * 5f)) }
            drawLine(ink, Offset(50f, 65f), Offset(50f, 91f), 4f)
        }
        "🐙" -> {
            repeat(5) { index ->
                val x = 26f + index * 12f
                val tentacle = Path().apply { moveTo(x, 61f); quadraticTo(x - 9f, 83f, x + 3f, 91f) }
                drawPath(tentacle, Color(0xFFAF80DF), style = Stroke(9f, cap = StrokeCap.Round))
                drawPath(tentacle, ink, style = Stroke(2f, cap = StrokeCap.Round))
            }
            drawCircle(Color(0xFFAF80DF), 30f, Offset(50f, 47f))
            drawCircle(ink, 30f, Offset(50f, 47f), style = Stroke(2.5f))
            drawCircle(Color.White, 8f, Offset(39f, 43f))
            drawCircle(Color.White, 8f, Offset(61f, 43f))
            drawCircle(ink, 2.5f, Offset(41f, 44f))
            drawCircle(ink, 2.5f, Offset(63f, 44f))
        }
    }
}

@Composable
private fun DrawingActivity(
    berry: Color,
    onBack: () -> Unit,
    onSoundEffect: (SoundEffect) -> Unit
) {
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

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
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
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp).align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
            shape = RoundedCornerShape(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        onSoundEffect(SoundEffect.TAP)
                        onBack()
                    }
                ) {
                    Text("← Back")
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Draw a Picture", color = berry, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("Your canvas fills the screen", fontSize = 12.sp)
                }
                Spacer(Modifier.size(56.dp))
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp).align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Choose a colour, then draw anywhere!", color = berry, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    palette.forEach { colour ->
                        val colourScale by animateFloatAsState(
                            targetValue = if (selectedColour == colour) 1.12f else 1f,
                            animationSpec = tween(160),
                            label = "selected-colour"
                        )
                        Button(
                            onClick = {
                                onSoundEffect(SoundEffect.TAP)
                                selectedColour = colour
                            },
                            modifier = Modifier.size(48.dp).scale(colourScale),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colour)
                        ) {
                            if (selectedColour == colour) Text("✓", color = Color.White)
                        }
                    }
                }
                TextButton(
                    onClick = {
                        onSoundEffect(SoundEffect.TAP)
                        strokes = emptyList()
                        currentStroke = emptyList()
                    }
                ) {
                    Text("Clear drawing")
                }
            }
        }
    }
}

@Composable
private fun BuddyActivity(
    age: Int,
    initialStage: Int,
    berry: Color,
    onBack: () -> Unit,
    onSoundEffect: (SoundEffect) -> Unit,
    onStageCompleted: (LearningModule, Int) -> Unit
) {
    val allFacts = listOf(
        BuddyFact("Why is the sky blue?", "Sunlight has many colours. Air scatters blue light more than most other colours.", "🌤️"),
        BuddyFact("How do plants drink?", "Roots take in water from the soil and carry it up through the plant.", "🌱"),
        BuddyFact("Why do we see the Moon?", "The Moon reflects light from the Sun back toward Earth.", "🌙"),
        BuddyFact("What is a habitat?", "A habitat is the natural home where an animal or plant finds what it needs.", "🏡"),
        BuddyFact("Why do we have day and night?", "Earth spins. The side facing the Sun has daytime, while the other side has nighttime.", "🌍"),
        BuddyFact("How many planets orbit the Sun?", "There are eight planets in our Solar System.", "🪐"),
        BuddyFact("What is the largest animal on Earth?", "The blue whale is the largest animal known to live on Earth.", "🐋"),
        BuddyFact("Why do bees visit flowers?", "Bees collect nectar and pollen. As they visit, they help many flowers make seeds.", "🐝"),
        BuddyFact("How does a rainbow form?", "Sunlight bends and separates into colours as it passes through raindrops.", "🌈"),
        BuddyFact("How many sides does a triangle have?", "A triangle has three sides and three corners.", "🔺"),
        BuddyFact("Why does ice float on water?", "Frozen water is less dense than liquid water, so ice floats.", "🧊"),
        BuddyFact("What helps a seed start growing?", "Many seeds need water, air, and the right warmth to begin growing.", "🌰")
    )
    val facts = when (ageBandFor(age)) {
        AgeBand.EARLY -> listOf(allFacts[1], allFacts[2], allFacts[3], allFacts[8], allFacts[9], allFacts[11])
        AgeBand.FOUNDATIONS -> allFacts.take(10)
        AgeBand.DEVELOPING, AgeBand.ADVANCING -> allFacts
    }
    var shuffledFacts by remember { mutableStateOf(facts.shuffled()) }
    var stage by remember { mutableIntStateOf(initialStage) }
    var currentFactIndex by remember { mutableIntStateOf(0) }
    var answerRevealed by remember { mutableStateOf(false) }
    val fact = shuffledFacts[currentFactIndex]

    ActivityPage("Learning Buddy", berry, onBack, onSoundEffect) {
        Text("A new offline fun fact each round.")
        Text("Stage $stage • Question ${currentFactIndex + 1} of ${shuffledFacts.size}")
        Card(
            colors = CardDefaults.cardColors(containerColor = berry.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth().animateContentSize()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedLearningIllustration(fact.illustration, fontSize = 68)
                Text(fact.question, fontWeight = FontWeight.Bold)
                AnimatedVisibility(
                    visible = answerRevealed,
                    enter = fadeIn(tween(180)) + expandVertically(),
                    exit = fadeOut(tween(120)) + shrinkVertically()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedLearningIllustration("💡", fontSize = 40)
                        Text(fact.answer)
                    }
                }
            }
        }

        if (!answerRevealed) {
            Button(
                onClick = {
                    onSoundEffect(SoundEffect.TAP)
                    answerRevealed = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = berry)
            ) {
                Text("Show answer")
            }
        } else {
            Button(
                onClick = {
                    onSoundEffect(SoundEffect.TAP)
                    if (currentFactIndex == shuffledFacts.lastIndex) {
                        val nextFacts = facts.shuffled()
                        shuffledFacts =
                            if (nextFacts.first() == shuffledFacts.last()) {
                                nextFacts.drop(1) + nextFacts.first()
                            } else {
                                nextFacts
                            }
                        currentFactIndex = 0
                        onStageCompleted(LearningModule.BUDDY, stage)
                        stage += 1
                    } else {
                        currentFactIndex += 1
                    }
                    answerRevealed = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = berry)
            ) {
                Text(if (currentFactIndex == shuffledFacts.lastIndex) "New round" else "Next question")
            }
        }
    }
}

private data class BuddyFact(
    val question: String,
    val answer: String,
    val illustration: String
)

@Composable
private fun MazeActivity(
    age: Int,
    initialStage: Int,
    berry: Color,
    onBack: () -> Unit,
    onSoundEffect: (SoundEffect) -> Unit,
    onStageCompleted: (LearningModule, Int) -> Unit
) {
    var stage by remember { mutableIntStateOf(initialStage) }
    val roomCount = mazeRoomCountFor(age, stage)
    var maze by remember { mutableStateOf(generateMaze(roomCount = roomCount)) }
    var playerX by remember { mutableIntStateOf(0) }
    var playerY by remember { mutableIntStateOf(0) }
    var foundGoal by remember { mutableStateOf(false) }

    val move: (Int, Int) -> Unit = { dx, dy ->
        val nextX = playerX + dx
        val nextY = playerY + dy
        if (!foundGoal && nextX in maze[0].indices && nextY in maze.indices && maze[nextY][nextX] != '#') {
            playerX = nextX
            playerY = nextY
            if (maze[nextY][nextX] == 'G') {
                foundGoal = true
                onStageCompleted(LearningModule.MAZE, stage)
                onSoundEffect(SoundEffect.CORRECT)
            } else {
                onSoundEffect(SoundEffect.TAP)
            }
        } else {
            onSoundEffect(SoundEffect.TAP)
        }
    }

    ActivityPage("Star Maze", berry, onBack, onSoundEffect) {
        Text(
            if (foundGoal) "Stage $stage complete! You found the star!"
            else "Stage $stage • Guide the explorer to the star."
        )
        val cellSize = when {
            maze.size <= 5 -> 38.dp
            maze.size <= 7 -> 32.dp
            maze.size <= 9 -> 28.dp
            maze.size <= 11 -> 24.dp
            else -> 20.dp
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            maze.forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    row.forEachIndexed { columnIndex, cell ->
                        val isPlayer = playerX == columnIndex && playerY == rowIndex
                        val cellColor = when {
                            isPlayer -> Color(0xFFFFD166)
                            cell == '#' -> Color(0xFF7253B7)
                            cell == 'G' -> Color(0xFF62C9A5)
                            else -> Color(0xFFDCEBCF)
                        }
                        Box(
                            modifier = Modifier.size(cellSize).background(cellColor, RoundedCornerShape(6.dp))
                        ) {
                            Canvas(Modifier.fillMaxSize()) {
                                val scaleX = size.width / 40f
                                val scaleY = size.height / 40f
                                withTransform({
                                    scale(scaleX, scaleY, pivot = Offset.Zero)
                                }) {
                                    when {
                                        cell == '#' -> drawMazeWall()
                                        isPlayer -> drawMazeExplorer(foundGoal)
                                        cell == 'G' -> drawMazeGoal()
                                        else -> drawMazeFloor(columnIndex, rowIndex)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { move(0, -1) }, enabled = !foundGoal) { Text("↑") }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { move(-1, 0) }, enabled = !foundGoal) { Text("←") }
                Button(onClick = { move(0, 1) }, enabled = !foundGoal) { Text("↓") }
                Button(onClick = { move(1, 0) }, enabled = !foundGoal) { Text("→") }
            }
        }
        Button(
            onClick = {
                onSoundEffect(SoundEffect.TAP)
                if (foundGoal) {
                    val nextStage = stage + 1
                    maze = generateDifferentMaze(
                        maze,
                        roomCount = mazeRoomCountFor(age, nextStage)
                    )
                    stage = nextStage
                }
                playerX = 0
                playerY = 0
                foundGoal = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = berry)
        ) {
            Text(if (foundGoal) "Next maze" else "Restart maze")
        }
    }
}

private fun DrawScope.drawMazeWall() {
    val outline = Color(0xFF493A64)
    drawRoundRect(Color(0xFF9276B8), Offset(2f, 3f), androidx.compose.ui.geometry.Size(36f, 35f), androidx.compose.ui.geometry.CornerRadius(6f))
    drawRoundRect(outline, Offset(2f, 3f), androidx.compose.ui.geometry.Size(36f, 35f), androidx.compose.ui.geometry.CornerRadius(6f), style = Stroke(2f))
    drawLine(Color(0xFFC8B6DD), Offset(5f, 8f), Offset(34f, 8f), 2f, StrokeCap.Round)
    drawLine(outline.copy(alpha = 0.65f), Offset(5f, 21f), Offset(34f, 21f), 1.5f)
    drawLine(Color(0xFF594A70), Offset(18f, 9f), Offset(18f, 21f), 1.5f)
    drawLine(Color(0xFF594A70), Offset(28f, 22f), Offset(28f, 34f), 1.5f)
}

private fun DrawScope.drawMazeExplorer(foundGoal: Boolean) {
    val outline = Color(0xFF34424A)
    drawOval(Color(0xFF738596).copy(alpha = 0.3f), Offset(7f, 31f), androidx.compose.ui.geometry.Size(27f, 5f))
    drawRoundRect(Color(0xFF458BC2), Offset(12f, 17f), androidx.compose.ui.geometry.Size(17f, 17f), androidx.compose.ui.geometry.CornerRadius(6f))
    drawRoundRect(outline, Offset(12f, 17f), androidx.compose.ui.geometry.Size(17f, 17f), androidx.compose.ui.geometry.CornerRadius(6f), style = Stroke(2f))
    drawCircle(Color(0xFFFFD3A5), 8f, Offset(20.5f, 12f))
    drawCircle(outline, 8f, Offset(20.5f, 12f), style = Stroke(1.7f))
    drawArc(Color(0xFF5C4635), 180f, 180f, true, Offset(12f, 4f), androidx.compose.ui.geometry.Size(17f, 11f))
    drawLine(outline, Offset(15f, 31f), Offset(12f, 37f), 3f, StrokeCap.Round)
    drawLine(outline, Offset(26f, 31f), Offset(29f, 37f), 3f, StrokeCap.Round)
    if (foundGoal) drawMazeGoal()
}

private fun DrawScope.drawMazeGoal() {
    val star = Path().apply {
        moveTo(20f, 4f)
        lineTo(24f, 15f)
        lineTo(36f, 15f)
        lineTo(27f, 22f)
        lineTo(31f, 35f)
        lineTo(20f, 27f)
        lineTo(9f, 35f)
        lineTo(13f, 22f)
        lineTo(4f, 15f)
        lineTo(16f, 15f)
        close()
    }
    drawPath(star, Color(0xFFFFD43B))
    drawPath(star, Color(0xFF966E22), style = Stroke(2f))
}

private fun DrawScope.drawMazeFloor(column: Int, row: Int) {
    val detailX = 7f + (column * 13 % 25)
    val detailY = 8f + (row * 11 % 22)
    drawLine(Color(0xFF85AF76), Offset(detailX, detailY), Offset(detailX - 3f, detailY - 5f), 1.5f, StrokeCap.Round)
    drawLine(Color(0xFF85AF76), Offset(detailX, detailY), Offset(detailX + 4f, detailY - 4f), 1.5f, StrokeCap.Round)
    drawCircle(Color(0xFFBDD4A9), 1.5f, Offset(31f - detailX / 2f, 33f - detailY / 2f))
}

internal enum class ColoringTheme(
    val thumbnail: String,
    val titles: List<String>
) {
    DINO(
        "🦕",
        listOf(
            "Dinosaur Valley", "Friendly T-Rex", "Long-Neck Picnic", "Dino Egg Garden",
            "Pterodactyl Sky", "Triceratops Meadow", "Volcano Friends", "Tiny Dino Parade",
            "Prehistoric River", "Dinosaur Night"
        )
    ),
    UNDERWATER(
        "🐠",
        listOf(
            "Coral Reef Friends", "Dolphin Bay", "Octopus Garden", "Seashell Shore",
            "Turtle Lagoon", "Mermaid Cove", "Fish School", "Underwater Castle",
            "Whale Song", "Starfish Beach"
        )
    ),
    SAFARI(
        "🦒",
        listOf(
            "Sunny Safari", "Giraffe Grove", "Elephant Waterhole", "Zebra Plains",
            "Lion's Meadow", "Monkey Treehouse", "Safari Sunrise", "Hippo Lagoon",
            "Cheetah Trail", "Baobab Friends"
        )
    ),
    GARDEN(
        "🌼",
        listOf(
            "Butterfly Garden", "Flower Patch", "Ladybug Picnic", "Sunflower Corner",
            "Busy Bee Garden", "Rainbow Greenhouse", "Garden Pond", "Tulip Path",
            "Snail's Garden", "Morning Blooms"
        )
    ),
    FOREST(
        "🦉",
        listOf(
            "Forest Friends", "Owl's Treehouse", "Mushroom Trail", "Squirrel's Picnic",
            "Fox in the Woods", "Woodland Stream", "Leafy Hideaway", "Deer Meadow",
            "Cozy Forest Cabin", "Firefly Night"
        )
    ),
    FARM(
        "🐮",
        listOf(
            "Farmyard Friends", "Barnyard Morning", "Cow's Meadow", "Little Red Barn",
            "Tractor on the Trail", "Duck Pond Farm", "Horse Stable", "Sheep's Clover Field",
            "Apple Orchard", "Harvest Day"
        )
    ),
    SPACE(
        "🚀",
        listOf(
            "Moon Garden", "Rocket Journey", "Friendly Aliens", "Planet Parade",
            "Astronaut's Picnic", "Saturn's Rings", "Comet Crossing", "Space Station",
            "Little Moon Rover", "Starry Night Flight"
        )
    ),
    FANTASY(
        "🏰",
        listOf(
            "Dragon's Castle", "Fairy Tale Meadow", "Castle Garden", "Unicorn Valley",
            "Friendly Dragon", "Wizard's Tower", "Rainbow Castle", "Enchanted Forest",
            "Dragon Egg Cave", "Magic Mushroom Grove"
        )
    ),
    ARCTIC(
        "🐧",
        listOf(
            "Penguin's Snowy Day", "Polar Bear Bay", "Igloo Village", "Arctic Fox Trail",
            "Seal's Iceberg", "Snowflake Festival", "Northern Lights", "Cozy Winter Camp",
            "Whale on Ice", "Snowy Owl Night"
        )
    ),
    BUGS(
        "🐝",
        listOf(
            "Butterfly Meadow", "Bee's Flower Shop", "Ladybug Lane", "Tiny Beetle Garden",
            "Busy Bug Picnic", "Dragonfly Pond", "Caterpillar's Leaf", "Ant Hill Adventure",
            "Moth Moonlight", "Garden Friends"
        )
    )
}

internal data class ColoringScene(
    val id: Int,
    val title: String,
    val thumbnail: String,
    val theme: ColoringTheme,
    val variation: Int,
    val regions: List<ColoringRegion>
)

private fun coloringRegionsFor(theme: ColoringTheme): List<ColoringRegion> = when (theme) {
    ColoringTheme.DINO -> listOf(
        ColoringRegion("sun", "Sun", 55f, 48f, 42f),
        ColoringRegion("cloud", "Cloud", 305f, 62f, 58f),
        ColoringRegion("mountains", "Mountains", 340f, 156f, 64f),
        ColoringRegion("body", "Dinosaur", 190f, 211f, 58f),
        ColoringRegion("head", "Dinosaur head", 251f, 158f, 39f),
        ColoringRegion("tail", "Dinosaur tail", 119f, 220f, 38f),
        ColoringRegion("spikes", "Dinosaur spikes", 195f, 153f, 47f),
        ColoringRegion("flowers", "Flowers", 61f, 255f, 38f)
    )
    ColoringTheme.UNDERWATER -> listOf(
        ColoringRegion("bubbles", "Bubbles", 54f, 48f, 42f),
        ColoringRegion("coral", "Coral", 73f, 224f, 48f),
        ColoringRegion("fish", "Fish", 194f, 145f, 45f),
        ColoringRegion("tail", "Fish tail", 142f, 145f, 28f),
        ColoringRegion("fin", "Fish fin", 205f, 108f, 28f),
        ColoringRegion("octopus", "Octopus", 296f, 184f, 48f),
        ColoringRegion("seaweed", "Seaweed", 350f, 252f, 45f),
        ColoringRegion("shell", "Seashell", 157f, 246f, 34f)
    )
    ColoringTheme.SAFARI -> listOf(
        ColoringRegion("sun", "Sun", 55f, 48f, 42f),
        ColoringRegion("cloud", "Cloud", 306f, 58f, 58f),
        ColoringRegion("tree", "Tree", 66f, 163f, 52f),
        ColoringRegion("trunk", "Tree trunk", 70f, 222f, 27f),
        ColoringRegion("body", "Giraffe", 199f, 205f, 51f),
        ColoringRegion("neck", "Giraffe neck", 226f, 155f, 27f),
        ColoringRegion("head", "Giraffe head", 244f, 117f, 28f),
        ColoringRegion("spots", "Giraffe spots", 193f, 203f, 42f)
    )
    ColoringTheme.GARDEN -> listOf(
        ColoringRegion("sun", "Sun", 55f, 48f, 35f),
        ColoringRegion("cloud", "Cloud", 310f, 55f, 55f),
        ColoringRegion("flower", "Big flower", 190f, 159f, 48f),
        ColoringRegion("butterfly", "Butterfly", 302f, 145f, 40f),
        ColoringRegion("leaves", "Leaves", 72f, 223f, 48f),
        ColoringRegion("snail", "Snail", 126f, 248f, 40f),
        ColoringRegion("pot", "Flower pot", 211f, 252f, 38f),
        ColoringRegion("smallFlowers", "Little flowers", 335f, 247f, 40f)
    )
    ColoringTheme.FOREST -> listOf(
        ColoringRegion("sun", "Sun", 57f, 46f, 35f),
        ColoringRegion("tree", "Tree top", 76f, 141f, 48f),
        ColoringRegion("trunk", "Tree trunk", 76f, 220f, 34f),
        ColoringRegion("owl", "Owl", 225f, 139f, 42f),
        ColoringRegion("mushrooms", "Mushrooms", 121f, 259f, 40f),
        ColoringRegion("squirrel", "Squirrel", 321f, 203f, 46f),
        ColoringRegion("leaves", "Leaves", 300f, 74f, 42f),
        ColoringRegion("stream", "Forest stream", 218f, 268f, 42f)
    )
    ColoringTheme.FARM -> listOf(
        ColoringRegion("sun", "Sun", 54f, 46f, 34f),
        ColoringRegion("cloud", "Cloud", 315f, 55f, 50f),
        ColoringRegion("barn", "Barn", 105f, 185f, 62f),
        ColoringRegion("roof", "Barn roof", 106f, 112f, 53f),
        ColoringRegion("door", "Barn door", 106f, 221f, 34f),
        ColoringRegion("cow", "Cow", 263f, 207f, 55f),
        ColoringRegion("fence", "Fence", 340f, 258f, 45f),
        ColoringRegion("hay", "Hay bale", 188f, 261f, 35f)
    )
    ColoringTheme.SPACE -> listOf(
        ColoringRegion("moon", "Moon", 61f, 60f, 40f),
        ColoringRegion("planet", "Planet", 333f, 78f, 44f),
        ColoringRegion("rocket", "Rocket", 198f, 158f, 55f),
        ColoringRegion("window", "Rocket window", 198f, 151f, 22f),
        ColoringRegion("flame", "Rocket flames", 199f, 241f, 34f),
        ColoringRegion("alien", "Friendly alien", 309f, 222f, 43f),
        ColoringRegion("stars", "Stars", 87f, 193f, 47f),
        ColoringRegion("comet", "Comet", 108f, 265f, 38f)
    )
    ColoringTheme.FANTASY -> listOf(
        ColoringRegion("cloud", "Cloud", 62f, 54f, 42f),
        ColoringRegion("castle", "Castle", 201f, 177f, 63f),
        ColoringRegion("towers", "Castle towers", 201f, 104f, 58f),
        ColoringRegion("dragon", "Dragon", 319f, 213f, 48f),
        ColoringRegion("wings", "Dragon wings", 311f, 168f, 45f),
        ColoringRegion("stars", "Magic stars", 67f, 161f, 38f),
        ColoringRegion("tree", "Magic tree", 84f, 251f, 43f),
        ColoringRegion("moon", "Moon", 342f, 59f, 32f)
    )
    ColoringTheme.ARCTIC -> listOf(
        ColoringRegion("sun", "Winter sun", 53f, 47f, 32f),
        ColoringRegion("igloo", "Igloo", 106f, 203f, 60f),
        ColoringRegion("penguin", "Penguin", 248f, 211f, 47f),
        ColoringRegion("fish", "Fish", 326f, 260f, 35f),
        ColoringRegion("snowflakes", "Snowflakes", 195f, 57f, 48f),
        ColoringRegion("iceberg", "Iceberg", 321f, 142f, 55f),
        ColoringRegion("scarf", "Penguin scarf", 249f, 216f, 28f),
        ColoringRegion("ground", "Snowy ground", 171f, 280f, 52f)
    )
    ColoringTheme.BUGS -> listOf(
        ColoringRegion("sun", "Sun", 53f, 46f, 32f),
        ColoringRegion("flower", "Flower", 104f, 193f, 48f),
        ColoringRegion("bee", "Bee", 221f, 111f, 38f),
        ColoringRegion("butterfly", "Butterfly", 311f, 153f, 43f),
        ColoringRegion("ladybug", "Ladybug", 236f, 239f, 43f),
        ColoringRegion("leaves", "Leaves", 71f, 259f, 44f),
        ColoringRegion("cloud", "Cloud", 324f, 55f, 45f),
        ColoringRegion("caterpillar", "Caterpillar", 333f, 256f, 43f)
    )
}

internal val coloringScenes = ColoringTheme.values().flatMapIndexed { themeIndex, theme ->
    theme.titles.mapIndexed { variation, title ->
        ColoringScene(
            id = themeIndex * theme.titles.size + variation,
            title = title,
            thumbnail = theme.thumbnail,
            theme = theme,
            variation = variation,
            regions = coloringRegionsFor(theme)
        )
    }
}

internal data class ColoringRegion(
    val id: String,
    val label: String,
    val centerX: Float,
    val centerY: Float,
    val hitRadius: Float
)

@Composable
private fun ColoringActivity(
    berry: Color,
    onBack: () -> Unit,
    onSoundEffect: (SoundEffect) -> Unit
) {
    val palette = listOf(
        Color(0xFFE76F51),
        Color(0xFFF4A261),
        Color(0xFFE9C46A),
        Color(0xFF2A9D8F),
        Color(0xFF48CAE4),
        Color(0xFF4D96FF),
        Color(0xFF9B5DE5),
        Color(0xFFF15BB5)
    )
    var selectedColor by remember { mutableStateOf(palette.first()) }
    var selectedScene by remember { mutableStateOf(coloringScenes.first()) }
    var sceneFills by remember { mutableStateOf<Map<Int, Map<String, Color>>>(emptyMap()) }
    val fills = sceneFills[selectedScene.id].orEmpty()
    val completedRegions = selectedScene.regions.count { it.id in fills }
    val sceneComplete = completedRegions == selectedScene.regions.size

    ActivityPage("Coloring Studio", berry, onBack, onSoundEffect) {
        Text("Choose from 100 original coloring pages. Your colors stay when you switch pictures.")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(coloringScenes, key = { it.id }) { scene ->
                val isSelected = scene == selectedScene
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) berry.copy(alpha = 0.2f) else Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(width = 164.dp, height = 72.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) berry else berry.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            onSoundEffect(SoundEffect.TAP)
                            selectedScene = scene
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Canvas(Modifier.size(width = 52.dp, height = 52.dp)) {
                            drawColoringScene(scene, emptyMap())
                        }
                        Text(
                            scene.title,
                            modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        Text(
            "${selectedScene.thumbnail} Picture ${selectedScene.id + 1} of ${coloringScenes.size}: " +
                "${selectedScene.title} • $completedRegions/${selectedScene.regions.size} colored",
            fontWeight = FontWeight.Bold,
            color = berry
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color(0xFFFFFEF7), RoundedCornerShape(20.dp))
                .border(1.dp, berry.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                .pointerInput(selectedScene, selectedColor) {
                    detectTapGestures { tap ->
                        val x = tap.x / size.width * 400f
                        val y = tap.y / size.height * 300f
                        val region = selectedScene.regions
                            .map { candidate ->
                                val dx = x - candidate.centerX
                                val dy = y - candidate.centerY
                                candidate to (dx * dx + dy * dy)
                            }
                            .filter { (candidate, distance) ->
                                distance <= candidate.hitRadius * candidate.hitRadius
                            }
                            .minByOrNull { it.second }
                            ?.first
                        if (region != null) {
                            onSoundEffect(SoundEffect.TAP)
                            val updatedSceneFills = fills + (region.id to selectedColor)
                            sceneFills = sceneFills + (selectedScene.id to updatedSceneFills)
                        }
                    }
                }
        ) {
            drawColoringScene(selectedScene, fills)
        }
        Text(
            if (sceneComplete) "Beautiful work! You finished this picture. 🎉"
            else "Selected color: ${palette.indexOf(selectedColor) + 1} • Tap an outlined part to fill it."
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            palette.forEachIndexed { index, color ->
                Button(
                    onClick = {
                        onSoundEffect(SoundEffect.TAP)
                        selectedColor = color
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) {
                    if (selectedColor == color) Text("✓", color = Color.White)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    onSoundEffect(SoundEffect.TAP)
                    sceneFills = sceneFills + (selectedScene.id to emptyMap())
                },
                colors = ButtonDefaults.buttonColors(containerColor = berry.copy(alpha = 0.75f))
            ) {
                Text("Clear picture")
            }
            if (sceneComplete) {
                Button(
                    onClick = {
                        onSoundEffect(SoundEffect.TAP)
                        val nextIndex = (selectedScene.id + 1) % coloringScenes.size
                        selectedScene = coloringScenes[nextIndex]
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = berry)
                ) {
                    Text("Next picture")
                }
            }
        }
    }
}

private fun DrawScope.drawColoringScene(
    scene: ColoringScene,
    fills: Map<String, Color>
) {
    val scaleX = size.width / 400f
    val scaleY = size.height / 300f
    val outline = Color(0xFF242424)
    withTransform({
        scale(scaleX, scaleY, pivot = Offset.Zero)
    }) {
        fun color(id: String) = fills[id] ?: Color.White
        fun outlined(path: Path, fill: Color) {
            drawPath(path, fill)
            drawPath(path, outline, style = Stroke(width = 3f))
        }

        drawRect(Color.White, size = androidx.compose.ui.geometry.Size(400f, 300f))
        when (scene.theme) {
            ColoringTheme.DINO -> {
                drawCircle(color("sun"), 24f, Offset(55f, 48f))
                drawCircle(outline, 24f, Offset(55f, 48f), style = Stroke(3f))

                listOf(Offset(282f, 65f), Offset(306f, 51f), Offset(330f, 66f)).forEach {
                    drawCircle(color("cloud"), 22f, it)
                    drawCircle(outline, 22f, it, style = Stroke(3f))
                }
                val mountains = Path().apply {
                    moveTo(239f, 189f)
                    lineTo(289f, 112f)
                    lineTo(329f, 169f)
                    lineTo(365f, 121f)
                    lineTo(410f, 189f)
                    close()
                }
                outlined(mountains, color("mountains"))

                val dinoTail = Path().apply {
                    moveTo(142f, 206f)
                    lineTo(99f, 185f)
                    lineTo(113f, 224f)
                    lineTo(151f, 232f)
                    close()
                }
                outlined(dinoTail, color("tail"))
                drawOval(color("body"), Offset(132f, 177f), androidx.compose.ui.geometry.Size(123f, 76f))
                drawOval(outline, Offset(132f, 177f), androidx.compose.ui.geometry.Size(123f, 76f), style = Stroke(3f))
                drawRoundRect(
                    color("head"),
                    Offset(222f, 133f),
                    androidx.compose.ui.geometry.Size(55f, 70f),
                    androidx.compose.ui.geometry.CornerRadius(25f)
                )
                drawRoundRect(
                    outline,
                    Offset(222f, 133f),
                    androidx.compose.ui.geometry.Size(55f, 70f),
                    androidx.compose.ui.geometry.CornerRadius(25f),
                    style = Stroke(3f)
                )
                repeat(4) { index ->
                    val x = 164f + index * 19f
                    val spike = Path().apply {
                        moveTo(x, 183f)
                        lineTo(x + 9f, 160f)
                        lineTo(x + 18f, 183f)
                        close()
                    }
                    outlined(spike, color("spikes"))
                }
                repeat(2) { index ->
                    drawRoundRect(
                        color("body"),
                        Offset(163f + index * 51f, 231f),
                        androidx.compose.ui.geometry.Size(19f, 39f),
                        androidx.compose.ui.geometry.CornerRadius(8f)
                    )
                    drawRoundRect(
                        outline,
                        Offset(163f + index * 51f, 231f),
                        androidx.compose.ui.geometry.Size(19f, 39f),
                        androidx.compose.ui.geometry.CornerRadius(8f),
                        style = Stroke(3f)
                    )
                }
                drawCircle(Color.White, 7f, Offset(263f, 151f))
                drawCircle(outline, 3f, Offset(265f, 151f))
                drawCircle(outline, 2f, Offset(270f, 176f))
                repeat(3) { index ->
                    val flowerX = 39f + index * 23f
                    drawCircle(color("flowers"), 7f, Offset(flowerX, 244f))
                    drawCircle(color("flowers"), 7f, Offset(flowerX + 8f, 251f))
                    drawCircle(outline, 2.5f, Offset(flowerX + 4f, 248f))
                    drawLine(outline, Offset(flowerX + 4f, 255f), Offset(flowerX + 4f, 278f), 3f)
                }
            }
            ColoringTheme.UNDERWATER -> {
                listOf(Offset(47f, 43f), Offset(67f, 66f), Offset(31f, 78f)).forEach {
                    drawCircle(color("bubbles"), 9f, it)
                    drawCircle(outline, 9f, it, style = Stroke(2.5f))
                }
                val fishTail = Path().apply {
                    moveTo(155f, 144f)
                    lineTo(119f, 119f)
                    lineTo(119f, 169f)
                    close()
                }
                outlined(fishTail, color("tail"))
                drawOval(color("fish"), Offset(149f, 111f), androidx.compose.ui.geometry.Size(105f, 68f))
                drawOval(outline, Offset(149f, 111f), androidx.compose.ui.geometry.Size(105f, 68f), style = Stroke(3f))
                val fin = Path().apply {
                    moveTo(180f, 117f)
                    lineTo(202f, 83f)
                    lineTo(220f, 119f)
                    close()
                }
                outlined(fin, color("fin"))
                drawCircle(Color.White, 8f, Offset(228f, 135f))
                drawCircle(outline, 3f, Offset(230f, 135f))
                repeat(3) { index ->
                    val x = 49f + index * 18f
                    drawLine(color("coral"), Offset(x, 274f), Offset(x + 4f, 225f), 7f, StrokeCap.Round)
                    drawLine(outline, Offset(x, 274f), Offset(x + 4f, 225f), 2f, StrokeCap.Round)
                    drawLine(outline, Offset(x + 3f, 250f), Offset(x - 7f, 238f), 2f, StrokeCap.Round)
                    drawCircle(outline, 2f, Offset(x + 4f, 225f))
                }
                repeat(4) { index ->
                    val x = 270f + index * 25f
                    drawLine(color("seaweed"), Offset(x, 285f), Offset(x - 5f, 230f - index % 2 * 15f), 7f, StrokeCap.Round)
                    drawLine(outline, Offset(x, 285f), Offset(x - 5f, 230f - index % 2 * 15f), 2f, StrokeCap.Round)
                }
                drawCircle(color("octopus"), 31f, Offset(309f, 190f))
                drawCircle(outline, 31f, Offset(309f, 190f), style = Stroke(3f))
                repeat(4) { index ->
                    val x = 289f + index * 13f
                    val tentacle = Path().apply {
                        moveTo(x, 209f)
                        quadraticTo(x - 8f, 235f, x + 5f, 249f)
                    }
                    drawPath(tentacle, color("octopus"), style = Stroke(8f, cap = StrokeCap.Round))
                    drawPath(tentacle, outline, style = Stroke(2f, cap = StrokeCap.Round))
                }
                drawCircle(Color.White, 3f, Offset(300f, 185f))
                drawCircle(Color.White, 3f, Offset(318f, 185f))
                drawCircle(outline, 1.5f, Offset(301f, 185f))
                drawCircle(outline, 1.5f, Offset(319f, 185f))
                val shell = Path().apply {
                    moveTo(128f, 276f)
                    quadraticTo(158f, 224f, 187f, 276f)
                    close()
                }
                outlined(shell, color("shell"))
                repeat(3) { index ->
                    drawLine(outline, Offset(158f, 271f), Offset(139f + index * 16f, 242f), 2f)
                }
            }
            ColoringTheme.SAFARI -> {
                drawCircle(color("sun"), 25f, Offset(55f, 48f))
                drawCircle(outline, 25f, Offset(55f, 48f), style = Stroke(3f))
                listOf(Offset(281f, 60f), Offset(306f, 45f), Offset(331f, 61f)).forEach {
                    drawCircle(color("cloud"), 22f, it)
                    drawCircle(outline, 22f, it, style = Stroke(3f))
                }
                drawRoundRect(
                    color("trunk"),
                    Offset(53f, 164f),
                    androidx.compose.ui.geometry.Size(34f, 114f),
                    androidx.compose.ui.geometry.CornerRadius(8f)
                )
                drawRoundRect(
                    outline,
                    Offset(53f, 164f),
                    androidx.compose.ui.geometry.Size(34f, 114f),
                    androidx.compose.ui.geometry.CornerRadius(8f),
                    style = Stroke(3f)
                )
                drawCircle(color("tree"), 49f, Offset(69f, 143f))
                drawCircle(outline, 49f, Offset(69f, 143f), style = Stroke(3f))

                drawOval(color("body"), Offset(151f, 179f), androidx.compose.ui.geometry.Size(100f, 68f))
                drawOval(outline, Offset(151f, 179f), androidx.compose.ui.geometry.Size(100f, 68f), style = Stroke(3f))
                drawRoundRect(
                    color("neck"),
                    Offset(208f, 125f),
                    androidx.compose.ui.geometry.Size(31f, 79f),
                    androidx.compose.ui.geometry.CornerRadius(13f)
                )
                drawRoundRect(
                    outline,
                    Offset(208f, 125f),
                    androidx.compose.ui.geometry.Size(31f, 79f),
                    androidx.compose.ui.geometry.CornerRadius(13f),
                    style = Stroke(3f)
                )
                drawOval(color("head"), Offset(222f, 101f), androidx.compose.ui.geometry.Size(47f, 39f))
                drawOval(outline, Offset(222f, 101f), androidx.compose.ui.geometry.Size(47f, 39f), style = Stroke(3f))
                repeat(2) { index ->
                    drawCircle(color("head"), 7f, Offset(235f + index * 23f, 102f))
                    drawCircle(outline, 2f, Offset(235f + index * 23f, 102f))
                }
                repeat(5) { index ->
                    val x = 166f + index * 15f
                    val y = 193f + (index % 2) * 25f
                    drawCircle(color("spots"), 7f, Offset(x, y))
                    drawCircle(outline, 1.5f, Offset(x, y))
                }
                repeat(3) { index ->
                    drawRoundRect(
                        color("body"),
                        Offset(167f + index * 31f, 230f),
                        androidx.compose.ui.geometry.Size(12f, 44f),
                        androidx.compose.ui.geometry.CornerRadius(6f)
                    )
                    drawRoundRect(
                        outline,
                        Offset(167f + index * 31f, 230f),
                        androidx.compose.ui.geometry.Size(12f, 44f),
                        androidx.compose.ui.geometry.CornerRadius(6f),
                        style = Stroke(2.5f)
                    )
                }
            }
            ColoringTheme.GARDEN -> {
                drawCircle(color("sun"), 22f, Offset(55f, 48f))
                drawCircle(outline, 22f, Offset(55f, 48f), style = Stroke(2.5f))
                drawColoringCloud(outline, color("cloud"), Offset(307f, 58f))

                drawLine(outline, Offset(190f, 182f), Offset(190f, 267f), 4f, StrokeCap.Round)
                drawColoringLeaf(outline, color("leaves"), 154f, 219f, 185f, 205f, 184f, 232f)
                drawColoringLeaf(outline, color("leaves"), 194f, 238f, 226f, 218f, 219f, 244f)
                val flowerPetals = listOf(
                    Offset(190f, 129f), Offset(211f, 137f), Offset(220f, 158f),
                    Offset(211f, 179f), Offset(190f, 187f), Offset(169f, 179f),
                    Offset(160f, 158f), Offset(169f, 137f)
                )
                flowerPetals.forEach {
                    drawCircle(color("flower"), 13f, it)
                    drawCircle(outline, 13f, it, style = Stroke(2.5f))
                }
                drawCircle(color("flower"), 15f, Offset(190f, 158f))
                drawCircle(outline, 15f, Offset(190f, 158f), style = Stroke(2.5f))

                drawOval(color("butterfly"), Offset(280f, 128f), androidx.compose.ui.geometry.Size(23f, 30f))
                drawOval(outline, Offset(280f, 128f), androidx.compose.ui.geometry.Size(23f, 30f), style = Stroke(2.5f))
                drawOval(color("butterfly"), Offset(302f, 128f), androidx.compose.ui.geometry.Size(23f, 30f))
                drawOval(outline, Offset(302f, 128f), androidx.compose.ui.geometry.Size(23f, 30f), style = Stroke(2.5f))
                drawLine(outline, Offset(302f, 137f), Offset(302f, 166f), 4f, StrokeCap.Round)
                drawLine(outline, Offset(302f, 137f), Offset(294f, 126f), 2f)
                drawLine(outline, Offset(302f, 137f), Offset(310f, 126f), 2f)

                val snail = Path().apply {
                    moveTo(88f, 265f)
                    quadraticTo(92f, 237f, 117f, 240f)
                    quadraticTo(139f, 243f, 137f, 266f)
                    close()
                }
                outlined(snail, color("snail"))
                drawCircle(color("snail"), 13f, Offset(118f, 244f))
                drawCircle(outline, 13f, Offset(118f, 244f), style = Stroke(2.5f))
                drawLine(outline, Offset(125f, 238f), Offset(127f, 229f), 2f)
                drawLine(outline, Offset(131f, 239f), Offset(136f, 231f), 2f)
                drawCircle(outline, 2f, Offset(127f, 229f))
                drawCircle(outline, 2f, Offset(136f, 231f))

                val pot = Path().apply {
                    moveTo(199f, 243f)
                    lineTo(232f, 243f)
                    lineTo(226f, 278f)
                    lineTo(205f, 278f)
                    close()
                }
                outlined(pot, color("pot"))
                drawLine(outline, Offset(196f, 243f), Offset(235f, 243f), 4f)
                repeat(3) { index ->
                    val x = 299f + index * 24f
                    drawLine(outline, Offset(x, 278f), Offset(x + 2f, 246f), 3f)
                    drawCircle(color("smallFlowers"), 7f, Offset(x + 2f, 244f))
                    drawCircle(outline, 7f, Offset(x + 2f, 244f), style = Stroke(2f))
                }
            }
            ColoringTheme.FOREST -> {
                drawCircle(color("sun"), 20f, Offset(57f, 46f))
                drawCircle(outline, 20f, Offset(57f, 46f), style = Stroke(2.5f))
                drawRoundRect(
                    color("trunk"),
                    Offset(56f, 148f),
                    androidx.compose.ui.geometry.Size(39f, 125f),
                    androidx.compose.ui.geometry.CornerRadius(8f)
                )
                drawRoundRect(
                    outline,
                    Offset(56f, 148f),
                    androidx.compose.ui.geometry.Size(39f, 125f),
                    androidx.compose.ui.geometry.CornerRadius(8f),
                    style = Stroke(2.5f)
                )
                listOf(Offset(76f, 113f), Offset(48f, 143f), Offset(103f, 145f)).forEach {
                    drawCircle(color("tree"), 39f, it)
                    drawCircle(outline, 39f, it, style = Stroke(2.5f))
                }
                drawCircle(color("owl"), 35f, Offset(223f, 139f))
                drawCircle(outline, 35f, Offset(223f, 139f), style = Stroke(2.5f))
                drawOval(color("owl"), Offset(190f, 130f), androidx.compose.ui.geometry.Size(27f, 42f))
                drawOval(outline, Offset(190f, 130f), androidx.compose.ui.geometry.Size(27f, 42f), style = Stroke(2f))
                drawOval(color("owl"), Offset(229f, 130f), androidx.compose.ui.geometry.Size(27f, 42f))
                drawOval(outline, Offset(229f, 130f), androidx.compose.ui.geometry.Size(27f, 42f), style = Stroke(2f))
                drawCircle(Color.White, 9f, Offset(212f, 136f))
                drawCircle(Color.White, 9f, Offset(234f, 136f))
                drawCircle(outline, 3f, Offset(213f, 136f))
                drawCircle(outline, 3f, Offset(235f, 136f))
                val beak = Path().apply {
                    moveTo(218f, 147f)
                    lineTo(228f, 147f)
                    lineTo(223f, 155f)
                    close()
                }
                outlined(beak, color("owl"))

                repeat(2) { index ->
                    val x = 105f + index * 34f
                    val cap = Path().apply {
                        moveTo(x - 15f, 264f)
                        quadraticTo(x, 235f, x + 15f, 264f)
                        close()
                    }
                    outlined(cap, color("mushrooms"))
                    drawRoundRect(
                        Color.White,
                        Offset(x - 7f, 263f),
                        androidx.compose.ui.geometry.Size(14f, 18f),
                        androidx.compose.ui.geometry.CornerRadius(5f)
                    )
                    drawRoundRect(
                        outline,
                        Offset(x - 7f, 263f),
                        androidx.compose.ui.geometry.Size(14f, 18f),
                        androidx.compose.ui.geometry.CornerRadius(5f),
                        style = Stroke(2f)
                    )
                }
                drawCircle(color("squirrel"), 21f, Offset(319f, 217f))
                drawCircle(outline, 21f, Offset(319f, 217f), style = Stroke(2.5f))
                drawCircle(color("squirrel"), 12f, Offset(332f, 193f))
                drawCircle(outline, 12f, Offset(332f, 193f), style = Stroke(2.5f))
                val squirrelTail = Path().apply {
                    moveTo(304f, 217f)
                    cubicTo(274f, 206f, 285f, 253f, 312f, 239f)
                }
                drawPath(squirrelTail, color("squirrel"), style = Stroke(11f, cap = StrokeCap.Round))
                drawPath(squirrelTail, outline, style = Stroke(2.5f, cap = StrokeCap.Round))
                repeat(3) { index ->
                    val x = 270f + index * 32f
                    drawColoringLeaf(outline, color("leaves"), x, 78f, x + 22f, 62f, x + 17f, 88f)
                }
                val stream = Path().apply {
                    moveTo(169f, 280f)
                    cubicTo(194f, 262f, 213f, 294f, 239f, 276f)
                    cubicTo(255f, 264f, 273f, 279f, 291f, 279f)
                }
                drawPath(stream, color("stream"), style = Stroke(11f, cap = StrokeCap.Round))
                drawPath(stream, outline, style = Stroke(2.5f, cap = StrokeCap.Round))
            }
            ColoringTheme.FARM -> {
                drawCircle(color("sun"), 21f, Offset(54f, 46f))
                drawCircle(outline, 21f, Offset(54f, 46f), style = Stroke(2.5f))
                drawColoringCloud(outline, color("cloud"), Offset(315f, 55f))
                drawRect(color("barn"), Offset(59f, 130f), androidx.compose.ui.geometry.Size(93f, 111f))
                drawRect(outline, Offset(59f, 130f), androidx.compose.ui.geometry.Size(93f, 111f), style = Stroke(3f))
                val roof = Path().apply {
                    moveTo(47f, 134f)
                    lineTo(104f, 91f)
                    lineTo(164f, 134f)
                    close()
                }
                outlined(roof, color("roof"))
                val barnDoor = Path().apply {
                    moveTo(81f, 241f)
                    lineTo(81f, 182f)
                    quadraticTo(105f, 160f, 130f, 182f)
                    lineTo(130f, 241f)
                    close()
                }
                outlined(barnDoor, color("door"))
                drawLine(outline, Offset(82f, 183f), Offset(129f, 240f), 2f)
                drawLine(outline, Offset(129f, 183f), Offset(82f, 240f), 2f)
                drawOval(color("cow"), Offset(220f, 183f), androidx.compose.ui.geometry.Size(91f, 57f))
                drawOval(outline, Offset(220f, 183f), androidx.compose.ui.geometry.Size(91f, 57f), style = Stroke(3f))
                drawCircle(color("cow"), 24f, Offset(308f, 187f))
                drawCircle(outline, 24f, Offset(308f, 187f), style = Stroke(3f))
                drawOval(color("cow"), Offset(290f, 167f), androidx.compose.ui.geometry.Size(18f, 12f))
                drawOval(outline, Offset(290f, 167f), androidx.compose.ui.geometry.Size(18f, 12f), style = Stroke(2f))
                drawOval(color("cow"), Offset(312f, 167f), androidx.compose.ui.geometry.Size(18f, 12f))
                drawOval(outline, Offset(312f, 167f), androidx.compose.ui.geometry.Size(18f, 12f), style = Stroke(2f))
                drawCircle(outline, 2.5f, Offset(315f, 183f))
                drawCircle(outline, 2.5f, Offset(327f, 183f))
                repeat(4) { index ->
                    val x = 235f + index * 19f
                    drawCircle(color("cow"), 7f, Offset(x, 199f + index % 2 * 17f))
                    drawCircle(outline, 7f, Offset(x, 199f + index % 2 * 17f), style = Stroke(1.5f))
                }
                repeat(3) { index ->
                    val x = 330f + index * 22f
                    drawLine(outline, Offset(x, 218f), Offset(x, 278f), 4f)
                    drawLine(outline, Offset(x + 17f, 218f), Offset(x + 17f, 278f), 4f)
                    drawLine(outline, Offset(x, 233f), Offset(x + 17f, 233f), 3f)
                    drawLine(outline, Offset(x, 253f), Offset(x + 17f, 253f), 3f)
                }
                val hay = Path().apply {
                    moveTo(166f, 275f)
                    lineTo(177f, 246f)
                    lineTo(203f, 246f)
                    lineTo(217f, 275f)
                    close()
                }
                outlined(hay, color("hay"))
                repeat(3) { index ->
                    drawLine(outline, Offset(178f + index * 9f, 254f), Offset(174f + index * 10f, 270f), 1.5f)
                }
            }
            ColoringTheme.SPACE -> {
                drawCircle(color("moon"), 30f, Offset(61f, 60f))
                drawCircle(outline, 30f, Offset(61f, 60f), style = Stroke(2.5f))
                listOf(Offset(48f, 51f), Offset(70f, 68f), Offset(47f, 75f)).forEach {
                    drawCircle(outline, 3f, it)
                }
                drawCircle(color("planet"), 30f, Offset(333f, 78f))
                drawCircle(outline, 30f, Offset(333f, 78f), style = Stroke(2.5f))
                drawOval(
                    Color.White,
                    Offset(296f, 68f),
                    androidx.compose.ui.geometry.Size(74f, 23f),
                    style = Stroke(3f)
                )
                val rocket = Path().apply {
                    moveTo(198f, 91f)
                    cubicTo(165f, 120f, 174f, 180f, 182f, 205f)
                    lineTo(214f, 205f)
                    cubicTo(222f, 180f, 231f, 120f, 198f, 91f)
                    close()
                }
                outlined(rocket, color("rocket"))
                drawCircle(color("window"), 15f, Offset(198f, 151f))
                drawCircle(outline, 15f, Offset(198f, 151f), style = Stroke(2.5f))
                val leftFin = Path().apply {
                    moveTo(181f, 172f)
                    lineTo(159f, 199f)
                    lineTo(184f, 193f)
                    close()
                }
                val rightFin = Path().apply {
                    moveTo(215f, 172f)
                    lineTo(237f, 199f)
                    lineTo(212f, 193f)
                    close()
                }
                outlined(leftFin, color("rocket"))
                outlined(rightFin, color("rocket"))
                val flame = Path().apply {
                    moveTo(183f, 205f)
                    quadraticTo(198f, 269f, 213f, 205f)
                    close()
                }
                outlined(flame, color("flame"))
                drawCircle(color("alien"), 25f, Offset(309f, 222f))
                drawCircle(outline, 25f, Offset(309f, 222f), style = Stroke(2.5f))
                drawOval(Color.White, Offset(294f, 213f), androidx.compose.ui.geometry.Size(11f, 15f))
                drawOval(Color.White, Offset(313f, 213f), androidx.compose.ui.geometry.Size(11f, 15f))
                drawCircle(outline, 2.5f, Offset(300f, 220f))
                drawCircle(outline, 2.5f, Offset(319f, 220f))
                repeat(4) { index ->
                    val x = 55f + index * 22f
                    val y = 172f + index % 2 * 23f
                    drawColoringStar(outline, x, y, 7f + scene.variation % 3)
                }
                val comet = Path().apply {
                    moveTo(81f, 249f)
                    quadraticTo(92f, 237f, 108f, 252f)
                    quadraticTo(92f, 269f, 81f, 249f)
                    close()
                }
                outlined(comet, color("comet"))
                drawLine(outline, Offset(72f, 263f), Offset(49f, 281f), 2f)
                drawLine(outline, Offset(81f, 269f), Offset(67f, 286f), 2f)
            }
            ColoringTheme.FANTASY -> {
                drawColoringCloud(outline, color("cloud"), Offset(62f, 54f))
                drawCircle(color("moon"), 21f, Offset(342f, 59f))
                drawCircle(outline, 21f, Offset(342f, 59f), style = Stroke(2.5f))
                drawRect(color("castle"), Offset(151f, 145f), androidx.compose.ui.geometry.Size(102f, 113f))
                drawRect(outline, Offset(151f, 145f), androidx.compose.ui.geometry.Size(102f, 113f), style = Stroke(3f))
                repeat(2) { index ->
                    val x = 132f + index * 109f
                    drawRect(color("towers"), Offset(x, 113f), androidx.compose.ui.geometry.Size(39f, 145f))
                    drawRect(outline, Offset(x, 113f), androidx.compose.ui.geometry.Size(39f, 145f), style = Stroke(3f))
                    val roof = Path().apply {
                        moveTo(x - 6f, 113f)
                        lineTo(x + 19f, 79f)
                        lineTo(x + 45f, 113f)
                        close()
                    }
                    outlined(roof, color("towers"))
                    drawRect(Color.White, Offset(x + 13f, 134f), androidx.compose.ui.geometry.Size(12f, 18f))
                    drawRect(outline, Offset(x + 13f, 134f), androidx.compose.ui.geometry.Size(12f, 18f), style = Stroke(2f))
                }
                val gate = Path().apply {
                    moveTo(184f, 258f)
                    lineTo(184f, 221f)
                    quadraticTo(202f, 197f, 220f, 221f)
                    lineTo(220f, 258f)
                    close()
                }
                outlined(gate, color("castle"))

                val wing = Path().apply {
                    moveTo(294f, 192f)
                    quadraticTo(267f, 143f, 307f, 159f)
                    quadraticTo(322f, 171f, 319f, 196f)
                    close()
                }
                outlined(wing, color("wings"))
                drawOval(color("dragon"), Offset(288f, 186f), androidx.compose.ui.geometry.Size(58f, 42f))
                drawOval(outline, Offset(288f, 186f), androidx.compose.ui.geometry.Size(58f, 42f), style = Stroke(2.5f))
                drawCircle(color("dragon"), 20f, Offset(344f, 185f))
                drawCircle(outline, 20f, Offset(344f, 185f), style = Stroke(2.5f))
                drawLine(outline, Offset(328f, 220f), Offset(322f, 251f), 5f, StrokeCap.Round)
                drawLine(outline, Offset(341f, 220f), Offset(346f, 251f), 5f, StrokeCap.Round)
                val tail = Path().apply {
                    moveTo(290f, 204f)
                    cubicTo(267f, 200f, 267f, 231f, 284f, 228f)
                }
                drawPath(tail, color("dragon"), style = Stroke(7f, cap = StrokeCap.Round))
                drawPath(tail, outline, style = Stroke(2f, cap = StrokeCap.Round))
                drawCircle(Color.White, 6f, Offset(351f, 180f))
                drawCircle(outline, 2f, Offset(352f, 180f))
                repeat(3) { index -> drawColoringStar(outline, 50f + index * 24f, 156f + index % 2 * 25f, 8f) }
                drawRoundRect(color("tree"), Offset(60f, 240f), androidx.compose.ui.geometry.Size(19f, 40f), androidx.compose.ui.geometry.CornerRadius(4f))
                drawCircle(color("tree"), 30f, Offset(69f, 231f))
                drawCircle(outline, 30f, Offset(69f, 231f), style = Stroke(2.5f))
            }
            ColoringTheme.ARCTIC -> {
                drawCircle(color("sun"), 20f, Offset(53f, 47f))
                drawCircle(outline, 20f, Offset(53f, 47f), style = Stroke(2.5f))
                val igloo = Path().apply {
                    moveTo(47f, 250f)
                    quadraticTo(47f, 149f, 108f, 149f)
                    quadraticTo(168f, 149f, 168f, 250f)
                    close()
                }
                outlined(igloo, color("igloo"))
                drawArc(outline, 180f, 180f, false, Offset(84f, 207f), androidx.compose.ui.geometry.Size(48f, 86f), style = Stroke(2.5f))
                repeat(3) { row ->
                    val y = 180f + row * 22f
                    drawLine(outline, Offset(57f, y), Offset(158f, y), 2f)
                }
                drawLine(outline, Offset(96f, 163f), Offset(96f, 228f), 2f)
                drawLine(outline, Offset(126f, 163f), Offset(126f, 228f), 2f)
                drawOval(color("penguin"), Offset(218f, 168f), androidx.compose.ui.geometry.Size(62f, 92f))
                drawOval(outline, Offset(218f, 168f), androidx.compose.ui.geometry.Size(62f, 92f), style = Stroke(3f))
                drawOval(Color.White, Offset(230f, 188f), androidx.compose.ui.geometry.Size(39f, 58f))
                drawCircle(Color.White, 8f, Offset(235f, 190f))
                drawCircle(Color.White, 8f, Offset(260f, 190f))
                drawCircle(outline, 2.5f, Offset(237f, 191f))
                drawCircle(outline, 2.5f, Offset(262f, 191f))
                val beak = Path().apply {
                    moveTo(242f, 204f)
                    lineTo(253f, 204f)
                    lineTo(247f, 213f)
                    close()
                }
                outlined(beak, color("penguin"))
                drawLine(color("scarf"), Offset(222f, 215f), Offset(278f, 215f), 8f, StrokeCap.Round)
                drawLine(outline, Offset(222f, 215f), Offset(278f, 215f), 2f, StrokeCap.Round)
                drawLine(color("scarf"), Offset(265f, 217f), Offset(273f, 238f), 6f, StrokeCap.Round)
                drawLine(outline, Offset(265f, 217f), Offset(273f, 238f), 2f, StrokeCap.Round)
                val iceberg = Path().apply {
                    moveTo(280f, 177f)
                    lineTo(305f, 123f)
                    lineTo(321f, 150f)
                    lineTo(339f, 111f)
                    lineTo(370f, 177f)
                    close()
                }
                outlined(iceberg, color("iceberg"))
                repeat(4) { index ->
                    drawColoringSnowflake(outline, 177f + index * 47f, 48f + index % 2 * 20f, 8f)
                }
                val fish = Path().apply {
                    moveTo(314f, 263f)
                    quadraticTo(338f, 244f, 359f, 263f)
                    quadraticTo(338f, 282f, 314f, 263f)
                    close()
                }
                outlined(fish, color("fish"))
                val tail = Path().apply {
                    moveTo(316f, 263f)
                    lineTo(300f, 252f)
                    lineTo(300f, 274f)
                    close()
                }
                outlined(tail, color("fish"))
                drawCircle(outline, 2f, Offset(347f, 261f))
                drawLine(outline, Offset(36f, 280f), Offset(382f, 280f), 3f)
            }
            ColoringTheme.BUGS -> {
                drawCircle(color("sun"), 20f, Offset(53f, 46f))
                drawCircle(outline, 20f, Offset(53f, 46f), style = Stroke(2.5f))
                drawColoringCloud(outline, color("cloud"), Offset(324f, 55f))
                repeat(6) { index ->
                    val angleOffset = listOf(
                        Offset(104f, 145f), Offset(128f, 158f), Offset(128f, 184f),
                        Offset(104f, 197f), Offset(80f, 184f), Offset(80f, 158f)
                    )[index]
                    drawCircle(color("flower"), 14f, angleOffset)
                    drawCircle(outline, 14f, angleOffset, style = Stroke(2.5f))
                }
                drawCircle(color("flower"), 14f, Offset(104f, 171f))
                drawCircle(outline, 14f, Offset(104f, 171f), style = Stroke(2.5f))
                drawLine(outline, Offset(104f, 211f), Offset(104f, 277f), 4f)
                drawColoringLeaf(outline, Color.White, 104f, 242f, 76f, 230f, 90f, 251f)

                drawOval(color("bee"), Offset(196f, 97f), androidx.compose.ui.geometry.Size(52f, 31f))
                drawOval(outline, Offset(196f, 97f), androidx.compose.ui.geometry.Size(52f, 31f), style = Stroke(2.5f))
                drawOval(Color.White, Offset(204f, 78f), androidx.compose.ui.geometry.Size(20f, 25f))
                drawOval(outline, Offset(204f, 78f), androidx.compose.ui.geometry.Size(20f, 25f), style = Stroke(2f))
                drawOval(Color.White, Offset(222f, 78f), androidx.compose.ui.geometry.Size(20f, 25f))
                drawOval(outline, Offset(222f, 78f), androidx.compose.ui.geometry.Size(20f, 25f), style = Stroke(2f))
                drawLine(outline, Offset(213f, 99f), Offset(213f, 126f), 4f)
                drawLine(outline, Offset(229f, 99f), Offset(229f, 126f), 4f)
                drawCircle(outline, 2.5f, Offset(237f, 108f))
                drawLine(outline, Offset(235f, 95f), Offset(241f, 87f), 2f)
                drawLine(outline, Offset(235f, 95f), Offset(230f, 86f), 2f)

                drawOval(color("butterfly"), Offset(291f, 135f), androidx.compose.ui.geometry.Size(23f, 32f))
                drawOval(outline, Offset(291f, 135f), androidx.compose.ui.geometry.Size(23f, 32f), style = Stroke(2.5f))
                drawOval(color("butterfly"), Offset(313f, 135f), androidx.compose.ui.geometry.Size(23f, 32f))
                drawOval(outline, Offset(313f, 135f), androidx.compose.ui.geometry.Size(23f, 32f), style = Stroke(2.5f))
                drawLine(outline, Offset(312f, 145f), Offset(312f, 174f), 4f)
                drawOval(color("ladybug"), Offset(210f, 220f), androidx.compose.ui.geometry.Size(53f, 37f))
                drawOval(outline, Offset(210f, 220f), androidx.compose.ui.geometry.Size(53f, 37f), style = Stroke(2.5f))
                drawLine(outline, Offset(236f, 222f), Offset(236f, 255f), 2f)
                listOf(Offset(223f, 232f), Offset(247f, 231f), Offset(223f, 246f), Offset(247f, 245f))
                    .forEach { drawCircle(outline, 3f, it) }
                repeat(3) { index ->
                    drawColoringLeaf(outline, color("leaves"), 40f + index * 22f, 271f, 52f + index * 22f, 252f, 59f + index * 22f, 278f)
                }
                repeat(5) { index ->
                    drawCircle(color("caterpillar"), 12f, Offset(300f + index * 17f, 258f))
                    drawCircle(outline, 12f, Offset(300f + index * 17f, 258f), style = Stroke(2f))
                }
                drawCircle(Color.White, 2.5f, Offset(366f, 253f))
                drawCircle(outline, 2f, Offset(367f, 253f))
            }
        }
        drawSceneVariation(scene, outline)
        drawLine(outline, Offset(0f, 286f), Offset(400f, 286f), 3f)
    }
}

private fun DrawScope.drawColoringCloud(
    outline: Color,
    fill: Color,
    center: Offset
) {
    val puffs = listOf(
        center + Offset(-20f, 7f) to 17f,
        center + Offset(0f, -2f) to 23f,
        center + Offset(22f, 8f) to 16f
    )
    puffs.forEach { (point, radius) ->
        drawCircle(fill, radius, point)
        drawCircle(outline, radius, point, style = Stroke(2.5f))
    }
    drawRoundRect(
        fill,
        center + Offset(-35f, 5f),
        androidx.compose.ui.geometry.Size(70f, 20f),
        androidx.compose.ui.geometry.CornerRadius(10f)
    )
    drawRoundRect(
        outline,
        center + Offset(-35f, 5f),
        androidx.compose.ui.geometry.Size(70f, 20f),
        androidx.compose.ui.geometry.CornerRadius(10f),
        style = Stroke(2.5f)
    )
}

private fun DrawScope.drawColoringLeaf(
    outline: Color,
    fill: Color,
    startX: Float,
    startY: Float,
    tipX: Float,
    tipY: Float,
    endX: Float,
    endY: Float
) {
    val leaf = Path().apply {
        moveTo(startX, startY)
        quadraticTo(tipX, tipY, endX, endY)
        quadraticTo(tipX - 2f, (tipY + startY) / 2f, startX, startY)
        close()
    }
    drawPath(leaf, fill)
    drawPath(leaf, outline, style = Stroke(2f))
    drawLine(outline, Offset(startX, startY), Offset(tipX, tipY), 1.5f)
}

private fun DrawScope.drawColoringStar(
    outline: Color,
    centerX: Float,
    centerY: Float,
    radius: Float
) {
    val points = listOf(
        Offset(centerX, centerY - radius),
        Offset(centerX + radius * 0.24f, centerY - radius * 0.24f),
        Offset(centerX + radius, centerY),
        Offset(centerX + radius * 0.24f, centerY + radius * 0.24f),
        Offset(centerX, centerY + radius),
        Offset(centerX - radius * 0.24f, centerY + radius * 0.24f),
        Offset(centerX - radius, centerY),
        Offset(centerX - radius * 0.24f, centerY - radius * 0.24f)
    )
    val star = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
    drawPath(star, Color.White)
    drawPath(star, outline, style = Stroke(1.8f))
}

private fun DrawScope.drawColoringSnowflake(
    outline: Color,
    centerX: Float,
    centerY: Float,
    radius: Float
) {
    drawLine(outline, Offset(centerX, centerY - radius), Offset(centerX, centerY + radius), 2f)
    drawLine(outline, Offset(centerX - radius * 0.8f, centerY - radius * 0.5f), Offset(centerX + radius * 0.8f, centerY + radius * 0.5f), 2f)
    drawLine(outline, Offset(centerX - radius * 0.8f, centerY + radius * 0.5f), Offset(centerX + radius * 0.8f, centerY - radius * 0.5f), 2f)
    drawCircle(outline, 2f, Offset(centerX, centerY))
}

private fun DrawScope.drawSceneVariation(scene: ColoringScene, outline: Color) {
    repeat(scene.variation % 5 + 2) { index ->
        val x = 20f + ((scene.variation * 41 + index * 67) % 360)
        val y = 24f + ((scene.variation * 23 + index * 39) % 105)
        when (scene.theme) {
            ColoringTheme.SPACE, ColoringTheme.FANTASY ->
                drawColoringStar(outline, x, y, 4f + scene.variation % 4)
            ColoringTheme.UNDERWATER, ColoringTheme.ARCTIC -> {
                val radius = 3f + index % 3
                drawCircle(Color.White, radius, Offset(x, y))
                drawCircle(outline, radius, Offset(x, y), style = Stroke(1.6f))
            }
            ColoringTheme.GARDEN, ColoringTheme.FOREST, ColoringTheme.BUGS ->
                drawColoringLeaf(outline, Color.White, x, y + 8f, x + 7f, y - 5f, x + 13f, y + 8f)
            ColoringTheme.DINO, ColoringTheme.SAFARI, ColoringTheme.FARM -> {
                drawCircle(Color.White, 3f + index % 3, Offset(x, y))
                drawCircle(outline, 3f + index % 3, Offset(x, y), style = Stroke(1.6f))
            }
        }
    }
}

@Composable
private fun CarDrivingActivity(
    age: Int,
    initialStage: Int,
    berry: Color,
    onBack: () -> Unit,
    onSoundEffect: (SoundEffect) -> Unit,
    onStageCompleted: (LearningModule, Int) -> Unit
) {
    var carLane by remember { mutableIntStateOf(1) }
    var obstacleLane by remember { mutableIntStateOf(kotlin.random.Random.nextInt(3)) }
    var obstacleRow by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var stage by remember { mutableIntStateOf(initialStage) }
    var crashed by remember { mutableStateOf(false) }
    var stageComplete by remember { mutableStateOf(false) }
    val scoreTarget = drivingGoalFor(age, stage)

    val steer: (Int) -> Unit = steer@{ direction ->
        if (crashed || stageComplete) return@steer
        carLane = (carLane + direction).coerceIn(0, 2)
        val nextObstacleRow = obstacleRow + 1
        if (nextObstacleRow >= 4) {
            if (obstacleLane == carLane) {
                crashed = true
                onSoundEffect(SoundEffect.INCORRECT)
            } else {
                val nextScore = score + 1
                score = nextScore
                if (nextScore >= scoreTarget) {
                    stageComplete = true
                    onStageCompleted(LearningModule.CAR_DRIVING, stage)
                }
                obstacleRow = 0
                obstacleLane = kotlin.random.Random.nextInt(3)
                onSoundEffect(SoundEffect.CORRECT)
            }
        } else {
            obstacleRow = nextObstacleRow
            onSoundEffect(SoundEffect.TAP)
        }
    }

    ActivityPage("Car Driving", berry, onBack, onSoundEffect) {
        Text(
            when {
                crashed -> "Bump! Try the stage again."
                stageComplete -> "Stage $stage complete! Great driving!"
                else -> "Stage $stage • Steer clear of the road cones."
            }
        )
        Text("Cones passed: $score / $scoreTarget", fontWeight = FontWeight.Bold)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (0..4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (0..2).forEach { lane ->
                        val isCar = row == 4 && lane == carLane
                        val isObstacle = row == obstacleRow && lane == obstacleLane
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .background(Color(0xFF454A54), RoundedCornerShape(8.dp)),
                        ) {
                            Canvas(Modifier.fillMaxSize()) {
                                val scaleX = size.width / 100f
                                val scaleY = size.height / 52f
                                withTransform({
                                    scale(scaleX, scaleY, pivot = Offset.Zero)
                                }) {
                                    if (lane < 2) {
                                        drawLine(
                                            Color.White.copy(alpha = 0.75f),
                                            Offset(97f, 4f),
                                            Offset(97f, 18f),
                                            2f
                                        )
                                        drawLine(
                                            Color.White.copy(alpha = 0.75f),
                                            Offset(97f, 32f),
                                            Offset(97f, 46f),
                                            2f
                                        )
                                    }
                                    when {
                                        isCar -> drawRoadCar()
                                        isObstacle -> drawRoadCone()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { steer(-1) }, enabled = !crashed && !stageComplete) { Text("← Left") }
            Button(onClick = { steer(1) }, enabled = !crashed && !stageComplete) { Text("Right →") }
        }
        if (stageComplete) {
            Button(
                onClick = {
                    onSoundEffect(SoundEffect.TAP)
                    stage += 1
                    carLane = 1
                    obstacleLane = kotlin.random.Random.nextInt(3)
                    obstacleRow = 0
                    score = 0
                    crashed = false
                    stageComplete = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = berry)
            ) {
                Text("Next stage")
            }
        } else if (crashed) {
            Button(
                onClick = {
                    onSoundEffect(SoundEffect.TAP)
                    carLane = 1
                    obstacleLane = kotlin.random.Random.nextInt(3)
                    obstacleRow = 0
                    score = 0
                    crashed = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = berry)
            ) {
                Text("Retry stage")
            }
        }
    }
}

private fun DrawScope.drawRoadCar() {
            val outline = Color(0xFF25313A)
            drawRoundRect(Color(0xFFDB4E4E), Offset(29f, 5f), androidx.compose.ui.geometry.Size(43f, 43f), androidx.compose.ui.geometry.CornerRadius(12f))
            drawRoundRect(outline, Offset(29f, 5f), androidx.compose.ui.geometry.Size(43f, 43f), androidx.compose.ui.geometry.CornerRadius(12f), style = Stroke(2.5f))
            drawRoundRect(Color(0xFFBCEBFA), Offset(36f, 11f), androidx.compose.ui.geometry.Size(29f, 13f), androidx.compose.ui.geometry.CornerRadius(5f))
            drawRoundRect(outline, Offset(36f, 11f), androidx.compose.ui.geometry.Size(29f, 13f), androidx.compose.ui.geometry.CornerRadius(5f), style = Stroke(1.5f))
            drawRoundRect(Color(0xFFBCEBFA), Offset(36f, 29f), androidx.compose.ui.geometry.Size(29f, 9f), androidx.compose.ui.geometry.CornerRadius(4f))
            listOf(Offset(25f, 12f), Offset(67f, 12f), Offset(25f, 35f), Offset(67f, 35f)).forEach {
                drawRoundRect(outline, it, androidx.compose.ui.geometry.Size(8f, 10f), androidx.compose.ui.geometry.CornerRadius(3f))
            }
            drawRoundRect(Color(0xFFFFF1A8), Offset(33f, 5f), androidx.compose.ui.geometry.Size(10f, 3f), androidx.compose.ui.geometry.CornerRadius(1.5f))
            drawRoundRect(Color(0xFFFFF1A8), Offset(58f, 5f), androidx.compose.ui.geometry.Size(10f, 3f), androidx.compose.ui.geometry.CornerRadius(1.5f))
}

private fun DrawScope.drawRoadCone() {
            val cone = Path().apply {
                moveTo(50f, 7f)
                lineTo(72f, 46f)
                lineTo(28f, 46f)
                close()
            }
            drawPath(cone, Color(0xFFFF8A35))
            drawPath(cone, Color(0xFF3B2F28), style = Stroke(2.5f))
            val stripe = Path().apply {
                moveTo(39f, 27f)
                lineTo(61f, 27f)
                lineTo(65f, 34f)
                lineTo(35f, 34f)
                close()
            }
            drawPath(stripe, Color.White)
            drawRoundRect(Color(0xFF3B2F28), Offset(25f, 44f), androidx.compose.ui.geometry.Size(50f, 5f), androidx.compose.ui.geometry.CornerRadius(2f))
}
