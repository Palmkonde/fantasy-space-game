package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.leaderboard.rest.LeaderBoardResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/leaderboards")
class LeaderBoardController(
    private val leaderBoardService: LeaderBoardService
) {
    @GetMapping
    fun getLeaderBoard(
        @RequestParam(value = "class", required = false) className: String? = null
    ): List<LeaderBoardResponse> {
        return leaderBoardService.getLeaderBoard(className)
    }
}