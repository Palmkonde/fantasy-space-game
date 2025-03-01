package com.motycka.edu.game.match

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.character.CharacterService
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior
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
import org.mockito.Mockito.*

class MatchServiceTest {

    private lateinit var matchService: MatchService
    private lateinit var matchRepository: MatchRepository
    private lateinit var characterService: CharacterService
    private lateinit var leaderBoardService: LeaderBoardService

    private val accountId = 1L
    private val matchId = 1L
    private val challengerId = 1L
    private val opponentId = 2L

    private val warrior = Warrior(challengerId, accountId, "Warrior", 100, 50, CharacterLevel.LEVEL_1, 40, 50, 30)
    private val sorcerer = Sorcerer(opponentId, accountId, "Sorcerer", 100, 50, CharacterLevel.LEVEL_1, 40, 50, 30)

    @BeforeEach
    fun setUp() {
        matchRepository = mock(MatchRepository::class.java)
        characterService = mock(CharacterService::class.java)
        leaderBoardService = mock(LeaderBoardService::class.java)
        matchService = MatchService(matchRepository, characterService, leaderBoardService)
    }

    @Test
    fun `createNewMatch should create match with valid characters`() {
        val request = MatchCreateRequest(challengerId = challengerId, opponentId = opponentId, rounds = 10)
        
        `when`(characterService.getChallengers(eq(accountId))).thenReturn(listOf(warrior))
        `when`(characterService.getOpponents(eq(accountId))).thenReturn(listOf(sorcerer))
        `when`(matchRepository.saveMatch(
            eq(challengerId),
            eq(opponentId),
            any(),
            any(),
            any(),
            any()
        )).thenReturn(createMatchResponse(MatchOutcome.CHALLENGER_WON))

        val result = matchService.createNewMatch(request, accountId)

        verify(characterService).getChallengers(eq(accountId))
        verify(characterService).getOpponents(eq(accountId))
        verify(matchRepository).saveMatch(
            eq(challengerId),
            eq(opponentId),
            any(),
            any(),
            any(),
            any()
        )
        verify(leaderBoardService).updateLeaderBoardFromMatch(eq(matchId))

        assert(result.id == matchId)
        assert(result.challenger.id == challengerId)
        assert(result.opponent.id == opponentId)
        assert(result.rounds.isNotEmpty())
    }

    @Test
    fun `createNewMatch should handle invalid challenger`() {
        val request = MatchCreateRequest(challengerId = 999L, opponentId = opponentId, rounds = 10)
        
        `when`(characterService.getChallengers(eq(accountId))).thenReturn(emptyList())
        `when`(characterService.getOpponents(eq(accountId))).thenReturn(listOf(sorcerer))

        assertThrows<IllegalArgumentException> {
            matchService.createNewMatch(request, accountId)
        }

        verify(characterService).getChallengers(eq(accountId))
        verify(characterService).getOpponents(eq(accountId))
        verify(matchRepository, never()).saveMatch(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `createNewMatch should handle invalid opponent`() {
        val request = MatchCreateRequest(challengerId = challengerId, opponentId = 999L, rounds = 10)
        
        `when`(characterService.getChallengers(eq(accountId))).thenReturn(listOf(warrior))
        `when`(characterService.getOpponents(eq(accountId))).thenReturn(emptyList())

        assertThrows<IllegalArgumentException> {
            matchService.createNewMatch(request, accountId)
        }

        verify(characterService).getChallengers(eq(accountId))
        verify(characterService).getOpponents(eq(accountId))
        verify(matchRepository, never()).saveMatch(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `createNewMatch should handle invalid rounds`() {
        val request = MatchCreateRequest(challengerId = challengerId, opponentId = opponentId, rounds = 0)
        
        assertThrows<IllegalArgumentException> {
            matchService.createNewMatch(request, accountId)
        }

        verify(matchRepository, never()).saveMatch(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `getAllMatches should return all matches`() {
        val matches = listOf(createMatchResponse(MatchOutcome.CHALLENGER_WON))
        `when`(matchRepository.getMatches()).thenReturn(matches)

        val result = matchService.getAllMatches()

        assert(result.size == 1)
        assert(result[0].id == matchId)
        verify(matchRepository).getMatches()
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