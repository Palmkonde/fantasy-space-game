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

class MatchServiceTest {

    private lateinit var matchService: MatchService
    private lateinit var matchRepository: MatchRepository
    private lateinit var characterService: CharacterService
    private lateinit var leaderBoardService: LeaderBoardService

    private val accountId = 1L
    private val matchId = 1L
    private val challengerId = 1L
    private val opponentId = 2L

    @BeforeEach
    fun setUp() {
        matchRepository = mockk()
        characterService = mockk()
        leaderBoardService = mockk()
        matchService = MatchService(matchRepository, characterService, leaderBoardService)
    }

    @Test
    fun `createNewMatch should create a new match`() {
        val request = MatchCreateRequest(challengerId = challengerId, opponentId = opponentId, rounds = 10)
        val challenger = Warrior(challengerId, accountId, "Challenger", 100, 50, CharacterLevel.LEVEL_1, 40, 50, 30)
        val opponent = Sorcerer(opponentId, accountId, "Opponent", 100, 50, CharacterLevel.LEVEL_1, 40, 50, 30)
        every { characterService.getChallengers(accountId) } returns listOf(challenger)
        every { characterService.getOpponents(accountId) } returns listOf(opponent)
        every { matchRepository.saveMatch(any(), any(), any(), any(), any(), any()) } returns MatchResultResponse(
            id = matchId,
            challenger = Fighter(challengerId, "Challenger", CharacterClass.WARRIOR, CharacterLevel.LEVEL_1, 100, 10),
            opponent = Fighter(opponentId, "Opponent", CharacterClass.SORCERER, CharacterLevel.LEVEL_1, 100, 10),
            rounds = listOf(
                RoundData(round = 1, characterId = challengerId, healthDelta = -10, staminaDelta = -5, manaDelta = 0),
                RoundData(round = 1, characterId = opponentId, healthDelta = -20, staminaDelta = -10, manaDelta = 0)
            ),
            matchOutcome = MatchOutcome.CHALLENGER_WON
        )

        val result = matchService.createNewMatch(request, accountId)

        assertNotNull(result)
        assertEquals(matchId, result.id)
        assertEquals(MatchOutcome.CHALLENGER_WON, result.matchOutcome)
        assertEquals(2, result.rounds.size)
        verify { matchRepository.saveMatch(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getAllMatches should return all matches`() {
        val response = listOf(
            MatchResultResponse(
                id = matchId,
                challenger = Fighter(challengerId, "Challenger", CharacterClass.WARRIOR, CharacterLevel.LEVEL_1, 100, 10),
                opponent = Fighter(opponentId, "Opponent", CharacterClass.SORCERER, CharacterLevel.LEVEL_1, 100, 10),
                rounds = listOf(
                    RoundData(round = 1, characterId = challengerId, healthDelta = -10, staminaDelta = -5, manaDelta = 0),
                    RoundData(round = 1, characterId = opponentId, healthDelta = -20, staminaDelta = -10, manaDelta = 0)
                ),
                matchOutcome = MatchOutcome.CHALLENGER_WON
            )
        )

        every { matchRepository.getMatches() } returns response

        val result = matchService.getAllMatches()

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(matchId, result.first().id)
        verify { matchRepository.getMatches() }
    }
}