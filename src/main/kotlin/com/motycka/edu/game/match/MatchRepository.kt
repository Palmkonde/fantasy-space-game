package com.motycka.edu.game.match

import com.motycka.edu.game.match.rest.MatchResultResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class MatchRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    private fun rowMapper(rs: ResultSet, i): MatchResultResponse {
       return MatchResultResponse(
           id:
       )
    }
}