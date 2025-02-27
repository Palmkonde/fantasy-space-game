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
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

    @BeforeEach
    fun setUp() {
        every { accountService.getCurrentAccountId() } returns accountId
    }

    @Test
    fun `createNewMatch should create a new match`() {
        val request = MatchCreateRequest(challengerId = 1L, opponentId = 2L, rounds = 10)
        val response = MatchResultResponse(
            id = matchId,
            challenger = Fighter(1L, "Challenger", CharacterClass.WARRIOR, CharacterLevel.LEVEL_1, 100, 10),
            opponent = Fighter(2L, "Opponent", CharacterClass.SORCERER, CharacterLevel.LEVEL_1, 100, 10),
            rounds = listOf(
                RoundData(round = 1, characterId = 1L, healthDelta = -10, staminaDelta = -5, manaDelta = 0),
                RoundData(round = 1, characterId = 2L, healthDelta = -20, staminaDelta = -10, manaDelta = 0)
            ),
            matchOutcome = MatchOutcome.CHALLENGER_WON
        )

        every { matchService.createNewMatch(any(), any()) } returns response

        mockMvc.perform(
            post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(matchId))
            .andExpect(jsonPath("$.matchOutcome").value(MatchOutcome.CHALLENGER_WON.name))
            .andExpect(jsonPath("$.rounds[0].characterId").value(1L))
            .andExpect(jsonPath("$.rounds[0].healthDelta").value(-10))

        verify { matchService.createNewMatch(request, accountId) }
    }

    @Test
    fun `getMatches should return all matches`() {
        val response = listOf(
            MatchResultResponse(
                id = matchId,
                challenger = Fighter(1L, "Challenger", CharacterClass.WARRIOR, CharacterLevel.LEVEL_1, 100, 10),
                opponent = Fighter(2L, "Opponent", CharacterClass.SORCERER, CharacterLevel.LEVEL_1, 100, 10),
                rounds = listOf(
                    RoundData(round = 1, characterId = 1L, healthDelta = -10, staminaDelta = -5, manaDelta = 0),
                    RoundData(round = 1, characterId = 2L, healthDelta = -20, staminaDelta = -10, manaDelta = 0)
                ),
                matchOutcome = MatchOutcome.CHALLENGER_WON
            )
        )

        every { matchService.getAllMatches() } returns response

        mockMvc.perform(get("/api/matches"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(matchId))
            .andExpect(jsonPath("$[0].matchOutcome").value(MatchOutcome.CHALLENGER_WON.name))
            .andExpect(jsonPath("$[0].rounds[0].characterId").value(1L))

        verify { matchService.getAllMatches() }
    }
}