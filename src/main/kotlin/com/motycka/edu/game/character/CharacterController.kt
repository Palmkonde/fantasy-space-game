package com.motycka.edu.game.character

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.rest.CharacterCreateRequest
import com.motycka.edu.game.character.rest.CharacterResponse
import com.motycka.edu.game.character.rest.toCharacter
import com.motycka.edu.game.character.rest.toCharacterResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

private val logger = KotlinLogging.logger {  }

@RestController
@RequestMapping("/api/characters")
class CharacterController(
    private val characterService: CharacterService,
    private val accountService: AccountService
) {

    @GetMapping
    fun getCharacter(
        @RequestParam(value = "class", required = false) className: String? = null,
        @RequestParam(value = "name", required = false) name: String? = null
    ): List<CharacterResponse> {
        val accountId = accountService.getCurrentAccountId()
        logger.debug { "Current Account ID: $accountId" }
        return characterService.getCharacters(className, name).toCharacterResponse(accountId)
    }

    @GetMapping("/{id}")
    fun getCharacterById(
        @PathVariable id: AccountId
    ): CharacterResponse {
        val accountId = accountService.getCurrentAccountId()
        logger.debug { "$accountId is require Character of $id" }
        return characterService.getCharacterById(id).toCharacterResponse(accountId)
    }

    @PostMapping
    fun postCharacter(
        @RequestBody newCharacter: CharacterCreateRequest
    ): ResponseEntity<CharacterResponse?> {
        val accountId = accountService.getCurrentAccountId()
        val response = characterService.createCharacter(
            newCharacter.toCharacter()
        ).toCharacterResponse(accountId)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}