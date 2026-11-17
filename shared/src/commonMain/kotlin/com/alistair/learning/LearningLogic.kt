package com.alistair.learning

import kotlin.random.Random

/** Age band used to choose a starting curriculum. */
enum class AgeBand { EARLY, FOUNDATIONS, DEVELOPING, ADVANCING }

fun ageBandFor(age: Int): AgeBand {
    require(age in 3..10) { "Supported ages are 3 through 10." }
    return when (age) {
        3, 4 -> AgeBand.EARLY
        5, 6 -> AgeBand.FOUNDATIONS
        7, 8 -> AgeBand.DEVELOPING
        else -> AgeBand.ADVANCING
    }
}

fun memoryPairCountFor(age: Int, stage: Int): Int {
    require(stage > 0) { "Stage numbers must be positive." }
    val startingPairs = when (ageBandFor(age)) {
        AgeBand.EARLY -> 3
        AgeBand.FOUNDATIONS -> 4
        AgeBand.DEVELOPING -> 5
        AgeBand.ADVANCING -> 6
    }
    return (startingPairs + stage - 1).coerceAtMost(12)
}

fun mazeRoomCountFor(age: Int, stage: Int): Int {
    require(stage > 0) { "Stage numbers must be positive." }
    val startingRooms = when (ageBandFor(age)) {
        AgeBand.EARLY -> 3
        AgeBand.FOUNDATIONS -> 4
        AgeBand.DEVELOPING -> 5
        AgeBand.ADVANCING -> 6
    }
    return (startingRooms + (stage - 1) / 2).coerceAtMost(8)
}

fun drivingGoalFor(age: Int, stage: Int): Int {
    require(stage > 0) { "Stage numbers must be positive." }
    val startingCones = when (ageBandFor(age)) {
        AgeBand.EARLY -> 3
        AgeBand.FOUNDATIONS -> 4
        AgeBand.DEVELOPING -> 5
        AgeBand.ADVANCING -> 6
    }
    return startingCones + (stage - 1) * 2
}

data class ChildProfile(val displayName: String, val age: Int) {
    init {
        require(displayName.isNotBlank()) { "Name cannot be blank." }
        require(age in 3..10) { "Age must be between 3 and 10." }
    }
}

interface ChildProfileStore {
    fun load(): ChildProfile?
    fun save(profile: ChildProfile): Boolean
}

data class AudioSettings(
    val soundEffectsEnabled: Boolean = true,
    val backgroundMusicEnabled: Boolean = true
)

interface AudioSettingsStore {
    fun loadAudioSettings(): AudioSettings
    fun saveAudioSettings(settings: AudioSettings): Boolean
}

data class LearningProgress(val completedStages: Map<String, Int> = emptyMap()) {
    fun nextStage(module: LearningModule): Int =
        (completedStages[module.name] ?: 0) + 1

    fun completeStage(module: LearningModule, stage: Int): LearningProgress {
        require(stage > 0) { "Stage numbers must be positive." }
        val previous = completedStages[module.name] ?: 0
        return copy(completedStages = completedStages + (module.name to maxOf(previous, stage)))
    }

    val totalCompletedStages: Int
        get() = completedStages.values.sum()
}

interface LearningProgressStore {
    fun loadProgress(profile: ChildProfile): LearningProgress
    fun saveProgress(profile: ChildProfile, progress: LearningProgress): Boolean
}

data class MemoryCard(val id: Int, val symbol: String, val matched: Boolean = false)

data class MemoryGameState(
    val cards: List<MemoryCard>,
    val firstCardId: Int? = null,
    val secondCardId: Int? = null,
    val turns: Int = 0
) {
    val isComplete: Boolean
        get() = cards.all { it.matched }

    val hasMismatch: Boolean
        get() {
            val first = cards.find { it.id == firstCardId } ?: return false
            val second = cards.find { it.id == secondCardId } ?: return false
            return first.symbol != second.symbol
        }
}

fun newMemoryGame(symbols: List<String>): MemoryGameState {
    require(symbols.size >= 2) { "A memory game needs at least two pairs." }
    require(symbols.all { it.isNotBlank() } && symbols.distinct().size == symbols.size) {
        "Each pair must have a unique, non-blank symbol."
    }

    return MemoryGameState(
        cards = symbols.flatMapIndexed { pairIndex, symbol ->
            listOf(
                MemoryCard(pairIndex * 2, symbol),
                MemoryCard(pairIndex * 2 + 1, symbol)
            )
        }
    )
}

