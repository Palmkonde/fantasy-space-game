package com.motycka.edu.game.character

import com.motycka.edu.game.account.AccountFixtures
import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior
import com.motycka.edu.game.character.rest.CharacterCreateRequest
import com.motycka.edu.game.character.rest.CharacterLevelUpRequest
import com.motycka.edu.game.config.SecurityContextHolderHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CharacterServiceTest {

    private val characterRepository: CharacterRepository = mockk()
    private val accountService: AccountService = mockk()
    private val characterService: CharacterService = CharacterService(
        characterRepository = characterRepository
    )

    // Test fixtures
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

    private val allCharacters = listOf(warriorCharacter, sorcererCharacter)

    @BeforeEach
    fun setUp() {
        SecurityContextHolderHelper.setSecurityContext(AccountFixtures.DEVELOPER)
        every { accountService.getCurrentAccountId() } returns accountId
    }

    @Test
    fun `getCharacters should return filtered characters`() {
        val className = "WARRIOR"
        val name = "Test"

        every { characterRepository.selectByFilters(className, name) } returns listOf(warriorCharacter)

        val result = characterService.getCharacters(className, name)

        assertEquals(1, result.size)
        assertEquals("TestWarrior", result[0].name)
        verify { characterRepository.selectByFilters(className, name) }
    }

    @Test
    fun `getCharacterById should return character when found`() {
        val id = 1L

        every { characterRepository.selectById(id) } returns warriorCharacter

        val result = characterService.getCharacterById(id)

        assertEquals(id, result.id)
        assertEquals("TestWarrior", result.name)
        verify { characterRepository.selectById(id) }
    }

    @Test
    fun `getCharacterById should throw error when character not found`() {
        val id = 999L

        every { characterRepository.selectById(id) } returns null

        val exception = assertThrows<IllegalStateException> {
            characterService.getCharacterById(id)
        }
        
        assertEquals("No such a character with ID: $id", exception.message)
        verify { characterRepository.selectById(id) }
    }

    @Test
    fun `createCharacter should create and return new character`() {
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

        every { characterRepository.insertCharacter(request, accountId) } returns warriorCharacter

        val result = characterService.createCharacter(request, accountId)

        assertEquals("TestWarrior", result.name)
        verify { characterRepository.insertCharacter(request, accountId) }
    }

    @Test
    fun `createCharacter should throw error when insert fails`() {
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

        every { characterRepository.insertCharacter(request, accountId) } returns null

        val exception = assertThrows<IllegalStateException> {
            characterService.createCharacter(request, accountId)
        }
        
        assertEquals("Error Can't create Charactor", exception.message)
        verify { characterRepository.insertCharacter(request, accountId) }
    }

    @Test
    fun `getChallengers should return characters owned by current account`() {
        val ownedCharacters = listOf(warriorCharacter)

        every { characterRepository.getOwnedCharacters(accountId) } returns ownedCharacters

        val result = characterService.getChallengers(accountId)

        assertEquals(1, result.size)
        assertEquals(accountId, result[0].accountId)
        verify { characterRepository.getOwnedCharacters(accountId) }
    }

    @Test
    fun `getChallengers should throw error when repository returns null`() {
        every { characterRepository.getOwnedCharacters(accountId) } returns null

        val exception = assertThrows<IllegalStateException> {
            characterService.getChallengers(accountId)
        }
        
        assertEquals("Can't require Challengers", exception.message)
        verify { characterRepository.getOwnedCharacters(accountId) }
    }

    @Test
    fun `getOpponents should return characters not owned by current account`() {
        val opponents = listOf(sorcererCharacter)

        every { characterRepository.getNotOwnedCharacters(accountId) } returns opponents

        val result = characterService.getOpponents(accountId)

        assertEquals(1, result.size)
        assertEquals(2L, result[0].accountId)
        verify { characterRepository.getNotOwnedCharacters(accountId) }
    }

    @Test
    fun `getOpponents should throw error when repository returns null`() {
        every { characterRepository.getNotOwnedCharacters(accountId) } returns null

        val exception = assertThrows<IllegalStateException> {
            characterService.getOpponents(accountId)
        }
        
        assertEquals("Can't require Opponents", exception.message)
        verify { characterRepository.getNotOwnedCharacters(accountId) }
    }

    @Test
    fun `upLevelCharacterById should update and return character`() {
        val id = 1L
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
            name = "UpdatedWarrior",
            health = 120,
            attackPower = 60,
            level = CharacterLevel.LEVEL_1,
            experience = 0,
            defensePower = 40,
            stamina = 50
        )

        every { characterRepository.upLevelCharacter(id, request) } returns updatedWarrior

        val result = characterService.upLevelCharacterById(id, request)

        assertEquals("UpdatedWarrior", result.name)
        assertEquals(120, result.health)
        verify { characterRepository.upLevelCharacter(id, request) }
    }

    @Test
    fun `upLevelCharacterById should throw error when update fails`() {
        val id = 1L
        val request = CharacterLevelUpRequest(
            name = "UpdatedWarrior",
            health = 120,
            attackPower = 60,
            defensePower = 40,
            stamina = 50,
            mana = null,
            healingPower = null
        )

        // Mock the repository to throw IllegalStateException when update fails
        every { characterRepository.upLevelCharacter(id, request) } throws IllegalStateException("Can't update Character")

        val exception = assertThrows<IllegalStateException> {
            characterService.upLevelCharacterById(id, request)
        }
        
        assertEquals("Can't update Character", exception.message)
        verify { characterRepository.upLevelCharacter(id, request) }
    }
    
    @Test
    fun `createCharacter should validate warrior point distribution`() {
        val request = CharacterCreateRequest(
            name = "InvalidWarrior",
            characterClass = CharacterClass.WARRIOR,
            health = 200,
            attackPower = 100,
            defensePower = 80,
            stamina = 90,
            mana = null,
            healingPower = null
        )
        
        // Mock the repository to throw IllegalArgumentException when trying to insert with invalid points
        every { characterRepository.insertCharacter(request, accountId) } throws IllegalArgumentException("Invalid point distribution for warrior")
        
        val exception = assertThrows<IllegalArgumentException> {
            characterService.createCharacter(request, accountId)
        }
        
        assertEquals("Invalid point distribution for warrior", exception.message)
    }
    
    @Test
    fun `createCharacter should validate sorcerer point distribution`() {
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
        
        // Mock the repository to throw IllegalArgumentException when trying to insert with invalid points
        every { characterRepository.insertCharacter(request, accountId) } throws IllegalArgumentException("Invalid point distribution for sorcerer")
        
        val exception = assertThrows<IllegalArgumentException> {
            characterService.createCharacter(request, accountId)
        }
        
        assertEquals("Invalid point distribution for sorcerer", exception.message)
    }
    
    @Test
    fun `createCharacter should validate character name uniqueness`() {
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
        
        // Mock the repository to throw IllegalArgumentException when trying to insert with an existing name
        every { characterRepository.insertCharacter(request, accountId) } throws IllegalArgumentException("Character name already exists")
        
        val exception = assertThrows<IllegalArgumentException> {
            characterService.createCharacter(request, accountId)
        }
        
        assertEquals("Character name already exists", exception.message)
    }

    @Test
    fun `upLevelCharacterById should validate warrior level up point distribution`() {
        val id = 1L
        val request = CharacterLevelUpRequest(
            name = "InvalidWarriorUpdate",
            health = 200,
            attackPower = 100,
            defensePower = 80,
            stamina = 90,
            mana = null,
            healingPower = null
        )
        
        // Mock the repository to throw IllegalArgumentException when trying to update with invalid points
        every { characterRepository.upLevelCharacter(id, request) } throws IllegalArgumentException("Invalid point distribution for warrior level up")
        
        val exception = assertThrows<IllegalArgumentException> {
            characterService.upLevelCharacterById(id, request)
        }
        
        assertEquals("Invalid point distribution for warrior level up", exception.message)
    }
    
    @Test
    fun `upLevelCharacterById should validate sorcerer level up point distribution`() {
        val id = 2L
        val request = CharacterLevelUpRequest(
            name = "InvalidSorcererUpdate",
            health = 150,
            attackPower = 80,
            defensePower = null,
            stamina = null,
            mana = 100,
            healingPower = 90
        )
        
        // Mock the repository to throw IllegalArgumentException when trying to update with invalid points
        every { characterRepository.upLevelCharacter(id, request) } throws IllegalArgumentException("Invalid point distribution for sorcerer level up")
        
        val exception = assertThrows<IllegalArgumentException> {
            characterService.upLevelCharacterById(id, request)
        }
        
        assertEquals("Invalid point distribution for sorcerer level up", exception.message)
    }
}
