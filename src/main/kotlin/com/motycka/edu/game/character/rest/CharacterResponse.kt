package com.motycka.edu.game.character.rest

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel

data class CharacterResponse(
    val id: AccountId,
    val name: String,
    val health: Int,
    val attackPower: Int,

    val stamina: Int?,
    val defensePower: Int?,

    val mana: Int?,
    val healingPower: Int?,

    val characterClass: CharacterClass,
    val level: CharacterLevel,
    val experience: Int,
    val shouldLevelUp: Boolean,
    val isOwner: Boolean
)