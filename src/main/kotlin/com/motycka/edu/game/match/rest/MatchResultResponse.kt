package com.motycka.edu.game.match.rest

import com.motycka.edu.game.match.model.Fighter
import com.motycka.edu.game.match.model.RoundData

data class MatchResultResponse(
    val id: Long,
    val challenger: Fighter,
    val opponent: Fighter,
    val rounds: RoundData
)
