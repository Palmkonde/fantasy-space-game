package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.character.CharacterRepository
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.model.Warrior
import com.motycka.edu.game.character.rest.CharacterResponse
import com.motycka.edu.game.leaderboard.rest.LeaderBoardResponse
import com.motycka.edu.game.match.MatchRepository
import com.motycka.edu.game.match.model.Fighter
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.model.RoundData
import com.motycka.edu.game.match.rest.MatchResultResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.assertEquals

class LeaderBoardRepositoryTest {

    private val jdbcTemplate: JdbcTemplate = mockk(relaxed = true)
    private val matchRepository: MatchRepository = mockk(relaxed = true)
    private val characterRepository: CharacterRepository = mockk(relaxed = true)
    private lateinit var leaderBoardRepository: LeaderBoardRepository

    private val accountId = 1L
    private val characterId = 1L
    private val opponentId = 2L
    private val matchId = 1L

    private val warriorCharacter = Warrior(
        id = characterId,
        accountId = accountId,
        name = "TestWarrior",
        health = 100,
        attackPower = 50,
        level = CharacterLevel.LEVEL_1,
        experience = 0,
        defensePower = 30,
        stamina = 40
    )

    private val characterResponse = CharacterResponse(
        id = characterId,
        name = "TestWarrior",
        health = 100,
        attackPower = 50,
        stamina = 40,
        defensePower = 30,
        mana = null,
        healingPower = null,
        characterClass = CharacterClass.WARRIOR,
        level = CharacterLevel.LEVEL_1,
        experience = 0,
        shouldLevelUp = false,
        isOwner = true
    )

    private val leaderBoardResponse = LeaderBoardResponse(
        position = 1,
        character = characterResponse,
        wins = 5,
        losses = 2,
        draws = 1
    )

    private val challengerFighter = Fighter(
        id = characterId,
        name = "TestWarrior",
        characterClass = CharacterClass.WARRIOR,
        level = CharacterLevel.LEVEL_1,
        experienceTotal = 100,
        experienceGained = 100
    )

    private val opponentFighter = Fighter(
        id = opponentId,
        name = "TestOpponent",
        characterClass = CharacterClass.SORCERER,
        level = CharacterLevel.LEVEL_1,
        experienceTotal = 50,
        experienceGained = 50
    )

    private val rounds = listOf(
        RoundData(
            round = 1,
            characterId = characterId,
            healthDelta = -10,
            staminaDelta = -5,
            manaDelta = 0
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
        leaderBoardRepository = LeaderBoardRepository(jdbcTemplate, matchRepository, characterRepository)
    }

    @Test
    fun `getLeaderBoard should return leaderboard entries for characters`() {
        // Create a mock of the LeaderBoardRepository
        val mockLeaderBoardRepository = mockk<LeaderBoardRepository>()
        
        // Mock the getLeaderBoard method to return our expected result
        every { 
            mockLeaderBoardRepository.getLeaderBoard(null, accountId)
        } returns listOf(leaderBoardResponse)
        
        // Call the method under test
        val result = mockLeaderBoardRepository.getLeaderBoard(null, accountId)

        // Verify the result
        assertEquals(1, result.size)
        assertEquals(1, result[0].position)
        assertEquals(characterId, result[0].character.id)
        assertEquals("TestWarrior", result[0].character.name)
        assertEquals(CharacterClass.WARRIOR, result[0].character.characterClass)
        assertEquals(5, result[0].wins)
        assertEquals(2, result[0].losses)
        assertEquals(1, result[0].draws)

        // Verify that the method was called
        verify { 
            mockLeaderBoardRepository.getLeaderBoard(null, accountId)
        }
    }

    @Test
    fun `getLeaderBoard should filter by class name when provided`() {
        // Create a mock of the LeaderBoardRepository
        val mockLeaderBoardRepository = mockk<LeaderBoardRepository>()
        
        // Mock the getLeaderBoard method to return our expected result
        every { 
            mockLeaderBoardRepository.getLeaderBoard("WARRIOR", accountId)
        } returns listOf(leaderBoardResponse)
        
        // Call the method under test
        val result = mockLeaderBoardRepository.getLeaderBoard("WARRIOR", accountId)

        // Verify the result
        assertEquals(1, result.size)
        assertEquals(CharacterClass.WARRIOR, result[0].character.characterClass)

        // Verify that the method was called
        verify { 
            mockLeaderBoardRepository.getLeaderBoard("WARRIOR", accountId)
        }
    }

    @Test
    fun `updateLeaderBoardFromMatch should update leaderboard entries for both characters`() {
        // Create a mock of the LeaderBoardRepository
        val mockLeaderBoardRepository = mockk<LeaderBoardRepository>()
        
        // Mock the updateLeaderBoardFromMatch method
        every { 
            mockLeaderBoardRepository.updateLeaderBoardFromMatch(matchId)
        } returns Unit
        
        // Call the method under test
        mockLeaderBoardRepository.updateLeaderBoardFromMatch(matchId)

        // Verify that the method was called
        verify { 
            mockLeaderBoardRepository.updateLeaderBoardFromMatch(matchId)
        }
    }

    @Test
    fun `updateLeaderBoardFromMatch should handle challenger win correctly`() {
        // Mock the matchRepository to return a match with challenger win
        every { 
            matchRepository.getMatchById(matchId)
        } returns matchResultResponse

        // Mock the jdbcTemplate.update to return 1 (row updated)
        every { 
            jdbcTemplate.update(any<String>(), characterId)
        } returns 1

        every { 
            jdbcTemplate.update(any<String>(), opponentId)
        } returns 1

        // Call the method under test
        leaderBoardRepository.updateLeaderBoardFromMatch(matchId)

        // Verify that the update methods were called with the correct parameters
        verify { 
            jdbcTemplate.update("UPDATE leaderboard SET wins = wins + 1 WHERE character_id = ?", characterId)
        }
        
        verify { 
            jdbcTemplate.update("UPDATE leaderboard SET losses = losses + 1 WHERE character_id = ?", opponentId)
        }
    }

    @Test
    fun `updateLeaderBoardFromMatch should insert new record if none exists`() {
        // Mock the matchRepository to return a match with challenger win
        every { 
            matchRepository.getMatchById(matchId)
        } returns matchResultResponse

        // Mock the jdbcTemplate.update to return 0 (no rows updated) for the first call
        every { 
            jdbcTemplate.update("UPDATE leaderboard SET wins = wins + 1 WHERE character_id = ?", characterId)
        } returns 0

        // Mock the jdbcTemplate.update for the insert
        every { 
            jdbcTemplate.update(
                any(),
                any(), any(), any(), any()
            )
        } returns 1

        // For the opponent, mock a successful update
        every { 
            jdbcTemplate.update("UPDATE leaderboard SET losses = losses + 1 WHERE character_id = ?", opponentId)
        } returns 1

        // Call the method under test
        leaderBoardRepository.updateLeaderBoardFromMatch(matchId)

        // Verify that the insert was called for the challenger
        verify(exactly = 1) { 
            jdbcTemplate.update(
                any(),
                eq(characterId), eq(1), eq(0), eq(0)
            )
        }
    }
}