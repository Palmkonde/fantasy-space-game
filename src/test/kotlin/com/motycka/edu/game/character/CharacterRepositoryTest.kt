package com.motycka.edu.game.character

import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.rest.CharacterCreateRequest
import com.motycka.edu.game.character.rest.CharacterLevelUpRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class CharacterRepositoryTest {

    @Mock
    private lateinit var jdbcTemplate: JdbcTemplate

    @InjectMocks
    private lateinit var characterRepository: CharacterRepository

    private val accountId = 1L
    private val characterId = 1L
    private val warriorMockData = listOf(
        com.motycka.edu.game.character.model.Warrior(
            id = characterId,
            accountId = accountId,
            name = "TestWarrior",
            health = 100,
            attackPower = 50,
            level = com.motycka.edu.game.character.model.CharacterLevel.LEVEL_1,
            experience = 0,
            defensePower = 30,
            stamina = 40
        )
    )

    private val sorcererMockData = listOf(
        com.motycka.edu.game.character.model.Sorcerer(
            id = 2L,
            accountId = 2L,
            name = "TestSorcerer",
            health = 80,
            attackPower = 40,
            level = com.motycka.edu.game.character.model.CharacterLevel.LEVEL_1,
            experience = 0,
            mana = 60,
            healingPower = 50
        )
    )

    @BeforeEach
    fun setUp() {
        // Default mock behaviors
    }

    @Test
    fun `selectByFilters should query with correct parameters when both class and name provided`() {
        val className = "WARRIOR"
        val name = "TestWarrior"
        val expectedSql = "SELECT * FROM character WHERE class = ? AND name = ?"

        `when`(jdbcTemplate.query(eq(expectedSql), any<RowMapper<Any>>(), eq(className), eq(name)))
            .thenReturn(warriorMockData)

        val result = characterRepository.selectByFilters(className, name)

        assertEquals(1, result.size)
        assertEquals("TestWarrior", result[0].name)
        assertEquals(100, result[0].health)
    }

    @Test
    fun `selectByFilters should query with correct parameters when only class provided`() {
        val className = "WARRIOR"
        val expectedSql = "SELECT * FROM character WHERE class = ?"

        `when`(jdbcTemplate.query(eq(expectedSql), any<RowMapper<Any>>(), eq(className)))
            .thenReturn(warriorMockData)

        val result = characterRepository.selectByFilters(className, null)

        assertEquals(1, result.size)
        assertEquals("TestWarrior", result[0].name)
    }

    @Test
    fun `selectByFilters should query with correct parameters when only name provided`() {
        val name = "TestWarrior"
        val expectedSql = "SELECT * FROM character WHERE name = ?"

        `when`(jdbcTemplate.query(eq(expectedSql), any<RowMapper<Any>>(), eq(name)))
            .thenReturn(warriorMockData)

        val result = characterRepository.selectByFilters(null, name)

        assertEquals(1, result.size)
        assertEquals("TestWarrior", result[0].name)
    }

    @Test
    fun `selectByFilters should query with correct parameters when no filters provided`() {
        val expectedSql = "SELECT * FROM character"

        `when`(jdbcTemplate.query(eq(expectedSql), any<RowMapper<Any>>()))
            .thenReturn(warriorMockData + sorcererMockData)

        val result = characterRepository.selectByFilters(null, null)

        assertEquals(2, result.size)
    }

    @Test
    fun `selectById should return character when found`() {
        val expectedSql = "SELECT * FROM character WHERE id = ?;"

        `when`(jdbcTemplate.query(eq(expectedSql), any<RowMapper<Any>>(), eq(characterId)))
            .thenReturn(warriorMockData)

        val result = characterRepository.selectById(characterId)

        assertNotNull(result)
        assertEquals(characterId, result.id)
        assertEquals("TestWarrior", result.name)
    }

    @Test
    fun `selectById should return null when character not found`() {
        val expectedSql = "SELECT * FROM character WHERE id = ?;"

        `when`(jdbcTemplate.query(eq(expectedSql), any<RowMapper<Any>>(), eq(characterId)))
            .thenReturn(emptyList())

        val result = characterRepository.selectById(characterId)

        assertNull(result)
    }

    @Test
    fun `getOwnedCharacters should return characters owned by account`() {
        val expectedSql = "SELECT * FROM character WHERE account_id = ?"

        `when`(jdbcTemplate.query(eq(expectedSql), any<RowMapper<Any>>(), eq(accountId)))
            .thenReturn(warriorMockData)

        val result = characterRepository.getOwnedCharacters(accountId)

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(accountId, result[0].accountId)
    }

    @Test
    fun `getNotOwnedCharacters should return characters not owned by account`() {
        val expectedSql = "SELECT * FROM character WHERE account_id != ?"

        `when`(jdbcTemplate.query(eq(expectedSql), any<RowMapper<Any>>(), eq(accountId)))
            .thenReturn(sorcererMockData)

        val result = characterRepository.getNotOwnedCharacters(accountId)

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(2L, result[0].accountId)
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

        val expectedSql = "SELECT * FROM FINAL TABLE ( INSERT INTO character ( account_id, name, class, health, attack, experience, defense, stamina, healing, mana ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) );"

        `when`(jdbcTemplate.query(
            anyString(),
            any<RowMapper<Any>>(),
            eq(accountId),
            eq("NewWarrior"),
            eq("WARRIOR"),
            eq(100),
            eq(50),
            eq(0),
            eq(30),
            eq(40),
            eq(null),
            eq(null)
        )).thenReturn(warriorMockData)

        val result = characterRepository.insertCharacter(request, accountId)

        assertNotNull(result)
        assertEquals("TestWarrior", result.name)
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

        val expectedSql = "SELECT * FROM FINAL TABLE ( INSERT INTO character ( account_id, name, class, health, attack, experience, defense, stamina, healing, mana ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) );"

        `when`(jdbcTemplate.query(
            anyString(),
            any<RowMapper<Any>>(),
            eq(accountId),
            eq("NewSorcerer"),
            eq("SORCERER"),
            eq(80),
            eq(40),
            eq(0),
            eq(null),
            eq(null),
            eq(50),
            eq(60)
        )).thenReturn(sorcererMockData)

        val result = characterRepository.insertCharacter(request, accountId)

        assertNotNull(result)
        assertEquals("TestSorcerer", result.name)
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

        val updatedWarriorData = warriorMockData.map {
            (it as com.motycka.edu.game.character.model.Warrior).copy(
                name = "UpdatedWarrior",
                health = 120,
                attackPower = 60,
                defensePower = 40,
                stamina = 50
            )
        }

        `when`(characterRepository.selectById(id)).thenReturn(warriorMockData[0])
        `when`(jdbcTemplate.update(
            anyString(),
            eq("UpdatedWarrior"),
            eq(120),
            eq(60),
            eq(50),
            eq(40),
            eq(id)
        )).thenReturn(1)

        `when`(jdbcTemplate.update(anyString(), eq(0), eq(id))).thenReturn(1)

        `when`(characterRepository.selectById(id)).thenReturn(updatedWarriorData[0])

        val result = characterRepository.upLevelCharacter(id, request)

        assertEquals("UpdatedWarrior", result.name)
        assertEquals(120, result.health)
        assertEquals(60, result.attackPower)
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

        val updatedSorcererData = sorcererMockData.map {
            (it as com.motycka.edu.game.character.model.Sorcerer).copy(
                name = "UpdatedSorcerer",
                health = 100,
                attackPower = 50,
                mana = 70,
                healingPower = 60
            )
        }

        `when`(characterRepository.selectById(id)).thenReturn(sorcererMockData[0])
        `when`(jdbcTemplate.update(
            anyString(),
            eq("UpdatedSorcerer"),
            eq(100),
            eq(50),
            eq(70),
            eq(60),
            eq(id)
        )).thenReturn(1)

        `when`(jdbcTemplate.update(anyString(), eq(0), eq(id))).thenReturn(1)

        `when`(characterRepository.selectById(id)).thenReturn(updatedSorcererData[0])

        val result = characterRepository.upLevelCharacter(id, request)

        assertEquals("UpdatedSorcerer", result.name)
        assertEquals(100, result.health)
        assertEquals(50, result.attackPower)
        assertTrue(result is com.motycka.edu.game.character.model.Sorcerer)
        assertEquals(70, (result as com.motycka.edu.game.character.model.Sorcerer).mana)
        assertEquals(60, result.healingPower)
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

        `when`(characterRepository.selectById(id)).thenReturn(null)

        org.junit.jupiter.api.assertThrows<SQLException> {
            characterRepository.upLevelCharacter(id, request)
        }
    }

    @Test
    fun `updateExperience should update character experience`() {
        val id = characterId
        val experience = 100
        val sql = "UPDATE character SET experience = ? WHERE id = ?"

        `when`(jdbcTemplate.update(eq(sql), eq(experience), eq(id))).thenReturn(1)

        val result = characterRepository.updateExperience(id, experience)

        assertEquals(1, result)
        verify(jdbcTemplate).update(eq(sql), eq(experience), eq(id))
    }
}