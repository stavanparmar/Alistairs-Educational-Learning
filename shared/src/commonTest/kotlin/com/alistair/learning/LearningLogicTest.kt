package com.alistair.learning

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.random.Random

class LearningLogicTest {
    @Test
    fun ageBandsAreAssignedCorrectly() {
        assertEquals(AgeBand.EARLY, ageBandFor(3))
        assertEquals(AgeBand.EARLY, ageBandFor(4))
        assertEquals(AgeBand.FOUNDATIONS, ageBandFor(5))
        assertEquals(AgeBand.FOUNDATIONS, ageBandFor(6))
        assertEquals(AgeBand.DEVELOPING, ageBandFor(7))
        assertEquals(AgeBand.DEVELOPING, ageBandFor(8))
        assertEquals(AgeBand.ADVANCING, ageBandFor(9))
        assertEquals(AgeBand.ADVANCING, ageBandFor(10))
    }

    @Test
    fun rejectsUnsupportedAges() {
        assertFailsWith<IllegalArgumentException> { ageBandFor(2) }
        assertFailsWith<IllegalArgumentException> { ageBandFor(11) }
    }

    @Test
    fun ageAdjustsGameDifficultyAndCurriculum() {
        assertEquals(3, memoryPairCountFor(3, 1))
        assertEquals(6, memoryPairCountFor(10, 1))
        assertEquals(4, memoryPairCountFor(3, 2))
        assertEquals(3, mazeRoomCountFor(3, 1))
        assertEquals(6, mazeRoomCountFor(10, 1))
        assertEquals(7, mazeRoomCountFor(10, 3))
        assertEquals(3, drivingGoalFor(3, 1))
        assertEquals(6, drivingGoalFor(10, 1))
        assertTrue(
            generalKnowledgeQuestionsForAge(3).map { it.prompt } !=
                generalKnowledgeQuestionsForAge(9).map { it.prompt }
        )
        assertTrue(
            colourQuestionsForAge(3).map { it.prompt } !=
                colourQuestionsForAge(9).map { it.prompt }
        )
    }

    @Test
    fun progressKeepsTheHighestCompletedStagePerGame() {
        val progress = LearningProgress()
            .completeStage(LearningModule.MATHS, 2)
            .completeStage(LearningModule.MATHS, 1)
            .completeStage(LearningModule.MAZE, 1)

        assertEquals(3, progress.nextStage(LearningModule.MATHS))
        assertEquals(2, progress.nextStage(LearningModule.MAZE))
        assertEquals(3, progress.totalCompletedStages)
    }

    @Test
    fun profileRejectsBlankName() {
        assertFailsWith<IllegalArgumentException> { ChildProfile(" ", 5) }
    }

    @Test
    fun activityQuestionsHaveDistinctChoicesAndAValidAnswer() {
        val questions = listOf(
            *mathsQuestionsForAge(4).toTypedArray(),
            *mathsQuestionsForAge(8).toTypedArray(),
            *mathsQuestionsForAge(10).toTypedArray(),
            *generalKnowledgeQuestions().toTypedArray(),
            *(3..10).flatMap(::generalKnowledgeQuestionsForAge).toTypedArray(),
            *(3..10).flatMap(::colourQuestionsForAge).toTypedArray()
        )

        questions.forEach { question ->
            assertTrue(question.options.size >= 3)
            assertEquals(question.options.size, question.options.distinct().size)
            assertTrue(question.answer in question.options)
        }
    }

    @Test
    fun coloringCatalogHasOneHundredDistinctInteractivePages() {
        assertEquals(100, coloringScenes.size)
        assertEquals(100, coloringScenes.map { it.title }.distinct().size)
        assertEquals((0 until 100).toList(), coloringScenes.map { it.id })
        assertTrue(coloringScenes.all { it.regions.size >= 8 })
    }

    @Test
    fun quizStagesRotateQuestionsAndKeepAnswersAvailable() {
        val questions = listOf(
            ActivityQuestion("One?", listOf("1", "2", "3"), "1"),
            ActivityQuestion("Two?", listOf("1", "2", "3"), "2"),
            ActivityQuestion("Three?", listOf("1", "2", "3"), "3")
        )

        val firstStage = questionsForStage(questions, 1)
        val secondStage = questionsForStage(questions, 2)

        assertNotEquals(firstStage.map { it.prompt }, secondStage.map { it.prompt })
        assertTrue(secondStage.all { it.answer in it.options })
    }

