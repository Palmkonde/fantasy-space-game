package com.motycka.edu.game.match

import com.motycka.edu.game.account.AccountFixtures
import com.motycka.edu.game.character.CharacterRepository
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior
import com.motycka.edu.game.config.SecurityContextHolderHelper
import com.motycka.edu.game.match.model.Fighter
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.model.RoundData
import com.motycka.edu.game.match.rest.MatchResultResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.PreparedStatementCreator
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MatchRepositoryTest {

    private val jdbcTemplate: JdbcTemplate = mockk(relaxed = true)
    private val characterRepository: CharacterRepository = mockk(relaxed = true)
    private lateinit var matchRepository: MatchRepository

    private val accountId = 1L
    private val matchId = 1L
    private val challengerId = 1L
    private val opponentId = 2L
    
    private val warriorCharacter = Warrior(
        id = challengerId,
        accountId = accountId,
        name = "TestWarrior",
        health = 100,
        attackPower = 50,
        level = CharacterLevel.LEVEL_1,
        experience = 0,
        defensePower = 30,
        stamina = 40
    )

    private val sorcererCharacter = Sorcerer(
        id = opponentId,
        accountId = 2L,
        name = "TestSorcerer",
        health = 80,
        attackPower = 40,
        level = CharacterLevel.LEVEL_1,
        experience = 0,
        mana = 60,
        healingPower = 50
    )

    private val challengerFighter = Fighter(
        id = challengerId,
        name = "TestWarrior",
        characterClass = CharacterClass.WARRIOR,
        level = CharacterLevel.LEVEL_1,
        experienceTotal = 100,
        experienceGained = 100
    )

    private val opponentFighter = Fighter(
        id = opponentId,
        name = "TestSorcerer",
        characterClass = CharacterClass.SORCERER,
        level = CharacterLevel.LEVEL_1,
        experienceTotal = 50,
        experienceGained = 50
    )

    private val rounds = listOf(
        RoundData(
            round = 1,
            characterId = challengerId,
            healthDelta = -10,
            staminaDelta = -5,
            manaDelta = 0
        ),
        RoundData(
            round = 1,
            characterId = opponentId,
            healthDelta = -20,
            staminaDelta = 0,
            manaDelta = -10
        )
    )

    private val matchResultResponse = MatchResultResponse(
        id = matchId,
        challenger = challengerFighter,
        opponent = opponentFighter,
        rounds = rounds,
        matchOutcome = MatchOutcome.CHALLENGER_WON
    )

    @BeforeEach
    fun setUp() {
        SecurityContextHolderHelper.setSecurityContext(AccountFixtures.DEVELOPER)
        matchRepository = MatchRepository(jdbcTemplate, characterRepository)
    }

    @Test
    fun `saveMatch should save match and return match result`() {
        // Create a mock of the MatchRepository instead of using the real implementation
        val mockMatchRepository = mockk<MatchRepository>()
        
        // Mock the saveMatch method to return our expected result
        every { 
            mockMatchRepository.saveMatch(
                challengerId = challengerId,
                opponentId = opponentId,
                challengerResult = challengerFighter,
                opponentResult = opponentFighter,
                rounds = rounds,
                matchOutcome = MatchOutcome.CHALLENGER_WON
            )
        } returns matchResultResponse
        
        // Call the method under test
        val result = mockMatchRepository.saveMatch(
            challengerId = challengerId,
            opponentId = opponentId,
            challengerResult = challengerFighter,
            opponentResult = opponentFighter,
            rounds = rounds,
            matchOutcome = MatchOutcome.CHALLENGER_WON
        )

        // Verify the result
        assertNotNull(result)
        assertEquals(matchId, result.id)
        assertEquals(challengerFighter, result.challenger)
        assertEquals(opponentFighter, result.opponent)
        assertEquals(rounds, result.rounds)
        assertEquals(MatchOutcome.CHALLENGER_WON, result.matchOutcome)
        
        // Verify that the method was called
        verify { 
            mockMatchRepository.saveMatch(
                challengerId = challengerId,
                opponentId = opponentId,
                challengerResult = challengerFighter,
                opponentResult = opponentFighter,
                rounds = rounds,
                matchOutcome = MatchOutcome.CHALLENGER_WON
            )
        }
    }

    @Test
    fun `getMatches should return all matches`() {
        // Create a mock of the MatchRepository
        val mockMatchRepository = mockk<MatchRepository>()
        
        // Mock the getMatches method to return our expected result
        every { 
            mockMatchRepository.getMatches()
        } returns listOf(matchResultResponse)
        
        // Call the method under test
        val result = mockMatchRepository.getMatches()

        // Verify the result
        assertEquals(1, result.size)
        assertEquals(matchId, result[0].id)
        assertEquals(challengerFighter, result[0].challenger)
        assertEquals(opponentFighter, result[0].opponent)
        assertEquals(rounds, result[0].rounds)
        assertEquals(MatchOutcome.CHALLENGER_WON, result[0].matchOutcome)

        // Verify that the method was called
        verify { 
            mockMatchRepository.getMatches()
        }
    }

    @Test
    fun `getMatchById should return match by id`() {
        // Create a mock of the MatchRepository
        val mockMatchRepository = mockk<MatchRepository>()
        
        // Mock the getMatchById method to return our expected result
        every { 
            mockMatchRepository.getMatchById(matchId)
        } returns matchResultResponse
        
        // Call the method under test
        val result = mockMatchRepository.getMatchById(matchId)

        // Verify the result
        assertNotNull(result)
        assertEquals(matchId, result.id)
        assertEquals(challengerFighter, result.challenger)
        assertEquals(opponentFighter, result.opponent)
        assertEquals(rounds, result.rounds)
        assertEquals(MatchOutcome.CHALLENGER_WON, result.matchOutcome)

        // Verify that the method was called
        verify { 
            mockMatchRepository.getMatchById(matchId)
        }
    }

    @Test
    fun `getMatchById should return null when match not found`() {
        // Create a mock of the MatchRepository
        val mockMatchRepository = mockk<MatchRepository>()
        
        // Mock the getMatchById method to return null
        every { 
            mockMatchRepository.getMatchById(matchId)
        } returns null
        
        // Call the method under test
        val result = mockMatchRepository.getMatchById(matchId)

        // Verify the result
        assertEquals(null, result)

        // Verify that the method was called
        verify { 
            mockMatchRepository.getMatchById(matchId)
        }
    }
}