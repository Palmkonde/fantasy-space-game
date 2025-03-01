package com.motycka.edu.game.match.model

import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel

data class Fighter(
    val id: Long,
    val name: String,
    val characterClass: CharacterClass,
    val level: CharacterLevel,
    val experienceTotal: Int,
    val experienceGained: Int,
)
