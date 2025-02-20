package com.motycka.edu.game.match

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.character.CharacterService
import com.motycka.edu.game.match.rest.MatchCreateRequest
import com.motycka.edu.game.match.rest.MatchResultResponse
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties.Http
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/matches")
class MatchController(
    private val characterService: CharacterService,
    private val matchService: MatchService,
    private val accountService: AccountService
) {

    @PostMapping
    fun createNewMatch(
        @RequestBody request: MatchCreateRequest
    ): ResponseEntity<MatchResultResponse?> {
        val accountId = accountService.getCurrentAccountId()
        val response = matchService.createNewMatch(request, accountId)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}