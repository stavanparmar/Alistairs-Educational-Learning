package com.alistair.learning

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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
    fun profileRejectsBlankName() {
        assertFailsWith<IllegalArgumentException> { ChildProfile(" ", 5) }
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
