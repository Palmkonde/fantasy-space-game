package com.motycka.edu.game.character

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class CharacterRepository(
    private val jdbcTemplate: JdbcTemplate
) {

}