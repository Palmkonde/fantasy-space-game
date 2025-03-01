package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.rest.CharacterResponse
import com.motycka.edu.game.leaderboard.rest.LeaderBoardResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LeaderBoardServiceTest {

    private val leaderBoardRepository: LeaderBoardRepository = mockk()
    private val accountService: AccountService = mockk()
    private lateinit var leaderBoardService: LeaderBoardService

    private val accountId = 1L
    private val characterId = 1L
    private val matchId = 1L

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

    @BeforeEach
    fun setUp() {
        leaderBoardService = LeaderBoardService(leaderBoardRepository, accountService)
    }

    @Test
    fun `getLeaderBoard should return leaderboard entries for current account`() {
        // Mock the accountService to return the current account ID
        every { 
            accountService.getCurrentAccountId() 
        } returns accountId

        // Mock the leaderBoardRepository to return our expected result
        every { 
            leaderBoardRepository.getLeaderBoard(null, accountId)
        } returns listOf(leaderBoardResponse)
        
        // Call the method under test
        val result = leaderBoardService.getLeaderBoard(null)

        // Verify the result
        assertEquals(1, result.size)
        assertEquals(1, result[0].position)
        assertEquals(characterId, result[0].character.id)
        assertEquals("TestWarrior", result[0].character.name)
        assertEquals(CharacterClass.WARRIOR, result[0].character.characterClass)
        assertEquals(5, result[0].wins)
        assertEquals(2, result[0].losses)
        assertEquals(1, result[0].draws)

        // Verify that the repository method was called with the correct parameters
        verify { 
            leaderBoardRepository.getLeaderBoard(null, accountId)
        }
    }

    @Test
    fun `getLeaderBoard should filter by class name when provided`() {
        // Mock the accountService to return the current account ID
        every { 
            accountService.getCurrentAccountId() 
        } returns accountId

        // Mock the leaderBoardRepository to return our expected result
        every { 
            leaderBoardRepository.getLeaderBoard("WARRIOR", accountId)
        } returns listOf(leaderBoardResponse)
        
        // Call the method under test
        val result = leaderBoardService.getLeaderBoard("WARRIOR")

        // Verify the result
        assertEquals(1, result.size)
        assertEquals(CharacterClass.WARRIOR, result[0].character.characterClass)

        // Verify that the repository method was called with the correct parameters
        verify { 
            leaderBoardRepository.getLeaderBoard("WARRIOR", accountId)
        }
    }

    @Test
    fun `updateLeaderBoardFromMatch should call repository method`() {
        // Mock the leaderBoardRepository.updateLeaderBoardFromMatch method
        every { 
            leaderBoardRepository.updateLeaderBoardFromMatch(matchId)
        } returns Unit
        
        // Call the method under test
        leaderBoardService.updateLeaderBoardFromMatch(matchId)

        // Verify that the repository method was called with the correct parameter
        verify { 
            leaderBoardRepository.updateLeaderBoardFromMatch(matchId)
        }
    }
}