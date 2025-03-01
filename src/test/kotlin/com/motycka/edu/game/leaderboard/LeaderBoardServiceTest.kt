package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.rest.CharacterResponse
import com.motycka.edu.game.leaderboard.rest.LeaderBoardResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull

@ExtendWith(MockitoExtension::class)
class LeaderBoardServiceTest {

    @Mock
    private lateinit var leaderBoardRepository: LeaderBoardRepository

    @Mock
    private lateinit var accountService: AccountService

    private lateinit var leaderBoardService: LeaderBoardService

    private val accountId = 1L
    private val warriorCharacterId = 1L
    private val sorcererCharacterId = 2L

    private val warriorCharacter = CharacterResponse(
        id = warriorCharacterId,
        name = "TestWarrior",
        characterClass = CharacterClass.WARRIOR,
        level = CharacterLevel.LEVEL_1,
        experience = 0,
        health = 100,
        attackPower = 50,
        defensePower = 30,
        stamina = 40,
        mana = null,
        healingPower = null,
        isOwner = true,
        shouldLevelUp = false
    )

    private val sorcererCharacter = CharacterResponse(
        id = sorcererCharacterId,
        name = "TestSorcerer",
        characterClass = CharacterClass.SORCERER,
        level = CharacterLevel.LEVEL_1,
        experience = 0,
        health = 80,
        attackPower = 40,
        defensePower = null,
        stamina = null,
        mana = 60,
        healingPower = 50,
        isOwner = false,
        shouldLevelUp = false
    )

    private val warriorEntry = LeaderBoardResponse(
        position = 1,
        character = warriorCharacter,
        wins = 10,
        losses = 2,
        draws = 1
    )

    private val sorcererEntry = LeaderBoardResponse(
        position = 2,
        character = sorcererCharacter,
        wins = 8,
        losses = 3,
        draws = 2
    )

    @BeforeEach
    fun setUp() {
        leaderBoardService = LeaderBoardService(leaderBoardRepository, accountService)
        `when`(accountService.getCurrentAccountId()).thenReturn(accountId)
    }

    @Test
    fun `getLeaderBoard should return all entries when no class filter`() {
        val entries = listOf(warriorEntry, sorcererEntry)
        `when`(leaderBoardRepository.getLeaderBoard(isNull(), eq(accountId))).thenReturn(entries)

        val result = leaderBoardService.getLeaderBoard(null)

        assert(result.size == 2)
        assert(result[0].position == 1)
        assert(result[0].character.characterClass == CharacterClass.WARRIOR)
        assert(result[0].character.isOwner)
        assert(result[1].position == 2)
        assert(result[1].character.characterClass == CharacterClass.SORCERER)
        assert(!result[1].character.isOwner)
        verify(leaderBoardRepository).getLeaderBoard(isNull(), eq(accountId))
        verify(accountService).getCurrentAccountId()
    }

    @Test
    fun `getLeaderBoard should return filtered warrior entries`() {
        val entries = listOf(warriorEntry)
        `when`(leaderBoardRepository.getLeaderBoard(eq("WARRIOR"), eq(accountId))).thenReturn(entries)

        val result = leaderBoardService.getLeaderBoard("WARRIOR")

        assert(result.size == 1)
        assert(result[0].position == 1)
        assert(result[0].character.characterClass == CharacterClass.WARRIOR)
        assert(result[0].character.isOwner)
        verify(leaderBoardRepository).getLeaderBoard(eq("WARRIOR"), eq(accountId))
        verify(accountService).getCurrentAccountId()
    }

    @Test
    fun `getLeaderBoard should return filtered sorcerer entries`() {
        val entries = listOf(sorcererEntry)
        `when`(leaderBoardRepository.getLeaderBoard(eq("SORCERER"), eq(accountId))).thenReturn(entries)

        val result = leaderBoardService.getLeaderBoard("SORCERER")

        assert(result.size == 1)
        assert(result[0].position == 2)
        assert(result[0].character.characterClass == CharacterClass.SORCERER)
        assert(!result[0].character.isOwner)
        verify(leaderBoardRepository).getLeaderBoard(eq("SORCERER"), eq(accountId))
        verify(accountService).getCurrentAccountId()
    }

    @Test
    fun `getLeaderBoard should handle empty results`() {
        `when`(leaderBoardRepository.getLeaderBoard(isNull(), eq(accountId))).thenReturn(emptyList())

        val result = leaderBoardService.getLeaderBoard(null)

        assert(result.isEmpty())
        verify(leaderBoardRepository).getLeaderBoard(isNull(), eq(accountId))
        verify(accountService).getCurrentAccountId()
    }

    @Test
    fun `getLeaderBoard should handle invalid class filter`() {
        assertThrows<IllegalArgumentException> {
            leaderBoardService.getLeaderBoard("INVALID_CLASS")
        }
    }

    @Test
    fun `updateLeaderBoardFromMatch should update leaderboard successfully`() {
        val matchId = 1L
        doNothing().`when`(leaderBoardRepository).updateLeaderBoardFromMatch(eq(matchId))

        leaderBoardService.updateLeaderBoardFromMatch(matchId)

        verify(leaderBoardRepository).updateLeaderBoardFromMatch(eq(matchId))
    }

    @Test
    fun `updateLeaderBoardFromMatch should handle match not found`() {
        val matchId = 999L
        doThrow(NoSuchElementException("Match not found"))
            .`when`(leaderBoardRepository).updateLeaderBoardFromMatch(eq(matchId))

        assertThrows<NoSuchElementException> {
            leaderBoardService.updateLeaderBoardFromMatch(matchId)
        }

        verify(leaderBoardRepository).updateLeaderBoardFromMatch(eq(matchId))
    }
}