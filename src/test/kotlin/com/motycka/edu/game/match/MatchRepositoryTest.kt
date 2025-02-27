package com.motycka.edu.game.match

import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.match.model.Fighter
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.model.RoundData
import com.motycka.edu.game.match.rest.MatchResultResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import javax.sql.DataSource

@JdbcTest
@ContextConfiguration(classes = [MatchRepository::class])
class MatchRepositoryTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var matchRepository: MatchRepository

    private val matchId = 1L
    private val challengerId = 1L
    private val opponentId = 2L

    @BeforeEach
    fun setUp() {
        // insert test data
        jdbcTemplate.update(
            "INSERT INTO match (id, challenger_id, opponent_id, outcome) VALUES (?, ?, ?, ?)",
            matchId, challengerId, opponentId, MatchOutcome.CHALLENGER_WON.name
        )
        jdbcTemplate.update(
            "INSERT INTO round_data (match_id, round, character_id, health_delta, stamina_delta, mana_delta) VALUES (?, ?, ?, ?, ?, ?)",
            matchId, 1, challengerId, -10, -5, 0
        )
    }

    @Test
    fun `insertMatch should return inserted match`() {
        val match = MatchResultResponse(
            id = 0L,
            challenger = Fighter(challengerId, "Challenger", CharacterClass.WARRIOR, CharacterLevel.LEVEL_1, 100, 10),
            opponent = Fighter(opponentId, "Opponent", CharacterClass.SORCERER, CharacterLevel.LEVEL_1, 100, 10),
            rounds = listOf(
                RoundData(round = 1, characterId = challengerId, healthDelta = -10, staminaDelta = -5, manaDelta = 0)
            ),
            matchOutcome = MatchOutcome.CHALLENGER_WON
        )
        val result = matchRepository.saveMatch(
            challengerId = challengerId,
            opponentId = opponentId,
            challengerResult = match.challenger,
            opponentResult = match.opponent,
            rounds = match.rounds,
            matchOutcome = match.matchOutcome
        )

        assertNotNull(result)
        assertNotNull(result.id)
        assertEquals(match.copy(id = result.id), result)
    }

    @Test
    fun `selectById should return match when found`() {
        val result = matchRepository.getMatchById(matchId)
        assertNotNull(result)
        assertEquals(matchId, result?.id)
        assertEquals(challengerId, result?.challenger?.id)
        assertEquals(opponentId, result?.opponent?.id)
        assertEquals(MatchOutcome.CHALLENGER_WON, result?.matchOutcome)
        assertEquals(1, result?.rounds?.size)
        assertEquals(-10, result?.rounds?.first()?.healthDelta)
    }

    @Test
    fun `selectById should return null when not found`() {
        val result = matchRepository.getMatchById(999L)
        assertNull(result)
    }

    @Test
    fun `getAllMatches should return all matches`() {
        val result = matchRepository.getMatches()
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertEquals(matchId, result.first().id)
    }
}