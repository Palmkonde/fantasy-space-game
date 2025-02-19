package com.motycka.edu.game.character.rest

import com.motycka.edu.game.character.model.CharacterClass

data class CharacterCreateRequest(
    val name: String,
    val health: Int,
    val attackPower: Int,

    val stamina: Int?,
    val defensePower: Int?,

    val mana: Int?,
    val healingPower: Int?,
    val characterClass: CharacterClass
)
