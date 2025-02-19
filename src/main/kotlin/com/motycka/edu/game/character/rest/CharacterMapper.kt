package com.motycka.edu.game.character.rest

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior


fun List<Pair<AccountId, Character>>.toCharacterResponse(
    id: AccountId,
): List<CharacterResponse> {
    return this.map { (elementId, element) ->
        CharacterResponse(
            id = id,
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
            isOwner = elementId == id
        )
    }
}