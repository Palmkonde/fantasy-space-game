package com.motycka.edu.game.character.rest

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior


fun List<Pair<AccountId, Character>>.toCharacterResponse(
    userId: AccountId,
): List<CharacterResponse> {
    return this.map { (elementId, element) ->
        CharacterResponse(
            id = elementId,
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
            isOwner = elementId == userId
        )
    }
}

fun Pair<AccountId, Character>.toCharacterResponse(
    userId: AccountId,
): CharacterResponse {
    return CharacterResponse(
            id = this.first,
            name = this.second.name,
            health = this.second.health,
            attackPower = this.second.attackPower,

            stamina = when(this.second) {
                is Warrior -> (this.second as Warrior).stamina
                else -> null
            },
            defensePower = when(this.second) {
                is Warrior -> (this.second as Warrior).defensePower
                else -> null
            },

            mana = when(this.second){
                is Sorcerer -> (this.second as Sorcerer).mana
                else -> null
            },
            healingPower = when(this.second) {
                is Sorcerer -> (this.second as Sorcerer).healingPower
                else -> null
            },

            characterClass = when(this.second) {
                is Warrior -> CharacterClass.WARRIOR
                is Sorcerer -> CharacterClass.SORCERER
                else -> error("Require CharacterClass")
            },

            level = this.second.level,
            experience = this.second.experience,
            shouldLevelUp = this.second.level.shouldLevelup(this.second.experience),
            isOwner = this.first == userId
    )
}

