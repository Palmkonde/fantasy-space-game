package com.motycka.edu.game.match

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.CharacterService
import com.motycka.edu.game.match.rest.MatchCreateRequest
import com.motycka.edu.game.match.rest.MatchResultResponse
import org.springframework.stereotype.Service
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior
import com.motycka.edu.game.leaderboard.LeaderBoardService
import com.motycka.edu.game.match.model.Fighter
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.model.RoundData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val characterService: CharacterService,
    private val leaderBoardService: LeaderBoardService
) {

    @Transactional
    fun createNewMatch(request: MatchCreateRequest, accountId: AccountId): MatchResultResponse {
        val challenger = characterService.getChallengers(accountId).firstOrNull { it.id == request.challengerId }
        val opponent = characterService.getOpponents(accountId).firstOrNull { it.id == request.opponentId }

        if (challenger == null || opponent == null) {
            error("Challenger: $challenger or Opponent: $opponent is Invalid")
        }

        val rounds = request.rounds
        val roundsResult = mutableListOf<RoundData>()
        var round = 1
        while (challenger.getCurrentHeath() > 0 && opponent.getCurrentHeath() > 0 && round <= rounds) {
            val challengerData = challenger.beforeRounds()
            val opponentData = opponent.beforeRounds()
            challenger.attack(opponent)
            opponent.attack(challenger)

            roundsResult.add(
                createRoundData(
                    data = opponentData,
                    round = round,
                    character = opponent
                )
            )
            roundsResult.add(
                createRoundData(
                    data = challengerData,
                    round = round,
                    character = challenger
                )
            )

            round++
        }

        val winner: Character? = when {
            challenger.getCurrentHeath() <= 0 && opponent.getCurrentHeath() > 0 -> opponent
            opponent.getCurrentHeath() <= 0 && challenger.getCurrentHeath() > 0 -> challenger
            else -> null
        }

        val matchOutcome = when (winner) {
            challenger -> MatchOutcome.CHALLENGER_WON
            opponent -> MatchOutcome.OPPONENT_WON
            else -> MatchOutcome.DRAW
        }

        val challengerExp = if (winner == challenger) 100 else 50
        logger.info { "Challenger Experience Gained: $challengerExp" }
        val challengerResult = Fighter(
            id = challenger.id,
            name = challenger.name,
            characterClass = when (challenger) {
                is Warrior -> CharacterClass.WARRIOR
                else -> CharacterClass.SORCERER
            },
            experienceTotal = challenger.experience + challengerExp,
            experienceGained = challengerExp,
            level = challenger.level.upLevel(
                character = challenger,
                otherPoints = null
            )
        )

        val opponentExp = if (winner == opponent) 100 else 50
        logger.info { "Opponent Experience Gained: $opponentExp" }
        val opponentResult = Fighter(
            id = opponent.id,
            name = opponent.name,
            characterClass = when (opponent) {
                is Warrior -> CharacterClass.WARRIOR
                else -> CharacterClass.SORCERER
            },
            experienceTotal = opponent.experience + opponentExp,
            experienceGained = opponentExp,
            level = opponent.level.upLevel(
                character = opponent,
                otherPoints = null
            )
        )
        val matchResult = matchRepository.saveMatch(
            challengerId = challenger.id,
            opponentId = opponent.id,
            challengerResult = challengerResult,
            opponentResult = opponentResult,
            rounds = roundsResult,
            matchOutcome = matchOutcome
        )

        leaderBoardService.updateLeaderBoardFromMatch(matchResult.id)

        return matchResult
    }

    fun getAllMatches(): List<MatchResultResponse> {
        return matchRepository.getMatches()
    }

    private fun createRoundData(data: List<Int>, round: Int, character: Character): RoundData {
        val (previousHealth, previousStamina, previousMana) = data
        logger.info { "Round: $round, Character: ${character.name}, PreviousHealth:$previousHealth, Health: ${character.getCurrentHeath()}"}
        return RoundData(
            round = round,
            characterId = character.id,
            healthDelta = character.getCurrentHeath() - previousHealth,
            staminaDelta = when (character) {
                is Warrior -> character.getCurrentStamina() - previousStamina
                else -> 0
            },
            manaDelta = when (character) {
                is Sorcerer -> character.getCurrentMana() - previousMana
                else -> 0
            }
        )
    }
}