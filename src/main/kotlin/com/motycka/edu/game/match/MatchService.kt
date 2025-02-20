package com.motycka.edu.game.match

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.CharacterRepository
import com.motycka.edu.game.match.rest.MatchCreateRequest
import com.motycka.edu.game.match.rest.MatchResultResponse
import org.springframework.stereotype.Service
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.Sorcerer
import com.motycka.edu.game.character.model.Warrior
import com.motycka.edu.game.match.model.Fighter
import com.motycka.edu.game.match.model.RoundData

@Service
class MatchService(
    private val characterRepository: CharacterRepository,
    private val matchRepository: MatchRepository
) {
    fun createNewMatch(request: MatchCreateRequest, accountId: AccountId): MatchResultResponse {
        val challenger = characterRepository.getOwnedCharacters(accountId)?.firstOrNull { it.id == request.challengerId }
        val opponent = characterRepository.getNotOwnedCharacters(accountId)?.firstOrNull { it.id == request.opponentId }

        if(challenger == null || opponent == null){
            error("Challenger: $challenger or Opponent: $opponent is Invalid")
        }

        val rounds = request.rounds
        val roundsResult = mutableListOf<RoundData>()
        var round = 1
        while( challenger.getCurrentHeath() > 0 && opponent.getCurrentHeath() > 0 && round <= rounds) {
            if(round % 2 == 0) {
                val data = challenger.beforeRounds()
                challenger.attack(opponent)

                roundsResult.add(createRoundData(
                    data,
                    round,
                    opponent
                ))
            }
            else {
                val data = opponent.beforeRounds()
                opponent.attack(challenger)

                roundsResult.add(createRoundData(
                    data,
                    round,
                    challenger
                ))
            }

            round++
        }

        val winner: Character? = when {
            challenger.getCurrentHeath() <= 0 && opponent.getCurrentHeath() > 0 -> opponent
            opponent.getCurrentHeath() <= 0 && challenger.getCurrentHeath() > 0 -> challenger
            else -> null
        }

        val challengerExp = if(winner == challenger) 100 else 50
        val challengerResult = Fighter(
            id = challenger.id,
            name = challenger.name,
            characterClass = when(challenger) {
                is Warrior -> CharacterClass.WARRIOR
                else -> CharacterClass.SORCERER
            },
            isVictor = winner == challenger,
            experienceTotal = challenger.experience + challengerExp,
            experienceGained = challengerExp,
            level = challenger.level.upLevel(challenger.experience + challengerExp)
        )

        val opponentExp = if(winner == opponent) 100 else 50
        val oppenetResult = Fighter(
            id = opponent.id,
            name = opponent.name,
            characterClass = when(opponent) {
                is Warrior -> CharacterClass.WARRIOR
                else -> CharacterClass.SORCERER
            },
            isVictor = winner == opponent,
            experienceTotal = opponent.experience + opponentExp,
            experienceGained = opponentExp,
            level = opponent.level.upLevel(opponent.experience + opponentExp)
        )

        val matchResult = match
        return data
    }


    fun createRoundData(data: List<Int>, round:Int, character: Character): RoundData {
        val (health, stamina, mana) = data
        RoundData(
            round = round,
            characterId = character.id,
            healthDelta = character.getCurrentHeath() - health,
            staminaDelta = when(character) {
                is Warrior -> stamina - character.getCurrentStamina()
                else -> 0
            },
            manaDelta = when(character) {
                is Sorcerer -> mana - character.getCurrentMana()
                else -> 0
            }
        )
    }
}