package com.alistair.learning

import org.junit.Assert.*
import org.junit.Test

class LearningLogicTest {
    @Test fun ageBandsAreAssignedCorrectly() {
        assertEquals(AgeBand.EARLY, ageBandFor(3))
        assertEquals(AgeBand.EARLY, ageBandFor(4))
        assertEquals(AgeBand.FOUNDATIONS, ageBandFor(5))
        assertEquals(AgeBand.FOUNDATIONS, ageBandFor(6))
        assertEquals(AgeBand.DEVELOPING, ageBandFor(7))
        assertEquals(AgeBand.DEVELOPING, ageBandFor(8))
        assertEquals(AgeBand.ADVANCING, ageBandFor(9))
        assertEquals(AgeBand.ADVANCING, ageBandFor(10))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAgeBelowThree() { ageBandFor(2) }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAgeAboveTen() { ageBandFor(11) }

    @Test fun profileRejectsBlankName() {
        try {
            ChildProfile(" ", 5)
            fail("Expected invalid profile")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun plannerCreatesNextStageAndDeduplicatesQuestions() {
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

    @Test fun localRetrieverFindsRelevantChunks() {
        val retriever = LocalKnowledgeRetriever(listOf(
            KnowledgeChunk("1", "Planets", "Earth moves around the Sun."),
            KnowledgeChunk("2", "Colours", "Blue and yellow are colours.")
        ))
        assertEquals("1", retriever.retrieve("earth sun").first().id)
    }

    @Test fun localRetrieverReturnsEmptyForUnknownQuery() {
        val retriever = LocalKnowledgeRetriever(listOf(
            KnowledgeChunk("1", "Planets", "Earth moves around the Sun.")
        ))
        assertTrue(retriever.retrieve("dinosaurs").isEmpty())
    }
}
