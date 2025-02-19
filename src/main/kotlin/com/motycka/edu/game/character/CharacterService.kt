package com.motycka.edu.game.character

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior
import org.springframework.stereotype.Service

object MockCharacters {
    fun getData(): List<Pair<AccountId, Character>> {
       val characterList = listOf<Pair<AccountId, Character>>(
           1L to Sorcerer(
               name = "Harry Potter",
               health = 100,
               attackPower = 40,
               level = CharacterLevel.LEVEL_1,
               experience = 0,
               mana = 30,
               healingPower = 0,
           ),
           2L to Warrior(
               name= "Luke Skywalker",
               health = 110,
               attackPower = 40,
               level = CharacterLevel.LEVEL_1,
               experience = 0,
               stamina = 30,
               defensePower = 0
           )
       )
    return characterList
    }
}

@Service
class CharacterService(
    private val characterRepository: CharacterRepository
) {
    fun getCharacters(className: String?, name: String?):List<Pair<AccountId, Character>> {
        val data = characterRepository.selectByFilters(className, name)

        return data.filter { (id, element) ->
            (className == null || element::class.simpleName == className) && (name == null || element.name == name)
        }
    }

    fun getCharacterById(id: AccountId): Pair<AccountId, Character> {
        val data = characterRepository.selectById(id)
        return data ?: error("No such a character with ID: $id")
    }

    fun createCharacter(newCharacter: Character): Pair<AccountId, Character> {

       return newId to newCharacter
    }
}