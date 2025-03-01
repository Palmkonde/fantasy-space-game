package com.motycka.edu.game.character

import com.fasterxml.jackson.databind.ObjectMapper
import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.account.model.Account
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior
import com.motycka.edu.game.character.rest.CharacterCreateRequest
import com.motycka.edu.game.character.rest.CharacterLevelUpRequest
import com.motycka.edu.game.character.rest.CharacterResponse
import com.motycka.edu.game.config.SecurityContextHolderHelper
import com.motycka.edu.game.config.TestSecurityConfiguration
import com.motycka.edu.game.error.NotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(CharacterController::class)
@Import(CharacterControllerTest.TestConfig::class, TestSecurityConfiguration::class)
class CharacterControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun characterService(): CharacterService = mockk(relaxed = true)
        
        @Bean
        @Primary
        fun accountService(): AccountService = mockk(relaxed = true)
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var characterService: CharacterService

    @Autowired
    private lateinit var accountService: AccountService

    private val objectMapper = ObjectMapper()

    // Constants
    private val accountId = 1L
    
    // Test account
    private val testAccount = Account(
        id = accountId,
        name = "Test User",
        username = "testuser",
        password = "password"
    )
    
    // Test fixtures
    private val warriorCharacter = Warrior(
        id = 1L,
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
        id = 2L,
        accountId = 2L,
        name = "TestSorcerer",
        health = 80,
        attackPower = 40,
        level = CharacterLevel.LEVEL_1,
        experience = 0,
        mana = 60,
        healingPower = 50
    )

    private val createWarriorRequest = CharacterCreateRequest(
        name = "NewWarrior",
        characterClass = CharacterClass.WARRIOR,
        health = 100,
        attackPower = 50,
        defensePower = 30,
        stamina = 40,
        mana = null,
        healingPower = null
    )

    private val levelUpWarriorRequest = CharacterLevelUpRequest(
        name = "UpdatedWarrior",
        health = 120,
        attackPower = 60,
        defensePower = 40,
        stamina = 50,
        mana = null,
        healingPower = null
    )

    @BeforeEach
    fun setUp() {
        SecurityContextHolderHelper.setSecurityContext(testAccount)
        every { accountService.getCurrentAccountId() } returns accountId
    }

    @Test
    fun `getCharacter should return filtered characters`() {
        val characters = listOf(warriorCharacter, sorcererCharacter)
        every { characterService.getCharacters("WARRIOR", "Test") } returns characters

        mockMvc.perform(get("/api/characters")
            .param("class", "WARRIOR")
            .param("name", "Test"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))

        verify { characterService.getCharacters("WARRIOR", "Test") }
    }

    @Test
    fun `getCharacterById should return character`() {
        every { characterService.getCharacterById(1L) } returns warriorCharacter

        mockMvc.perform(get("/api/characters/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("TestWarrior"))
            .andExpect(jsonPath("$.characterClass").value("WARRIOR"))

        verify { characterService.getCharacterById(1L) }
    }

    @Test
    fun `getChallengers should return owned characters`() {
        val challengers = listOf(warriorCharacter)
        every { characterService.getChallengers(accountId) } returns challengers

        mockMvc.perform(get("/api/characters/challengers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))

        verify { characterService.getChallengers(accountId) }
    }

    @Test
    fun `getOpponents should return non-owned characters`() {
        val opponents = listOf(sorcererCharacter)
        every { characterService.getOpponents(accountId) } returns opponents

        mockMvc.perform(get("/api/characters/opponents"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(2))

        verify { characterService.getOpponents(accountId) }
    }

    @Test
    fun `postCharacter should create character`() {
        every { characterService.createCharacter(any(), any()) } returns warriorCharacter

        mockMvc.perform(post("/api/characters")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createWarriorRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("TestWarrior"))

        verify { characterService.createCharacter(any(), any()) }
    }

    @Test
    fun `putCharacter should update character`() {
        val updatedWarrior = Warrior(
            id = 1L,
            accountId = accountId,
            name = "UpdatedWarrior",
            health = 120,
            attackPower = 60,
            defensePower = 40,
            stamina = 50,
            level = CharacterLevel.LEVEL_1,
            experience = 0
        )
        every { characterService.upLevelCharacterById(any(), any()) } returns updatedWarrior

        mockMvc.perform(put("/api/characters/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(levelUpWarriorRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("UpdatedWarrior"))
            .andExpect(jsonPath("$.health").value(120))

        verify { characterService.upLevelCharacterById(any(), any()) }
    }

    @Test
    fun `putCharacter should handle errors`() {
        every { characterService.upLevelCharacterById(any(), any()) } throws RuntimeException("Failed to update character")

        mockMvc.perform(put("/api/characters/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(levelUpWarriorRequest)))
            .andExpect(status().isInternalServerError)

        verify { characterService.upLevelCharacterById(any(), any()) }
    }

    @Test
    fun `postCharacter should validate warrior point distribution`() {
        val invalidRequest = CharacterCreateRequest(
            name = "InvalidWarrior",
            characterClass = CharacterClass.WARRIOR,
            health = 200,
            attackPower = 100,
            defensePower = 80,
            stamina = 90,
            mana = null,
            healingPower = null
        )
        every { characterService.createCharacter(any(), any()) } throws IllegalArgumentException("Invalid point distribution")

        mockMvc.perform(post("/api/characters")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest)

        verify { characterService.createCharacter(any(), any()) }
    }

    @Test
    fun `postCharacter should validate sorcerer point distribution`() {
        val request = CharacterCreateRequest(
            name = "InvalidSorcerer",
            characterClass = CharacterClass.SORCERER,
            health = 150,
            attackPower = 80,
            defensePower = null,
            stamina = null,
            mana = 100,
            healingPower = 90
        )

        every { characterService.createCharacter(any(), any()) } throws IllegalArgumentException("Invalid point distribution for sorcerer")

        mockMvc.perform(post("/api/characters")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)

        verify { characterService.createCharacter(any(), any()) }
    }

    @Test
    fun `putCharacter should validate warrior level up point distribution`() {
        val request = CharacterLevelUpRequest(
            name = "InvalidWarriorUpdate",
            health = 200,
            attackPower = 100,
            defensePower = 80,
            stamina = 90,
            mana = null,
            healingPower = null
        )

        every { characterService.upLevelCharacterById(any(), any()) } throws IllegalArgumentException("Invalid point distribution for warrior level up")

        mockMvc.perform(put("/api/characters/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError)

        verify { characterService.upLevelCharacterById(any(), any()) }
    }

    @Test
    fun `putCharacter should validate sorcerer level up point distribution`() {
        val request = CharacterLevelUpRequest(
            name = "InvalidSorcererUpdate",
            health = 150,
            attackPower = 80,
            defensePower = null,
            stamina = null,
            mana = 100,
            healingPower = 90
        )

        every { characterService.upLevelCharacterById(any(), any()) } throws IllegalArgumentException("Invalid point distribution for sorcerer level up")

        mockMvc.perform(put("/api/characters/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError)

        verify { characterService.upLevelCharacterById(any(), any()) }
    }

    @Test
    fun `getCharacterById should handle not found`() {
        every { characterService.getCharacterById(999L) } throws NotFoundException("Character not found")

        mockMvc.perform(get("/api/characters/999"))
            .andExpect(status().isNotFound)

        verify { characterService.getCharacterById(999L) }
    }

    @Test
    fun `postCharacter should validate character name uniqueness`() {
        val request = CharacterCreateRequest(
            name = "ExistingName",
            characterClass = CharacterClass.WARRIOR,
            health = 100,
            attackPower = 50,
            defensePower = 30,
            stamina = 40,
            mana = null,
            healingPower = null
        )

        every { characterService.createCharacter(any(), any()) } throws IllegalArgumentException("Character name already exists")

        mockMvc.perform(post("/api/characters")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)

        verify { characterService.createCharacter(any(), any()) }
    }
}