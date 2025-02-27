package com.motycka.edu.game.character

import com.fasterxml.jackson.databind.ObjectMapper
import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior
import com.motycka.edu.game.character.rest.CharacterCreateRequest
import com.motycka.edu.game.character.rest.CharacterLevelUpRequest
import com.motycka.edu.game.character.rest.CharacterResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(CharacterController::class)
class CharacterControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var characterService: CharacterService

    @MockBean
    private lateinit var accountService: AccountService

    private val accountId = 1L
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

    @BeforeEach
    fun setUp() {
        `when`(accountService.getCurrentAccountId()).thenReturn(accountId)
    }

    @Test
    fun `getCharacter should return filtered characters`() {
        val characters = listOf(warriorCharacter, sorcererCharacter)
        `when`(characterService.getCharacters(anyString(), anyString())).thenReturn(characters)

        mockMvc.perform(get("/api/characters")
            .param("class", "WARRIOR")
            .param("name", "Test"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))

        verify(characterService).getCharacters("WARRIOR", "Test")
    }

    @Test
    fun `getCharacterById should return character by id`() {
        `when`(characterService.getCharacterById(anyLong())).thenReturn(warriorCharacter)

        mockMvc.perform(get("/api/characters/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("TestWarrior"))
            .andExpect(jsonPath("$.characterClass").value("WARRIOR"))

        verify(characterService).getCharacterById(1L)
    }

    @Test
    fun `getChallengers should return owned characters`() {
        val challengers = listOf(warriorCharacter)
        `when`(characterService.getChallengers(anyLong())).thenReturn(challengers)

        mockMvc.perform(get("/api/characters/challengers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))

        verify(characterService).getChallengers(accountId)
    }

    @Test
    fun `getOpponents should return non-owned characters`() {
        val opponents = listOf(sorcererCharacter)
        `when`(characterService.getOpponents(anyLong())).thenReturn(opponents)

        mockMvc.perform(get("/api/characters/opponents"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(2))

        verify(characterService).getOpponents(accountId)
    }

    @Test
    fun `postCharacter should create new character`() {
        val request = CharacterCreateRequest(
            name = "NewWarrior",
            characterClass = CharacterClass.WARRIOR,
            health = 100,
            attackPower = 50,
            defensePower = 30,
            stamina = 40,
            mana = null,
            healingPower = null
        )

        `when`(characterService.createCharacter(any(), anyLong())).thenReturn(warriorCharacter)

        mockMvc.perform(post("/api/characters")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("TestWarrior"))

        verify(characterService).createCharacter(any(), eq(accountId))
    }

    @Test
    fun `putCharacter should update character`() {
        val request = CharacterLevelUpRequest(
            name = "UpdatedWarrior",
            health = 120,
            attackPower = 60,
            defensePower = 40,
            stamina = 50,
            mana = null,
            healingPower = null
        )

        val updatedWarrior = Warrior(
            id = 1L,
            accountId = accountId,
            experience = 0,
            name = "UpdatedWarrior",
            health = 120,
            attackPower = 60,
            defensePower = 40,
            level = CharacterLevel.LEVEL_1,
            stamina = 50
        )

        `when`(characterService.upLevelCharacterById(anyLong(), any())).thenReturn(updatedWarrior)

        mockMvc.perform(put("/api/characters/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("UpdatedWarrior"))
            .andExpect(jsonPath("$.health").value(120))

        verify(characterService).upLevelCharacterById(eq(1L), any())
    }

    @Test
    fun `putCharacter should handle errors`() {
        val request = CharacterLevelUpRequest(
            name = "UpdatedWarrior",
            health = 120,
            attackPower = 60,
            defensePower = 40,
            stamina = 50,
            mana = null,
            healingPower = null
        )

        `when`(characterService.upLevelCharacterById(anyLong(), any()))
            .thenThrow(RuntimeException("Failed to update character"))

        mockMvc.perform(put("/api/characters/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError)
    }
}