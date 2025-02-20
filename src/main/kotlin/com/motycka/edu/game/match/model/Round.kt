package com.motycka.edu.game.match.model

data class RoundData(
    val round: Int,
    val characterId: Int,
    val healthDelta: Int,
    val staminaDelta: Int,
    val manaDelta: Int
)