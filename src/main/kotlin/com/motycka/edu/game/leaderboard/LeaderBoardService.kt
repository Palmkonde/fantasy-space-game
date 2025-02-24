package com.motycka.edu.game.leaderboard

import org.springframework.stereotype.Service

@Service
class LeaderBoardService(
    private val leaderBoardRepository: LeaderBoardRepository
) {
    fun getLeaderBoard(className: String?): List<LeaderBoard> {
        return leaderBoardRepository.getLeaderBoard(className)
    }

    fun updateLeaderBoardFromMatch(matchId: Long) {
        leaderBoardRepository.updateLeaderBoardFromMatch(matchId = matchId)
    }
}