package com.motycka.edu.game.character

import com.motycka.edu.game.account.model.AccountId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior
import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.ResultSet
import java.sql.SQLException

private val logger = KotlinLogging.logger {  }

@Repository
class CharacterRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun selectByFilters(className: String?, name: String?): List<Pair<AccountId, Character>> {
        logger.debug { "Selecting characters by class: $className, name: $name" }

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

        return jdbcTemplate.query(sql.toString(), { rs, rowNum ->
            val id = rs.getLong("id")
            val character = rowMapper(rs, rowNum)
            id to character
        }, *params.toTypedArray())
    }

    fun selectById(id: Long): Pair<AccountId, Character>? {
        logger.debug { "Selecting character by id $id" }
        return jdbcTemplate.query(
            "SELECT * FROM character WHERE id = ?;",
            { rs, rowNum ->
                val character = rowMapper(rs, rowNum)
                id to character
            },
            id
        ).firstOrNull()
    }

    @Throws(SQLException::class)
    private fun rowMapper(rs: ResultSet, i: Int): Character {
        return when(rs.getString("class")) {
            CharacterClass.WARRIOR.toString() -> Warrior(
                name = rs.getString("name"),
                health = rs.getInt("health"),
                attackPower = rs.getInt("attack"),
                level = CharacterLevel.LEVEL_1,
                experience = rs.getInt("experience"),
                defensePower = rs.getInt("defense"),
                stamina = rs.getInt("stamina")
            )

            CharacterClass.SORCERER.toString() -> Sorcerer(
                name = rs.getString("name"),
                health = rs.getInt("health"),
                attackPower = rs.getInt("attack"),
                level = CharacterLevel.LEVEL_1,
                experience = rs.getInt("experience"),
                mana = rs.getInt("mana"),
                healingPower = rs.getInt("healing")
            )
            else -> error("Doesn't match any Class")
        }
    }
}