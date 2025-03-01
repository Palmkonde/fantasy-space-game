package com.motycka.edu.game.character.rest

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior


fun List<Character>.toCharacterResponse(
    userId: AccountId,
): List<CharacterResponse> {
    return this.map { element ->
        CharacterResponse(
            id = element.id,
            name = element.name,
            health = element.health,
            attackPower = element.attackPower,

            stamina = when(element) {
                is Warrior -> element.stamina
                else -> null
            },
            defensePower = when(element) {
                is Warrior -> element.defensePower
                else -> null
            },

            mana = when(element){
                is Sorcerer -> element.mana
                else -> null
            },
            healingPower = when(element) {
                is Sorcerer -> element.healingPower
                else -> null
            },

            characterClass = when(element) {
                is Warrior -> CharacterClass.WARRIOR
                is Sorcerer -> CharacterClass.SORCERER
                else -> error("Require CharacterClass")
            },

            level = element.level,
            experience = element.experience,
            shouldLevelUp = element.level.shouldLevelup(element.experience),
            isOwner = element.accountId == userId
        )
    }
}

fun Character.toCharacterResponse(
    userId: AccountId,
): CharacterResponse {
    return CharacterResponse(
            id = this.id,
            name = this.name,
            health = this.health,
            attackPower = this.attackPower,

            stamina = when(this) {
                is Warrior -> this.stamina
                else -> null
            },
            defensePower = when(this) {
                is Warrior -> this.defensePower
                else -> null
            },

            mana = when(this){
                is Sorcerer -> this.mana
                else -> null
            },
            healingPower = when(this) {
                is Sorcerer -> this.healingPower
                else -> null
            },

            characterClass = when(this) {
                is Warrior -> CharacterClass.WARRIOR
                is Sorcerer -> CharacterClass.SORCERER
                else -> error("Require CharacterClass")
            },

            level = this.level,
            experience = this.experience,
            shouldLevelUp = this.level.shouldLevelup(this.experience),
            isOwner = this.accountId == userId
    )
}
