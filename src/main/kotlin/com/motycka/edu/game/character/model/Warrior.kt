package com.motycka.edu.game.character.model

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.interfaces.Defender
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.max

private val logger = KotlinLogging.logger {  }

class Warrior(
    id: Long,
    accountId: AccountId,
    name: String,
    health: Int,
    attackPower: Int,
    override val level: CharacterLevel,
    experience: Int,
    override val stamina: Int,
    override val defensePower: Int,
): Character(
    id = id,
    accountId = accountId,
    name = name,
    health = health,
    attackPower = attackPower,
    level = level,
    experience = experience
), Defender {

    init {
        val totalPoints = attackPower + stamina + defensePower
        require(totalPoints <= level.points) {
            "Total points $totalPoints exceeds level ${level.points}"
        }
    }

    private val currentAttackPower = attackPower
    private val currentDefensePower = defensePower
    private var currentStamina = stamina

    override fun attack(target: Character){
        if(!isAlive) {
            logger.info{"$name is dead and cannot attack"}
            return
        }
        if(currentStamina<= 0){
            logger.info{"$name is too tired to attack"}
            return
        }
        else {
            logger.info{"$name swings a sword at ${target.name}"}
            target.receiveAttack(currentAttackPower)
            currentStamina -= 1
        }
    }

    override fun defend(attackPower: Int): Int {
        val result = max(0, attackPower - currentDefensePower)

        if(currentStamina <= 0) {
            currentStamina = 0
            logger.info{"$name is too tired to defend"}
            return attackPower
        }

        else if(currentStamina > 0) {
            logger.info{"$name raises shield and defends against $currentDefensePower damage"}
            currentStamina -= 1
            return result
        }
        return attackPower
    }

    override fun receiveAttack(attackPower: Int) {
        super.receiveAttack(defend(attackPower))
    }

    override fun beforeRounds():List<Int> {
        currentStamina++

        return listOf(currentHealth, currentStamina, 0)
    }

    fun getCurrentStamina(): Int {
        return currentStamina
    }

    override fun afterRound() {
        TODO("Not yet implemented")
    }
}
