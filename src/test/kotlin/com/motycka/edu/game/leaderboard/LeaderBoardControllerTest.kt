package com.motycka.edu.game.leaderboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.account.model.Account
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.rest.CharacterResponse
import com.motycka.edu.game.config.SecurityContextHolderHelper
import com.motycka.edu.game.config.TestSecurityConfiguration
import com.motycka.edu.game.leaderboard.rest.LeaderBoardResponse
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(LeaderBoardController::class)
@Import(LeaderBoardControllerTest.TestConfig::class, TestSecurityConfiguration::class)
class LeaderBoardControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun leaderBoardService(): LeaderBoardService = mockk(relaxed = true)
        
        @Bean
        @Primary
        fun accountService(): AccountService = mockk(relaxed = true)
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var leaderBoardService: LeaderBoardService

    @Autowired
    private lateinit var accountService: AccountService

    private val objectMapper = ObjectMapper()

    // Constants
    private val accountId = 1L
    private val characterId = 1L
    
    // Test account
    private val testAccount = Account(
        id = accountId,
        name = "Test User",
        username = "testuser",
        password = "password"
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

    @BeforeEach
    fun setUp() {
        SecurityContextHolderHelper.setSecurityContext(testAccount)
        every { accountService.getCurrentAccountId() } returns accountId
    }

    @Test
    fun `getLeaderBoard should return all leaderboard entries when no class filter`() {
        // Mock the leaderBoardService to return our expected result
        every { 
            leaderBoardService.getLeaderBoard(null)
        } returns listOf(leaderBoardResponse)
        
        // Perform the GET request
        mockMvc.perform(get("/api/leaderboards"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].position").value(1))
            .andExpect(jsonPath("$[0].character.id").value(characterId))
            .andExpect(jsonPath("$[0].character.name").value("TestWarrior"))
            .andExpect(jsonPath("$[0].character.characterClass").value("WARRIOR"))
            .andExpect(jsonPath("$[0].wins").value(5))
            .andExpect(jsonPath("$[0].losses").value(2))
            .andExpect(jsonPath("$[0].draws").value(1))

        // Verify that the service method was called with the correct parameter
        verify { 
            leaderBoardService.getLeaderBoard(null)
        }
    }

    @Test
    fun `getLeaderBoard should return filtered entries when class filter provided`() {
        // Mock the leaderBoardService to return our expected result
        every { 
            leaderBoardService.getLeaderBoard("WARRIOR")
        } returns listOf(leaderBoardResponse)
        
        // Perform the GET request with class parameter
        mockMvc.perform(get("/api/leaderboards?class=WARRIOR"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].position").value(1))
            .andExpect(jsonPath("$[0].character.characterClass").value("WARRIOR"))

        // Verify that the service method was called with the correct parameter
        verify { 
            leaderBoardService.getLeaderBoard("WARRIOR")
        }
    }

    @Test
    fun `getLeaderBoard should return empty array when no entries found`() {
        // Mock the leaderBoardService to return an empty list
        every { 
            leaderBoardService.getLeaderBoard(null)
        } returns emptyList()
        
        // Perform the GET request
        mockMvc.perform(get("/api/leaderboards"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)

        // Verify that the service method was called
        verify { 
            leaderBoardService.getLeaderBoard(null)
        }
    }

    @Test
    fun `getLeaderBoard should handle exceptions`() {
        // Mock the leaderBoardService to throw an exception
        every { 
            leaderBoardService.getLeaderBoard("INVALID_CLASS")
        } throws IllegalArgumentException("Invalid character class")
        
        // Perform the GET request with invalid class parameter
        mockMvc.perform(get("/api/leaderboards?class=INVALID_CLASS"))
            .andExpect(status().isBadRequest)

        // Verify that the service method was called
        verify { 
            leaderBoardService.getLeaderBoard("INVALID_CLASS")
        }
    }
} 