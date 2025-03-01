package com.motycka.edu.game.match.rest

data class MatchCreateRequest(
    val rounds: Int,
    val challengerId: Long,
    val opponentId: Long
)