    @Test
    fun memoryGameMatchesPairsAndCountsTurns() {
        val game = newMemoryGame(listOf("cat", "dog"))
        val firstCat = game.cards.first { it.symbol == "cat" }.id
        val secondCat = game.cards.last { it.symbol == "cat" }.id

        val firstPick = revealMemoryCard(game, firstCat)
        val matched = revealMemoryCard(firstPick, secondCat)

        assertEquals(1, matched.turns)
        assertTrue(matched.cards.filter { it.symbol == "cat" }.all { it.matched })
        assertTrue(!matched.isComplete)

        val firstDog = matched.cards.first { it.symbol == "dog" }.id
        val secondDog = matched.cards.last { it.symbol == "dog" }.id
        val complete = revealMemoryCard(revealMemoryCard(matched, firstDog), secondDog)
        assertTrue(complete.isComplete)
    }

    @Test
    fun memoryGameHidesMismatchesAndIgnoresPicksUntilHidden() {
        val game = newMemoryGame(listOf("cat", "dog"))
        val cat = game.cards.first { it.symbol == "cat" }.id
        val dog = game.cards.first { it.symbol == "dog" }.id

        val mismatch = revealMemoryCard(revealMemoryCard(game, cat), dog)
        assertTrue(mismatch.hasMismatch)
        assertEquals(mismatch, revealMemoryCard(mismatch, game.cards.last().id))

        val hidden = hideMismatchedMemoryCards(mismatch)
        assertTrue(!hidden.hasMismatch)
        assertEquals(null, hidden.firstCardId)
        assertEquals(null, hidden.secondCardId)
        assertEquals(hidden.turns, revealMemoryCard(hidden, game.cards.last().id).turns)
    }

    @Test
    fun generatedMazesAreDifferentAndSolvable() {
        val first = generateMaze(Random(1))
        val second = generateMaze(Random(2))

        assertTrue(first != second)
        assertNotEquals(first, generateDifferentMaze(first, Random(1)))
        assertEquals(9, first.size)
        assertTrue(first.all { it.length == 9 })

        val visited = mutableSetOf<Pair<Int, Int>>()
        val pending = mutableListOf(0 to 0)
        var foundGoal = false
        while (pending.isNotEmpty()) {
            val (x, y) = pending.removeAt(0)
            if (x !in first[0].indices || y !in first.indices ||
                first[y][x] == '#' || !visited.add(x to y)
            ) {
                continue
            }
            if (first[y][x] == 'G') {
                foundGoal = true
                break
            }
            pending += listOf(x to y - 1, x + 1 to y, x to y + 1, x - 1 to y)
        }
        assertTrue(foundGoal)
    }

    @Test
    fun plannerCreatesNextStageAndDeduplicatesQuestions() {
        val stage = StagePlanner().nextStage(
            subject = "Maths",
            age = 8,
            completedStageNumbers = listOf(1, 2),
            availableQuestionIds = listOf("q1", "q1", "q2", "q3"),
            questionCount = 3
        )
        assertEquals(3, stage.stageNumber)
        assertEquals(AgeBand.DEVELOPING, stage.ageBand)
        assertEquals(listOf("q1", "q2", "q3"), stage.questionIds)
    }

    @Test
    fun localRetrieverFindsRelevantChunks() {
        val retriever = LocalKnowledgeRetriever(
            listOf(
                KnowledgeChunk("1", "Planets", "Earth moves around the Sun."),
                KnowledgeChunk("2", "Colours", "Blue and yellow are colours.")
            )
        )
        assertEquals("1", retriever.retrieve("earth sun").first().id)
    }

    @Test
    fun localRetrieverReturnsEmptyForUnknownQuery() {
        val retriever = LocalKnowledgeRetriever(
            listOf(KnowledgeChunk("1", "Planets", "Earth moves around the Sun."))
        )
        assertTrue(retriever.retrieve("dinosaurs").isEmpty())
    }
}
