package com.motycka.edu.game.match

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/matches")
class MatchController {

    @GetMapping
    fun getMatchResult() {

    }
}