fun revealMemoryCard(state: MemoryGameState, cardId: Int): MemoryGameState {
    val card = state.cards.find { it.id == cardId } ?: return state
    if (card.matched || state.secondCardId != null || state.firstCardId == cardId) return state

    val firstCardId = state.firstCardId
    if (firstCardId == null) {
        return state.copy(firstCardId = cardId)
    }

    val firstCard = state.cards.find { it.id == firstCardId } ?: return state
    val updatedCards = if (firstCard.symbol == card.symbol) {
        state.cards.map {
            if (it.id == firstCardId || it.id == cardId) it.copy(matched = true) else it
        }
    } else {
        state.cards
    }

    return if (firstCard.symbol == card.symbol) {
        state.copy(cards = updatedCards, firstCardId = null, turns = state.turns + 1)
    } else {
        state.copy(secondCardId = cardId, turns = state.turns + 1)
    }
}

fun hideMismatchedMemoryCards(state: MemoryGameState): MemoryGameState =
    if (state.hasMismatch) state.copy(firstCardId = null, secondCardId = null) else state

fun generateMaze(random: Random = Random.Default, roomCount: Int = 5): List<String> {
    require(roomCount in 2..8) { "Maze room count must be between 2 and 8." }
    val gridSize = roomCount * 2 - 1
    val cells = Array(gridSize) { CharArray(gridSize) { '#' } }
    val visited = Array(roomCount) { BooleanArray(roomCount) }
    val directions = listOf(0 to -1, 1 to 0, 0 to 1, -1 to 0)

    fun carve(x: Int, y: Int) {
        visited[y][x] = true
        cells[y * 2][x * 2] = '.'
        directions.shuffled(random).forEach { (dx, dy) ->
            val nextX = x + dx
            val nextY = y + dy
            if (nextX in 0 until roomCount && nextY in 0 until roomCount && !visited[nextY][nextX]) {
                cells[y * 2 + dy][x * 2 + dx] = '.'
                carve(nextX, nextY)
            }
        }
    }

    carve(0, 0)
    cells[0][0] = 'S'
    cells[gridSize - 1][gridSize - 1] = 'G'
    return cells.map { String(it) }
}

fun generateDifferentMaze(
    previousMaze: List<String>,
    random: Random = Random.Default,
    roomCount: Int = 5
): List<String> {
    val generated = generateMaze(random, roomCount)
    if (generated != previousMaze) return generated

    val changed = generated.map { it.toCharArray() }
    val wall = changed.indices.firstNotNullOfOrNull { y ->
        changed[y].indices.firstOrNull { x -> changed[y][x] == '#' }?.let { x -> x to y }
    } ?: error("Generated maze has no wall to vary.")
    changed[wall.second][wall.first] = '.'
    return changed.map { String(it) }
}

data class LearningStage(
    val id: String,
    val subject: String,
    val title: String,
    val ageBand: AgeBand,
    val stageNumber: Int,
    val questionIds: List<String>
)

/**
 * Safe deterministic fallback planner. Production AI-generated stages must pass
 * validation and be saved only after retrieval and answer-key checks.
 */
class StagePlanner {
    fun nextStage(
        subject: String,
        age: Int,
        completedStageNumbers: List<Int>,
        availableQuestionIds: List<String>,
        questionCount: Int = 5
    ): LearningStage {
        require(subject.isNotBlank())
        require(questionCount > 0)
        require(availableQuestionIds.isNotEmpty()) { "No offline questions available." }

        val nextNumber = (completedStageNumbers.maxOrNull() ?: 0) + 1
        val uniqueIds = availableQuestionIds.distinct()
        val selected = uniqueIds.take(questionCount)
        return LearningStage(
            id = "${subject.lowercase()}-$nextNumber",
            subject = subject,
            title = "Stage $nextNumber",
            ageBand = ageBandFor(age),
            stageNumber = nextNumber,
            questionIds = selected
        )
    }
}

/** Local RAG boundary. Replace with a local index (e.g. SQLite/embedded vector store). */
data class KnowledgeChunk(val id: String, val topic: String, val text: String)

class LocalKnowledgeRetriever(private val chunks: List<KnowledgeChunk>) {
    fun retrieve(query: String, limit: Int = 4): List<KnowledgeChunk> {
        if (query.isBlank() || limit <= 0) return emptyList()
        val terms = query.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
        return chunks.map { chunk ->
            val haystack = (chunk.topic + " " + chunk.text).lowercase()
            val score = terms.count { it in haystack }
            chunk to score
        }.filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<KnowledgeChunk, Int>> { it.second }.thenBy { it.first.id })
            .take(limit)
            .map { it.first }
    }
}

/** Runtime adapter boundary; connect only to a local model runtime, never a hosted API. */
interface OnDeviceAiEngine {
    suspend fun generate(prompt: String): String
}
