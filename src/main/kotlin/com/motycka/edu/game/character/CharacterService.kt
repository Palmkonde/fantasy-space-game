package com.motycka.edu.game.character

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.rest.CharacterCreateRequest
import com.motycka.edu.game.character.rest.CharacterLevelUpRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CharacterService(
    private val characterRepository: CharacterRepository
) {
    fun getCharacters(className: String?, name: String?):List<Character> {
        val data = characterRepository.selectByFilters(className, name)
        return data
    }

    fun getCharacterById(id: Long): Character {
        val data = characterRepository.selectById(id)
        return data ?: error("No such a character with ID: $id")
    }

    @Transactional
    fun createCharacter(newCharacter: CharacterCreateRequest, accountId: AccountId): Character {
        return characterRepository.insertCharacter(newCharacter, accountId) ?: error("Error Can't create Charactor")
    }

    fun getChallengers(accountId: AccountId): List<Character> {
        val data = characterRepository.getOwnedCharacters(accountId)
        return data ?: error("Can't require Challengers")
    }

    fun getOpponents(accountId: AccountId): List<Character> {
        val data = characterRepository.getNotOwnedCharacters(accountId)
        return data ?: error("Can't require Opponents")
    }

    @Transactional
    fun upLevelCharacterById(id: Long, updateCharacter: CharacterLevelUpRequest): Int {
        val data = characterRepository.upLevelCharacter(id, updateCharacter)
        return data ?: error("Character Can't LevelUp")
    }
}