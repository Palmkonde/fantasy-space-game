package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.leaderboard.rest.LeaderBoardResponse
import com.motycka.edu.game.match.MatchRepository
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.rest.MatchResultResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class LeaderBoardRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val matchRepository: MatchRepository
) {
    fun updateLeaderBoardFromMatch(matchId: Long) {
        val matchResult = matchRepository.getMatchById(matchId)
            ?: throw IllegalArgumentException("Match with ID $matchId not found")

        val challengerResult = when(matchResult.matchOutcome) {
            MatchOutcome.CHALLENGER_WON -> "win"
            MatchOutcome.OPPONENT_WON -> "loss"
            MatchOutcome.DRAW -> "draw"
        }

        val opponentResult = when(matchResult.matchOutcome) {
            MatchOutcome.CHALLENGER_WON -> "loss"
            MatchOutcome.OPPONENT_WON -> "win"
            MatchOutcome.DRAW -> "draw"
        }

        updateLeaderBoard(matchResult.challenger.id, challengerResult)
        updateLeaderBoard(matchResult.opponent.id, opponentResult)
    }

    private fun updateLeaderBoard(characterId: Long, result: String) {
        val sql = when (result) {
            "win" -> "UPDATE leaderboard SET wins = wins + 1 WHERE character_id = ?"
            "loss" -> "UPDATE leaderboard SET losses = losses + 1 WHERE character_id = ?"
            "draw" -> "UPDATE leaderboard SET draws = draws + 1 WHERE character_id = ?"
            else -> throw IllegalArgumentException("Invalid result: $result")
        }

        val rowsAffected = jdbcTemplate.update(sql, characterId)
        if (rowsAffected == 0) {
            // If no rows were updated, insert a new record
            val insertSql = """
                INSERT INTO leaderboard (character_id, wins, losses, draws)
                VALUES (?, ?, ?, ?)
            """.trimIndent()

            jdbcTemplate.update(insertSql, characterId, if (result == "win") 1 else 0, if (result == "loss") 1 else 0, if (result == "draw") 1 else 0)
        }
    }
}