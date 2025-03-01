package com.motycka.edu.game.character

import com.motycka.edu.game.account.AccountFixtures
import com.motycka.edu.game.character.interfaces.Character
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CharacterRepositoryTest {

    private val jdbcTemplate: JdbcTemplate = mockk(relaxed = true)
    private lateinit var characterRepository: CharacterRepository

    private val accountId = 1L
    private val characterId = 1L
    
    private val warriorCharacter = Warrior(
        id = characterId,
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
        SecurityContextHolderHelper.setSecurityContext(AccountFixtures.DEVELOPER)
        characterRepository = CharacterRepository(jdbcTemplate)
    }

    @Test
    fun `selectByFilters should query with correct parameters when both class and name provided`() {
        val className = "WARRIOR"
        val name = "TestWarrior"

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                className,
                name
            )
        } returns listOf(warriorCharacter)

        val result = characterRepository.selectByFilters(className, name)

        assertEquals(1, result.size)
        assertEquals("TestWarrior", result[0].name)
        assertEquals(100, result[0].health)
        verify { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                className,
                name
            )
        }
    }

    @Test
    fun `selectByFilters should query with correct parameters when only class provided`() {
        val className = "WARRIOR"

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                className
            )
        } returns listOf(warriorCharacter)

        val result = characterRepository.selectByFilters(className, null)

        assertEquals(1, result.size)
        assertEquals("TestWarrior", result[0].name)
        verify { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                className
            )
        }
    }

    @Test
    fun `selectByFilters should query with correct parameters when only name provided`() {
        val name = "TestWarrior"

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                name
            )
        } returns listOf(warriorCharacter)

        val result = characterRepository.selectByFilters(null, name)

        assertEquals(1, result.size)
        assertEquals("TestWarrior", result[0].name)
        verify { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                name
            )
        }
    }

    @Test
    fun `selectByFilters should query with correct parameters when no filters provided`() {
        // Create a list of characters to return
        val characters = listOf(warriorCharacter, sorcererCharacter)
        
        // Mock the exact query method that will be called with no parameters
        every { 
            jdbcTemplate.query(
                "SELECT * FROM character",
                any<RowMapper<Character>>()
            )
        } returns characters

        val result = characterRepository.selectByFilters(null, null)

        assertEquals(2, result.size)
        verify { 
            jdbcTemplate.query(
                "SELECT * FROM character",
                any<RowMapper<Character>>()
            )
        }
    }

    @Test
    fun `selectById should return character when found`() {
        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                characterId
            )
        } returns listOf(warriorCharacter)

        val result = characterRepository.selectById(characterId)

        assertNotNull(result)
        assertEquals(characterId, result.id)
        assertEquals("TestWarrior", result.name)
        verify { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                characterId
            )
        }
    }

    @Test
    fun `selectById should return null when character not found`() {
        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                characterId
            )
        } returns emptyList()

        val result = characterRepository.selectById(characterId)

        assertNull(result)
        verify { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                characterId
            )
        }
    }

    @Test
    fun `getOwnedCharacters should return characters owned by account`() {
        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                accountId
            )
        } returns listOf(warriorCharacter)

        val result = characterRepository.getOwnedCharacters(accountId)

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(accountId, result[0].accountId)
        verify { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                accountId
            )
        }
    }

    @Test
    fun `getNotOwnedCharacters should return characters not owned by account`() {
        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                accountId
            )
        } returns listOf(sorcererCharacter)

        val result = characterRepository.getNotOwnedCharacters(accountId)

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(2L, result[0].accountId)
        verify { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                accountId
            )
        }
    }

    @Test
    fun `insertCharacter should insert warrior character`() {
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

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns listOf(warriorCharacter)

        val result = characterRepository.insertCharacter(request, accountId)

        assertNotNull(result)
        assertEquals("TestWarrior", result.name)
        verify { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `insertCharacter should insert sorcerer character`() {
        val request = CharacterCreateRequest(
            name = "NewSorcerer",
            characterClass = CharacterClass.SORCERER,
            health = 80,
            attackPower = 40,
            defensePower = null,
            stamina = null,
            mana = 60,
            healingPower = 50
        )

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns listOf(sorcererCharacter)

        val result = characterRepository.insertCharacter(request, accountId)

        assertNotNull(result)
        assertEquals("TestSorcerer", result.name)
        verify { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `insertCharacter should validate warrior point distribution`() {
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

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws IllegalArgumentException("Invalid point distribution for warrior")

        val exception = assertThrows<IllegalArgumentException> {
            characterRepository.insertCharacter(request, accountId)
        }
        
        assertEquals("Invalid point distribution for warrior", exception.message)
    }

    @Test
    fun `insertCharacter should validate sorcerer point distribution`() {
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

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws IllegalArgumentException("Invalid point distribution for sorcerer")

        val exception = assertThrows<IllegalArgumentException> {
            characterRepository.insertCharacter(request, accountId)
        }
        
        assertEquals("Invalid point distribution for sorcerer", exception.message)
    }

    @Test
    fun `insertCharacter should validate character name uniqueness`() {
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

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws IllegalArgumentException("Character name already exists")

        val exception = assertThrows<IllegalArgumentException> {
            characterRepository.insertCharacter(request, accountId)
        }
        
        assertEquals("Character name already exists", exception.message)
    }

    @Test
    fun `upLevelCharacter should update warrior character`() {
        val id = characterId
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
            id = characterId,
            accountId = accountId,
            name = "UpdatedWarrior",
            health = 120,
            attackPower = 60,
            level = CharacterLevel.LEVEL_1,
            experience = 0,
            defensePower = 40,
            stamina = 50
        )

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                id
            )
        } returns listOf(warriorCharacter) andThen listOf(updatedWarrior)

        every { 
            jdbcTemplate.update(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns 1

        every {
            jdbcTemplate.update(
                any(),
                any(),
                any()
            )
        } returns 1

        val result = characterRepository.upLevelCharacter(id, request)

        assertEquals("UpdatedWarrior", result.name)
        assertEquals(120, result.health)
        assertEquals(60, result.attackPower)
        assertTrue(result is Warrior)
        assertEquals(40, (result as Warrior).defensePower)
        assertEquals(50, result.stamina)
    }

    @Test
    fun `upLevelCharacter should update sorcerer character`() {
        val id = 2L
        val request = CharacterLevelUpRequest(
            name = "UpdatedSorcerer",
            health = 100,
            attackPower = 50,
            defensePower = null,
            stamina = null,
            mana = 70,
            healingPower = 60
        )

        val updatedSorcerer = Sorcerer(
            id = 2L,
            accountId = 2L,
            name = "UpdatedSorcerer",
            health = 100,
            attackPower = 50,
            level = CharacterLevel.LEVEL_1,
            experience = 0,
            mana = 70,
            healingPower = 60
        )

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                id
            )
        } returns listOf(sorcererCharacter) andThen listOf(updatedSorcerer)

        every { 
            jdbcTemplate.update(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns 1

        every {
            jdbcTemplate.update(
                any(),
                any(),
                any()
            )
        } returns 1

        val result = characterRepository.upLevelCharacter(id, request)

        assertEquals("UpdatedSorcerer", result.name)
        assertEquals(100, result.health)
        assertEquals(50, result.attackPower)
        assertTrue(result is Sorcerer)
        assertEquals(70, (result as Sorcerer).mana)
        assertEquals(60, result.healingPower)
    }

    @Test
    fun `upLevelCharacter should validate warrior level up point distribution`() {
        val id = characterId
        val request = CharacterLevelUpRequest(
            name = "InvalidWarriorUpdate",
            health = 200,
            attackPower = 100,
            defensePower = 80,
            stamina = 90,
            mana = null,
            healingPower = null
        )

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                id
            )
        } returns listOf(warriorCharacter)

        every { 
            jdbcTemplate.update(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws IllegalArgumentException("Invalid point distribution for warrior level up")

        val exception = assertThrows<IllegalArgumentException> {
            characterRepository.upLevelCharacter(id, request)
        }
        
        assertEquals("Invalid point distribution for warrior level up", exception.message)
    }

    @Test
    fun `upLevelCharacter should validate sorcerer level up point distribution`() {
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

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                id
            )
        } returns listOf(sorcererCharacter)

        every { 
            jdbcTemplate.update(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws IllegalArgumentException("Invalid point distribution for sorcerer level up")

        val exception = assertThrows<IllegalArgumentException> {
            characterRepository.upLevelCharacter(id, request)
        }
        
        assertEquals("Invalid point distribution for sorcerer level up", exception.message)
    }

    @Test
    fun `upLevelCharacter should throw exception when character not found`() {
        val id = 999L
        val request = CharacterLevelUpRequest(
            name = "UpdatedWarrior",
            health = 120,
            attackPower = 60,
            defensePower = 40,
            stamina = 50,
            mana = null,
            healingPower = null
        )

        every { 
            jdbcTemplate.query(
                any(),
                any<RowMapper<Character>>(),
                id
            )
        } returns emptyList()

        assertThrows<SQLException> {
            characterRepository.upLevelCharacter(id, request)
        }
    }

    @Test
    fun `updateExperience should update character experience`() {
        val id = characterId
        val experience = 100

        // Use a more specific mock that matches the exact SQL query and parameters
        every { 
            jdbcTemplate.update(
                "UPDATE character\nSET experience = ?\nWHERE id = ?",
                experience,
                id
            )
        } returns 1

        val result = characterRepository.updateExperience(id, experience)

        assertEquals(1, result)
        verify { 
            jdbcTemplate.update(
                "UPDATE character\nSET experience = ?\nWHERE id = ?",
                experience,
                id
            )
        }
    }
}