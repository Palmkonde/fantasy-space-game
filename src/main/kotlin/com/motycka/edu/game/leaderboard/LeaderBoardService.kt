package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.leaderboard.rest.LeaderBoardResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LeaderBoardService(
    private val leaderBoardRepository: LeaderBoardRepository,
    private val accountService: AccountService
) {
    fun getLeaderBoard(className: String?): List<LeaderBoardResponse> {
        val accountId = accountService.getCurrentAccountId()
        return leaderBoardRepository.getLeaderBoard(className, accountId)
    }

    @Transactional
    fun updateLeaderBoardFromMatch(matchId: Long) {
        leaderBoardRepository.updateLeaderBoardFromMatch(matchId = matchId)
    }
}