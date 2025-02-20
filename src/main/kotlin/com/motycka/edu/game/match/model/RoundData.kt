package com.motycka.edu.game.match.model

data class RoundData(
    val round: Int,
    val characterId: Long,
    val healthDelta: Int,
    val staminaDelta: Int,
    val manaDelta: Int
)