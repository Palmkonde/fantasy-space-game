package com.motycka.edu.game.match

import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.match.model.Fighter
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.model.RoundData
import com.motycka.edu.game.match.rest.MatchResultResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Statement

@Repository
class MatchRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun saveMatch(
        challengerId: Long,
        opponentId: Long,
        challengerResult: Fighter,
        opponentResult: Fighter,
        rounds: List<RoundData>,
        matchOutcome: MatchOutcome
    ): MatchResultResponse {
        // Insert match record
        val matchSql = """
            INSERT INTO match (
                challenger_id,
                opponent_id,
                match_outcome,
                challenger_xp,
                opponent_xp
            ) VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(matchSql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, challengerId)
            ps.setLong(2, opponentId)
            ps.setString(3, matchOutcome.toString())
            ps.setInt(4, challengerResult.experienceGained)
            ps.setInt(5, opponentResult.experienceGained)
            ps
        }, keyHolder)

        val matchId = keyHolder.key?.toLong()
            ?: throw IllegalStateException("Failed to retrieve generated match ID")

        // Insert round records
        val roundSql = """
            INSERT INTO round (
                match_id,
                round_number,
                character_id,
                health_delta,
                stamina_delta,
                mana_delta
            ) VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        rounds.forEach { round ->
            jdbcTemplate.update(
                roundSql,
                matchId,
                round.round,
                round.characterId,
                round.healthDelta,
                round.staminaDelta,
                round.manaDelta
            )
        }

        return getMatchById(matchId) ?: error("Error this match: $matchId doesn't exist")
    }


    fun getMatches(): List<MatchResultResponse> {
        val sql = """
        $matchSelectSql
        ORDER BY m.id DESC
    """.trimIndent()

        return jdbcTemplate.query(sql) { rs, _ -> mapMatchResult(rs) }
    }

    fun getMatchById(matchId: Long): MatchResultResponse? {
        val sql = """
        $matchSelectSql
        WHERE m.id = ?
    """.trimIndent()

        return jdbcTemplate.query(sql, { rs, _ -> mapMatchResult(rs) }, matchId)
            .firstOrNull()
    }

    private fun intToCharacterLevel(level: Int): CharacterLevel {
        return when (level) {
            1 -> CharacterLevel.LEVEL_1
            2 -> CharacterLevel.LEVEL_2
            3 -> CharacterLevel.LEVEL_3
            4 -> CharacterLevel.LEVEL_4
            5 -> CharacterLevel.LEVEL_5
            6 -> CharacterLevel.LEVEL_6
            7 -> CharacterLevel.LEVEL_7
            8 -> CharacterLevel.LEVEL_8
            9 -> CharacterLevel.LEVEL_9
            10 -> CharacterLevel.LEVEL_10
            else -> CharacterLevel.LEVEL_1 // Default to level 1 if unknown
        }
    }

    private val matchSelectSql = """
        SELECT 
            m.id,
            m.challenger_id,
            m.opponent_id,
            m.match_outcome,
            m.challenger_xp,
            m.opponent_xp,
            c.name as challenger_name,
            c.class as challenger_class,
            c.experience as challenger_exp,
            o.name as opponent_name,
            o.class as opponent_class,
            o.experience as opponent_exp
        FROM match m
        JOIN character c ON m.challenger_id = c.id
        JOIN character o ON m.opponent_id = o.id
    """.trimIndent()

    private fun mapFighter(
        rs: ResultSet,
        prefix: String,
    ) = Fighter(
        id = rs.getLong("${prefix}_id"),
        name = rs.getString("${prefix}_name"),
        characterClass = CharacterClass.valueOf(rs.getString("${prefix}_class")),
        level = CharacterLevel.LEVEL_1.upLevel(rs.getInt("${prefix}_exp")),
        experienceTotal = rs.getInt("${prefix}_exp"),
        experienceGained = rs.getInt("${prefix}_xp"),
    )

    private fun getRoundsByMatchId(matchId: Long): List<RoundData> {
        val roundsSql = """
            SELECT 
                round_number,
                character_id,
                health_delta,
                stamina_delta,
                mana_delta
            FROM round
            WHERE match_id = ?
            ORDER BY round_number
        """.trimIndent()

        return jdbcTemplate.query(roundsSql, { rs, _ ->
            RoundData(
                round = rs.getInt("round_number"),
                characterId = rs.getLong("character_id"),
                healthDelta = rs.getInt("health_delta"),
                staminaDelta = rs.getInt("stamina_delta"),
                manaDelta = rs.getInt("mana_delta")
            )
        }, matchId)
    }

    private fun mapMatchResult(rs: ResultSet): MatchResultResponse {
        val matchId = rs.getLong("id")
        val matchOutcome = rs.getString("match_outcome")

        return MatchResultResponse(
            id = matchId,
            challenger = mapFighter(rs, "challenger"),
            opponent = mapFighter(rs, "opponent"),
            rounds = getRoundsByMatchId(matchId),
            matchOutcome = MatchOutcome.valueOf(matchOutcome)
        )
    }
}