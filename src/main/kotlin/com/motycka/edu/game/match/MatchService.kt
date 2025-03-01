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
        validateRequest(request)
        
        val challenger = getCharacter(request.challengerId, characterService.getChallengers(accountId))
        val opponent = getCharacter(request.opponentId, characterService.getOpponents(accountId))
        
        val roundsResult = executeMatchRounds(challenger, opponent, request.rounds)
        
        val matchOutcome = determineMatchOutcome(challenger, opponent)
        
        val challengerResult = createFighterResult(challenger, matchOutcome == MatchOutcome.CHALLENGER_WON)
        val opponentResult = createFighterResult(opponent, matchOutcome == MatchOutcome.OPPONENT_WON)
        
        val matchResult = saveMatchResult(
            challenger, 
            opponent, 
            challengerResult, 
            opponentResult, 
            roundsResult, 
            matchOutcome
        )
        
        leaderBoardService.updateLeaderBoardFromMatch(matchResult.id)
        
        return matchResult
    }
    

    fun getAllMatches(): List<MatchResultResponse> {
        return matchRepository.getMatches()
    }

    fun getMatchById(matchId: Long): MatchResultResponse {
        return matchRepository.getMatchById(matchId)
            ?: throw IllegalStateException("No match found with ID: $matchId")
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

    private fun validateRequest(request: MatchCreateRequest) {
        if (request.rounds <= 0) {
            throw IllegalArgumentException("Number of rounds must be greater than 0")
        }
    }

    private fun getCharacter(characterId: Long, availableCharacters: List<Character>): Character {
        return availableCharacters.firstOrNull { it.id == characterId }
            ?: throw IllegalArgumentException("Character with ID $characterId not found")
    }

    private fun executeMatchRounds(challenger: Character, opponent: Character, maxRounds: Int): List<RoundData> {
        val roundsResult = mutableListOf<RoundData>()
        var round = 1

        while (challenger.getCurrentHeath() > 0 && opponent.getCurrentHeath() > 0 && round <= maxRounds) {
            val challengerData = challenger.beforeRounds()
            val opponentData = opponent.beforeRounds()

            challenger.attack(opponent)
            opponent.attack(challenger)

            roundsResult.add(createRoundData(opponentData, round, opponent))
            roundsResult.add(createRoundData(challengerData, round, challenger))

            round++
        }

        return roundsResult
    }

    private fun determineMatchOutcome(challenger: Character, opponent: Character): MatchOutcome {
        val winner: Character? = when {
            challenger.getCurrentHeath() <= 0 && opponent.getCurrentHeath() > 0 -> opponent
            opponent.getCurrentHeath() <= 0 && challenger.getCurrentHeath() > 0 -> challenger
            else -> null
        }

        return when (winner) {
            challenger -> MatchOutcome.CHALLENGER_WON
            opponent -> MatchOutcome.OPPONENT_WON
            else -> MatchOutcome.DRAW
        }
    }

    private fun createFighterResult(character: Character, isWinner: Boolean): Fighter {
        val experienceGained = if (isWinner) 100 else 50
        logger.info { "${character.name} Experience Gained: $experienceGained" }

        return Fighter(
            id = character.id,
            name = character.name,
            characterClass = getCharacterClass(character),
            experienceTotal = character.experience + experienceGained,
            experienceGained = experienceGained,
            level = character.level.upLevel(character, null)
        )
    }

    private fun getCharacterClass(character: Character): CharacterClass {
        return when (character) {
            is Warrior -> CharacterClass.WARRIOR
            is Sorcerer -> CharacterClass.SORCERER
            else -> throw IllegalArgumentException("Unknown character type")
        }
    }

    private fun saveMatchResult(
        challenger: Character,
        opponent: Character,
        challengerResult: Fighter,
        opponentResult: Fighter,
        rounds: List<RoundData>,
        matchOutcome: MatchOutcome
    ): MatchResultResponse {
        return matchRepository.saveMatch(
            challengerId = challenger.id,
            opponentId = opponent.id,
            challengerResult = challengerResult,
            opponentResult = opponentResult,
            rounds = rounds,
            matchOutcome = matchOutcome
        )
    }
}