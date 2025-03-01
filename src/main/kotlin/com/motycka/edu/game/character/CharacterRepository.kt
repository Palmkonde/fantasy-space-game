package com.motycka.edu.game.character

import com.fasterxml.jackson.annotation.Nulls
import com.motycka.edu.game.account.model.AccountId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior
import com.motycka.edu.game.character.rest.CharacterCreateRequest
import com.motycka.edu.game.character.rest.CharacterLevelUpRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.math.log

private val logger = KotlinLogging.logger {  }

@Repository
class CharacterRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun selectByFilters(className: String?, name: String?): List<Character> {
        logger.debug { "Selecting characters by class: $className, name: $name" }

        // If no filters are provided, use a simpler query
        if (className == null && name == null) {
            return jdbcTemplate.query(
                "SELECT * FROM character",
                ::rowMapper
            )
        }

        val sql = StringBuilder("SELECT * FROM character")
        val params = mutableListOf<Any>()

        if (className != null || name != null) {
            sql.append(" WHERE ")
            val conditions = mutableListOf<String>()

            if (className != null) {
                conditions.add("class = ?")
                params.add(className)
            }
            if (name != null) {
                conditions.add("name = ?")
                params.add(name)
            }

            sql.append(conditions.joinToString(" AND "))
        }

        return jdbcTemplate.query(sql.toString(),
        ::rowMapper,
        *params.toTypedArray())
    }

    fun selectById(id: Long): Character? {
        logger.debug { "Selecting character by id $id" }
        return jdbcTemplate.query(
            "SELECT * FROM character WHERE id = ?;",
            ::rowMapper,
            id
        ).firstOrNull()
    }

    fun getOwnedCharacters(accountId: AccountId): List<Character>? {
        logger.debug { "Getting own Characters of $accountId" }

        val sql = """
            SELECT * FROM character
            WHERE account_id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, ::rowMapper, accountId)
    }

    fun getNotOwnedCharacters(accountId: AccountId): List<Character>? {
        logger.debug { "Selecting characters not owned by account: $accountId" }

        val sql = """
        SELECT * FROM character 
        WHERE account_id != ?
        """.trimIndent()

        return jdbcTemplate.query(sql, ::rowMapper, accountId)
    }

    fun insertCharacter(character: CharacterCreateRequest, accountId: AccountId): Character? {
        logger.debug { "Inserting new character: ${character.name}" }

        val className = when(character.characterClass) {
            CharacterClass.WARRIOR -> CharacterClass.WARRIOR.toString()
            CharacterClass.SORCERER -> CharacterClass.SORCERER.toString()
            else -> error("Invalid Character class")
        }

        val (defense, stamina) = when(className) {
            CharacterClass.WARRIOR.toString() -> character.defensePower to character.stamina
            else -> null to null
        }

        val (healing, mana) = when(className) {
            CharacterClass.SORCERER.toString() -> character.healingPower to character.mana
            else -> null to null
        }

        return jdbcTemplate.query(
            """
            SELECT * FROM FINAL TABLE (
                INSERT INTO character (
                    account_id,
                    name,
                    class,
                    health,
                    attack,
                    experience,
                    defense,
                    stamina,
                    healing,
                    mana
                ) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            );
        """.trimIndent(),
            ::rowMapper,
            accountId,
            character.name,
            className,
            character.health,
            character.attackPower,
            0,
            defense,
            stamina,
            healing,
            mana
        ).firstOrNull()
    }

    fun upLevelCharacter(id: Long, updateCharacter: CharacterLevelUpRequest): Character {
            val sqlWarrior = """
                UPDATE character
                SET
                    name = ?,
                    health = ?,
                    attack = ?,
                    stamina = ?,
                    defense = ?
                WHERE id = ?
            """.trimIndent()

            val sqlSorcerer = """
                UPDATE character
                SET
                    name = ?,
                    health = ?,
                    attack = ?,
                    mana = ?,
                    healing = ?
                WHERE id = ?
            """.trimIndent()

            val character = selectById(id) ?: throw SQLException("Character not found")
            return try {
                if (updateCharacter.mana == null || updateCharacter.healingPower == null) {
                    jdbcTemplate.update(
                        sqlWarrior,
                        updateCharacter.name,
                        updateCharacter.health,
                        updateCharacter.attackPower,
                        updateCharacter.stamina,
                        updateCharacter.defensePower,
                        id
                    )
                    updateExperience(id, character.experience - character.level.requireExp)
                } else {
                    jdbcTemplate.update(
                        sqlSorcerer,
                        updateCharacter.name,
                        updateCharacter.health,
                        updateCharacter.attackPower,
                        updateCharacter.mana,
                        updateCharacter.healingPower,
                        id
                    )
                    updateExperience(id, character.experience - character.level.requireExp)
                }
                selectById(id) ?: throw SQLException("Character not found after update")
            } catch (ex: SQLException) {
                logger.error(ex) { "Error updating character with id $id" }
                throw ex
            }
    }

    fun updateExperience(id: Long, experience: Int): Int {
        val sql = """
            UPDATE character
            SET experience = ?
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.update(sql, experience, id)
    }

    @Throws(SQLException::class)
    private fun rowMapper(rs: ResultSet, i: Int): Character {
        return when(rs.getString("class")) {
            CharacterClass.WARRIOR.toString() -> {
                val health = rs.getInt("health")
                val attack = rs.getInt("attack")
                val defense = rs.getInt("defense")
                val stamina = rs.getInt("stamina")

                // Use the existing upLevel method with the list of points
                val level = CharacterLevel.LEVEL_1.upLevel(
                    character = null,
                    otherPoints = listOf(health, attack, defense, stamina)
                )

                Warrior(
                    id = rs.getLong("id"),
                    accountId = rs.getLong("account_id"),
                    name = rs.getString("name"),
                    health = health,
                    attackPower = attack,
                    level = level,
                    experience = rs.getInt("experience"),
                    defensePower = defense,
                    stamina = stamina
                )
            }

            CharacterClass.SORCERER.toString() -> {
                val health = rs.getInt("health")
                val attack = rs.getInt("attack")
                val mana = rs.getInt("mana")
                val healing = rs.getInt("healing")

                // Use the existing upLevel method with the list of points
                val level = CharacterLevel.LEVEL_1.upLevel(
                    character = null,
                    otherPoints = listOf(health, attack, mana, healing)
                )

                Sorcerer(
                    id = rs.getLong("id"),
                    accountId = rs.getLong("account_id"),
                    name = rs.getString("name"),
                    health = health,
                    attackPower = attack,
                    level = level,
                    experience = rs.getInt("experience"),
                    mana = mana,
                    healingPower = healing
                )
            }
            else -> error("Doesn't match any Class")
        }
    }
}