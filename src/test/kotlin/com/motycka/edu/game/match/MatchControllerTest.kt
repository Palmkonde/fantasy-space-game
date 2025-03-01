package com.motycka.edu.game.match

import com.fasterxml.jackson.databind.ObjectMapper
import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.match.model.Fighter
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.model.RoundData
import com.motycka.edu.game.match.rest.MatchCreateRequest
import com.motycka.edu.game.match.rest.MatchResultResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(MatchController::class)
class MatchControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var matchService: MatchService

    @MockBean
    private lateinit var accountService: AccountService

    private val objectMapper = ObjectMapper()

    private val accountId = 1L
    private val matchId = 1L
    private val challengerId = 1L
    private val opponentId = 2L

    private val validMatchResponse = MatchResultResponse(
        id = matchId,
        challenger = Fighter(challengerId, "Warrior", CharacterClass.WARRIOR, CharacterLevel.LEVEL_1, 100, 10),
        opponent = Fighter(opponentId, "Sorcerer", CharacterClass.SORCERER, CharacterLevel.LEVEL_1, 100, 10),
        rounds = listOf(
            RoundData(round = 1, characterId = challengerId, healthDelta = -10, staminaDelta = -5, manaDelta = 0),
            RoundData(round = 1, characterId = opponentId, healthDelta = -20, staminaDelta = -10, manaDelta = 0)
        ),
        matchOutcome = MatchOutcome.CHALLENGER_WON
    )

    @BeforeEach
    fun setUp() {
        `when`(accountService.getCurrentAccountId()).thenReturn(accountId)
    }

    @Test
    fun `createNewMatch should create a new match`() {
        val request = MatchCreateRequest(challengerId = challengerId, opponentId = opponentId, rounds = 10)
        `when`(matchService.createNewMatch(any<MatchCreateRequest>(), eq(accountId))).thenReturn(validMatchResponse)

        mockMvc.perform(
            post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(matchId))
            .andExpect(jsonPath("$.challenger.id").value(challengerId))
            .andExpect(jsonPath("$.challenger.characterClass").value("WARRIOR"))
            .andExpect(jsonPath("$.opponent.id").value(opponentId))
            .andExpect(jsonPath("$.opponent.characterClass").value("SORCERER"))
            .andExpect(jsonPath("$.matchOutcome").value("CHALLENGER_WON"))
            .andExpect(jsonPath("$.rounds.length()").value(2))
            .andExpect(jsonPath("$.rounds[0].healthDelta").value(-10))

        verify(matchService).createNewMatch(any<MatchCreateRequest>(), eq(accountId))
    }

    @Test
    fun `createNewMatch should handle invalid character error`() {
        val request = MatchCreateRequest(challengerId = 999L, opponentId = opponentId, rounds = 10)
        `when`(matchService.createNewMatch(any<MatchCreateRequest>(), eq(accountId)))
            .thenThrow(IllegalArgumentException("Invalid character ID"))

        mockMvc.perform(
            post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify(matchService).createNewMatch(any<MatchCreateRequest>(), eq(accountId))
    }

    @Test
    fun `createNewMatch should validate rounds`() {
        val request = MatchCreateRequest(challengerId = challengerId, opponentId = opponentId, rounds = 0)
        `when`(matchService.createNewMatch(any<MatchCreateRequest>(), eq(accountId)))
            .thenThrow(IllegalArgumentException("Rounds must be greater than 0"))

        mockMvc.perform(
            post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify(matchService).createNewMatch(any<MatchCreateRequest>(), eq(accountId))
    }

    @Test
    fun `getMatches should return all matches`() {
        val matches = listOf(validMatchResponse)
        `when`(matchService.getAllMatches()).thenReturn(matches)

        mockMvc.perform(get("/api/matches"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(matchId))
            .andExpect(jsonPath("$[0].challenger.id").value(challengerId))
            .andExpect(jsonPath("$[0].opponent.id").value(opponentId))
            .andExpect(jsonPath("$[0].matchOutcome").value("CHALLENGER_WON"))
            .andExpect(jsonPath("$[0].rounds.length()").value(2))

        verify(matchService).getAllMatches()
    }

    @Test
    fun `getMatches should handle empty result`() {
        `when`(matchService.getAllMatches()).thenReturn(emptyList())

        mockMvc.perform(get("/api/matches"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))

        verify(matchService).getAllMatches()
    }
}