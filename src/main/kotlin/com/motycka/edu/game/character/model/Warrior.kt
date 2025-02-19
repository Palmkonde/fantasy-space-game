package com.motycka.edu.game.character.model

import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.interfaces.Defender
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.max

private val logger = KotlinLogging.logger {  }

class Warrior(
    name: String,
    health: Int,
    attackPower: Int,
    level: CharacterLevel,
    override val stamina: Int,
    override val defensePower: Int,
): Character(
    name = name,
    health = health,
    attackPower = attackPower,
    level = level
), Defender {
    private var currentStamina = stamina

    init {
        val totalPoints = attackPower + currentStamina + defensePower
        require(totalPoints <= level.points) {
            logger.error {
                "Invaild totalPoints: $totalPoints only allowed ${level.points} at ${level.name}"
            }
        }
    }

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
            target.receiveAttack(attackPower)
            currentStamina -= 1
        }
    }

    override fun defend(attackPower: Int): Int {
        val result = max(0, attackPower - defensePower)

        if(currentStamina <= 0) {
            logger.info{"$name is too tired to defend"}
            return attackPower
        }

        else if(currentStamina > 0) {
            logger.info{"$name raises shield and defends against $defensePower damage"}
            currentStamina -= 1
            return result
        }
        return attackPower
    }

    override fun receiveAttack(attackPower: Int) {
        super.receiveAttack(defend(attackPower))
    }

    override fun beforeRounds() {
       currentStamina++
    }

    override fun afterRound() {
        TODO("Not yet implemented")
    }
}
