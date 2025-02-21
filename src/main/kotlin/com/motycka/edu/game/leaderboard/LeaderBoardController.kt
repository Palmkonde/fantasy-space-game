package com.motycka.edu.game.leaderboard

import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class LeaderBoardController(
    private val leaderBoardService: LeaderBoardService
) {
    @GetMapping
    fun getLeaderBoard(
        @RequestParam(value = "class", required = false) className: String? = null
    ): LeaderBoardResponse {

    }
}