package com.motycka.edu.game.match

import com.motycka.edu.game.account.AccountFixtures
import com.motycka.edu.game.character.CharacterService
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior
import com.motycka.edu.game.config.SecurityContextHolderHelper
import com.motycka.edu.game.leaderboard.LeaderBoardService
import com.motycka.edu.game.match.model.Fighter
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.model.RoundData
import com.motycka.edu.game.match.rest.MatchCreateRequest
import com.motycka.edu.game.match.rest.MatchResultResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MatchServiceTest {

    private val matchRepository: MatchRepository = mockk()
    private val characterService: CharacterService = mockk()
    private val leaderBoardService: LeaderBoardService = mockk()
    private val matchService: MatchService = MatchService(
        matchRepository = matchRepository,
        characterService = characterService,
        leaderBoardService = leaderBoardService
    )

    private val accountId = 1L
    private val matchId = 1L
    private val challengerId = 1L
    private val opponentId = 2L

    private val warrior = Warrior(challengerId, accountId, "Warrior", 100, 50, CharacterLevel.LEVEL_1, 40, 50, 30)
    private val sorcerer = Sorcerer(opponentId, accountId, "Sorcerer", 100, 50, CharacterLevel.LEVEL_1, 40, 50, 30)

    @BeforeEach
    fun setUp() {
        SecurityContextHolderHelper.setSecurityContext(AccountFixtures.DEVELOPER)
    }

    @Test
    fun `createNewMatch should create match with valid characters`() {
        val request = MatchCreateRequest(challengerId = challengerId, opponentId = opponentId, rounds = 10)
        
        every { characterService.getChallengers(accountId) } returns listOf(warrior)
        every { characterService.getOpponents(accountId) } returns listOf(sorcerer)
        every { 
            matchRepository.saveMatch(
                challengerId,
                opponentId,
                any(),
                any(),
                any(),
                any()
            )
        } returns createMatchResponse(MatchOutcome.CHALLENGER_WON)

        every { leaderBoardService.updateLeaderBoardFromMatch(matchId) } returns Unit

        val result = matchService.createNewMatch(request, accountId)

        verify { characterService.getChallengers(accountId) }
        verify { characterService.getOpponents(accountId) }
        verify { 
            matchRepository.saveMatch(
                challengerId,
                opponentId,
                any(),
                any(),
                any(),
                any()
            )
        }
        verify { leaderBoardService.updateLeaderBoardFromMatch(matchId) }

        assertEquals(matchId, result.id)
        assertEquals(challengerId, result.challenger.id)
        assertEquals(opponentId, result.opponent.id)
        assertTrue(result.rounds.isNotEmpty())
    }

    @Test
    fun `createNewMatch should handle invalid challenger`() {
        val request = MatchCreateRequest(challengerId = 999L, opponentId = opponentId, rounds = 10)
        
        every { characterService.getChallengers(accountId) } returns emptyList()
        every { characterService.getOpponents(accountId) } returns listOf(sorcerer)

        val exception = assertThrows<IllegalStateException> {
            matchService.createNewMatch(request, accountId)
        }
        
        assertTrue(exception.message!!.contains("Challenger"))

        verify { characterService.getChallengers(accountId) }
        verify { characterService.getOpponents(accountId) }
        verify(exactly = 0) { 
            matchRepository.saveMatch(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `createNewMatch should handle invalid opponent`() {
        val request = MatchCreateRequest(challengerId = challengerId, opponentId = 999L, rounds = 10)
        
        every { characterService.getChallengers(accountId) } returns listOf(warrior)
        every { characterService.getOpponents(accountId) } returns emptyList()

        val exception = assertThrows<IllegalStateException> {
            matchService.createNewMatch(request, accountId)
        }
        
        assertTrue(exception.message!!.contains("Opponent"))

        verify { characterService.getChallengers(accountId) }
        verify { characterService.getOpponents(accountId) }
        verify(exactly = 0) { 
            matchRepository.saveMatch(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `createNewMatch should handle invalid rounds`() {
        val request = MatchCreateRequest(challengerId = challengerId, opponentId = opponentId, rounds = 0)
        
        // No need to mock challenger or opponent since the rounds validation happens first
        
        val exception = assertThrows<IllegalArgumentException> {
            matchService.createNewMatch(request, accountId)
        }
        
        assertEquals("Number of rounds must be greater than 0", exception.message)
        
        verify(exactly = 0) { 
            matchRepository.saveMatch(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `getAllMatches should return all matches`() {
        val matches = listOf(createMatchResponse(MatchOutcome.CHALLENGER_WON))
        every { matchRepository.getMatches() } returns matches

        val result = matchService.getAllMatches()

        assertEquals(1, result.size)
        assertEquals(matchId, result[0].id)
        verify { matchRepository.getMatches() }
    }
    
    @Test
    fun `getMatchById should return match by id`() {
        val match = createMatchResponse(MatchOutcome.CHALLENGER_WON)
        every { matchRepository.getMatchById(matchId) } returns match

        val result = matchService.getMatchById(matchId)

        assertEquals(matchId, result.id)
        assertEquals(challengerId, result.challenger.id)
        assertEquals(opponentId, result.opponent.id)
        verify { matchRepository.getMatchById(matchId) }
    }
    
    @Test
    fun `getMatchById should throw exception when match not found`() {
        every { matchRepository.getMatchById(matchId) } returns null

        val exception = assertThrows<IllegalStateException> {
            matchService.getMatchById(matchId)
        }
        
        assertTrue(exception.message!!.contains("No match found with ID"))
        verify { matchRepository.getMatchById(matchId) }
    }

    private fun createMatchResponse(outcome: MatchOutcome) = MatchResultResponse(
        id = matchId,
        challenger = Fighter(challengerId, "Warrior", CharacterClass.WARRIOR, CharacterLevel.LEVEL_1, 100, 10),
        opponent = Fighter(opponentId, "Sorcerer", CharacterClass.SORCERER, CharacterLevel.LEVEL_1, 100, 10),
        rounds = listOf(
            RoundData(round = 1, characterId = challengerId, healthDelta = -10, staminaDelta = -5, manaDelta = 0),
            RoundData(round = 1, characterId = opponentId, healthDelta = -20, staminaDelta = -10, manaDelta = 0)
        ),
        matchOutcome = outcome
    )
}