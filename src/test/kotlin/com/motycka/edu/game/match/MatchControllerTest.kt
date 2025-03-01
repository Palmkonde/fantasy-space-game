package com.motycka.edu.game.match

import com.fasterxml.jackson.databind.ObjectMapper
import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.account.model.Account
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.config.SecurityContextHolderHelper
import com.motycka.edu.game.match.model.Fighter
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.model.RoundData
import com.motycka.edu.game.match.rest.MatchCreateRequest
import com.motycka.edu.game.match.rest.MatchResultResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(MatchController::class)
@Import(MatchControllerTest.TestConfig::class)
class MatchControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun matchService(): MatchService = mockk(relaxed = true)
        
        @Bean
        @Primary
        fun accountService(): AccountService = mockk(relaxed = true)
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var matchService: MatchService

    @Autowired
    private lateinit var accountService: AccountService

    private val objectMapper = ObjectMapper()

    // Constants
    private val accountId = 1L
    private val matchId = 1L
    private val challengerId = 1L
    private val opponentId = 2L
    
    // Test account
    private val testAccount = Account(
        id = accountId,
        name = "Test User",
        username = "testuser",
        password = "password"
    )

    private val matchCreateRequest = MatchCreateRequest(
        challengerId = challengerId,
        opponentId = opponentId,
        rounds = 10
    )

    private val matchResultResponse = MatchResultResponse(
        id = matchId,
        challenger = Fighter(
            id = challengerId,
            name = "TestWarrior",
            characterClass = CharacterClass.WARRIOR,
            level = CharacterLevel.LEVEL_1,
            experienceTotal = 100,
            experienceGained = 100
        ),
        opponent = Fighter(
            id = opponentId,
            name = "TestSorcerer",
            characterClass = CharacterClass.SORCERER,
            level = CharacterLevel.LEVEL_1,
            experienceTotal = 50,
            experienceGained = 50
        ),
        rounds = listOf(
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
        ),
        matchOutcome = MatchOutcome.CHALLENGER_WON
    )

    @BeforeEach
    fun setUp() {
        SecurityContextHolderHelper.setSecurityContext(testAccount)
        every { accountService.getCurrentAccountId() } returns accountId
    }

    @Test
    fun `createNewMatch should create match and return 201 status`() {
        // Mock the match service
        every { 
            matchService.createNewMatch(matchCreateRequest, accountId)
        } returns matchResultResponse

        // Perform the request
        mockMvc.perform(
            post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(matchCreateRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(matchId))
            .andExpect(jsonPath("$.challenger.id").value(challengerId))
            .andExpect(jsonPath("$.opponent.id").value(opponentId))
            .andExpect(jsonPath("$.matchOutcome").value(MatchOutcome.CHALLENGER_WON.toString()))

        // Verify the interactions
        verify { matchService.createNewMatch(matchCreateRequest, accountId) }
    }

    @Test
    fun `getMatches should return all matches`() {
        // Mock the match service
        every { matchService.getAllMatches() } returns listOf(matchResultResponse)

        // Perform the request
        mockMvc.perform(get("/api/matches"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(matchId))
            .andExpect(jsonPath("$[0].challenger.id").value(challengerId))
            .andExpect(jsonPath("$[0].opponent.id").value(opponentId))
            .andExpect(jsonPath("$[0].matchOutcome").value(MatchOutcome.CHALLENGER_WON.toString()))

        // Verify the interactions
        verify { matchService.getAllMatches() }
    }

    @Test
    fun `createNewMatch should handle exceptions`() {
        // Mock the match service to throw an exception
        every { 
            matchService.createNewMatch(matchCreateRequest, accountId)
        } throws IllegalArgumentException("Invalid match request")

        // Perform the request
        mockMvc.perform(
            post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(matchCreateRequest))
        )
            .andExpect(status().isBadRequest)

        // Verify the interactions
        verify { matchService.createNewMatch(matchCreateRequest, accountId) }
    }
    
    @Test
    fun `createNewMatch should handle server errors`() {
        // Mock the match service to throw an exception
        every { 
            matchService.createNewMatch(matchCreateRequest, accountId)
        } throws RuntimeException("Server error")

        // Perform the request
        mockMvc.perform(
            post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(matchCreateRequest))
        )
            .andExpect(status().isInternalServerError)

        // Verify the interactions
        verify { matchService.createNewMatch(matchCreateRequest, accountId) }
    }
}