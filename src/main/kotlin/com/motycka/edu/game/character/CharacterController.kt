package com.motycka.edu.game.character

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.rest.CharacterResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@RestController
@RequestMapping("/api/characters")
class CharacterController(
    private val characterService: CharacterService,
    private val accountService: AccountService
) {
    @GetMapping
    fun getCharacter(): CharacterResponse {
        TODO("Implement Service to return this request")
    }

    @GetMapping("/{id}")
    fun getCharacterById(
        @PathVariable id: AccountId
    ): CharacterResponse {
        TODO("Implement Service to return Character with Id selection")
    }

    @PostMapping
    fun postCharacter() {
        TODO("Implement this one tooo")
    }
}