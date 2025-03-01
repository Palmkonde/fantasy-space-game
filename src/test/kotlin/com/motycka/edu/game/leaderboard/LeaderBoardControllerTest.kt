package com.motycka.edu.game.leaderboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.rest.CharacterResponse
import com.motycka.edu.game.leaderboard.rest.LeaderBoardResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(LeaderBoardController::class)
class LeaderBoardControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var leaderBoardService: LeaderBoardService

    @MockBean
    private lateinit var accountService: AccountService

    private val accountId = 1L

    private val topWarrior = CharacterResponse(
        id = 1L,
        name = "TopWarrior",
        characterClass = CharacterClass.WARRIOR,
        level = CharacterLevel.LEVEL_3,
        experience = 900,
        health = 120,
        attackPower = 60,
        defensePower = 40,
        stamina = 50,
        mana = null,
        healingPower = null,
        isOwner = true,
        shouldLevelUp = false
    )

    private val midWarrior = CharacterResponse(
        id = 2L,
        name = "MidWarrior",
        characterClass = CharacterClass.WARRIOR,
        level = CharacterLevel.LEVEL_2,
        experience = 500,
        health = 100,
        attackPower = 50,
        defensePower = 30,
        stamina = 40,
        mana = null,
        healingPower = null,
        isOwner = false,
        shouldLevelUp = false
    )

    private val topSorcerer = CharacterResponse(
        id = 3L,
        name = "TopSorcerer",
        characterClass = CharacterClass.SORCERER,
        level = CharacterLevel.LEVEL_3,
        experience = 850,
        health = 90,
        attackPower = 45,
        defensePower = null,
        stamina = null,
        mana = 70,
        healingPower = 55,
        isOwner = false,
        shouldLevelUp = false
    )

    private val midSorcerer = CharacterResponse(
        id = 4L,
        name = "MidSorcerer",
        characterClass = CharacterClass.SORCERER,
        level = CharacterLevel.LEVEL_2,
        experience = 450,
        health = 80,
        attackPower = 40,
        defensePower = null,
        stamina = null,
        mana = 60,
        healingPower = 45,
        isOwner = true,
        shouldLevelUp = false
    )

    private val topWarriorEntry = LeaderBoardResponse(
        position = 1,
        character = topWarrior,
        wins = 15,
        losses = 2,
        draws = 1
    )

    private val midWarriorEntry = LeaderBoardResponse(
        position = 3,
        character = midWarrior,
        wins = 8,
        losses = 5,
        draws = 2
    )

    private val topSorcererEntry = LeaderBoardResponse(
        position = 2,
        character = topSorcerer,
        wins = 12,
        losses = 3,
        draws = 1
    )

    private val midSorcererEntry = LeaderBoardResponse(
        position = 4,
        character = midSorcerer,
        wins = 6,
        losses = 6,
        draws = 3
    )

    @BeforeEach
    fun setUp() {
        `when`(accountService.getCurrentAccountId()).thenReturn(accountId)
    }

    @Test
    fun `getLeaderboard should return all characters sorted by position when no class filter`() {
        val leaderboard = listOf(topWarriorEntry, topSorcererEntry, midWarriorEntry, midSorcererEntry)
        `when`(leaderBoardService.getLeaderBoard(null)).thenReturn(leaderboard)

        mockMvc.perform(get("/api/leaderboards"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(4))
            // First place warrior
            .andExpect(jsonPath("$[0].position").value(1))
            .andExpect(jsonPath("$[0].character.name").value("TopWarrior"))
            .andExpect(jsonPath("$[0].character.characterClass").value("WARRIOR"))
            .andExpect(jsonPath("$[0].wins").value(15))
            .andExpect(jsonPath("$[0].losses").value(2))
            .andExpect(jsonPath("$[0].draws").value(1))
            // Second place sorcerer
            .andExpect(jsonPath("$[1].position").value(2))
            .andExpect(jsonPath("$[1].character.name").value("TopSorcerer"))
            .andExpect(jsonPath("$[1].character.characterClass").value("SORCERER"))
            .andExpect(jsonPath("$[1].wins").value(12))
            // Third place warrior
            .andExpect(jsonPath("$[2].position").value(3))
            .andExpect(jsonPath("$[2].character.name").value("MidWarrior"))
            // Fourth place sorcerer
            .andExpect(jsonPath("$[3].position").value(4))
            .andExpect(jsonPath("$[3].character.name").value("MidSorcerer"))

        verify(leaderBoardService).getLeaderBoard(null)
    }

    @Test
    fun `getLeaderboard should return filtered warriors sorted by position`() {
        val leaderboard = listOf(topWarriorEntry, midWarriorEntry)
        `when`(leaderBoardService.getLeaderBoard("WARRIOR")).thenReturn(leaderboard)

        mockMvc.perform(get("/api/leaderboards")
            .param("class", "WARRIOR"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            // First place warrior
            .andExpect(jsonPath("$[0].position").value(1))
            .andExpect(jsonPath("$[0].character.name").value("TopWarrior"))
            .andExpect(jsonPath("$[0].character.characterClass").value("WARRIOR"))
            .andExpect(jsonPath("$[0].character.stamina").value(50))
            .andExpect(jsonPath("$[0].character.defensePower").value(40))
            .andExpect(jsonPath("$[0].character.mana").isEmpty)
            .andExpect(jsonPath("$[0].wins").value(15))
            // Second place warrior
            .andExpect(jsonPath("$[1].position").value(3))
            .andExpect(jsonPath("$[1].character.name").value("MidWarrior"))
            .andExpect(jsonPath("$[1].character.characterClass").value("WARRIOR"))

        verify(leaderBoardService).getLeaderBoard("WARRIOR")
    }

    @Test
    fun `getLeaderboard should return filtered sorcerers sorted by position`() {
        val leaderboard = listOf(topSorcererEntry, midSorcererEntry)
        `when`(leaderBoardService.getLeaderBoard("SORCERER")).thenReturn(leaderboard)

        mockMvc.perform(get("/api/leaderboards")
            .param("class", "SORCERER"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            // First place sorcerer
            .andExpect(jsonPath("$[0].position").value(2))
            .andExpect(jsonPath("$[0].character.name").value("TopSorcerer"))
            .andExpect(jsonPath("$[0].character.characterClass").value("SORCERER"))
            .andExpect(jsonPath("$[0].character.mana").value(70))
            .andExpect(jsonPath("$[0].character.healingPower").value(55))
            .andExpect(jsonPath("$[0].character.stamina").isEmpty)
            .andExpect(jsonPath("$[0].wins").value(12))
            // Second place sorcerer
            .andExpect(jsonPath("$[1].position").value(4))
            .andExpect(jsonPath("$[1].character.name").value("MidSorcerer"))
            .andExpect(jsonPath("$[1].character.characterClass").value("SORCERER"))

        verify(leaderBoardService).getLeaderBoard("SORCERER")
    }

    @Test
    fun `getLeaderboard should handle empty results`() {
        `when`(leaderBoardService.getLeaderBoard(any())).thenReturn(emptyList())

        mockMvc.perform(get("/api/leaderboards"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))

        verify(leaderBoardService).getLeaderBoard(null)
    }

    @Test
    fun `getLeaderboard should handle invalid class filter`() {
        `when`(leaderBoardService.getLeaderBoard("INVALID_CLASS"))
            .thenThrow(IllegalArgumentException("Invalid character class"))

        mockMvc.perform(get("/api/leaderboards")
            .param("class", "INVALID_CLASS"))
            .andExpect(status().isBadRequest)

        verify(leaderBoardService).getLeaderBoard("INVALID_CLASS")
    }
}