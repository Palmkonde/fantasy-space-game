package com.motycka.edu.game.character.interfaces

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.model.CharacterLevel
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {  }

abstract class Character(
    open val id: Long,
    open val accountId: AccountId,
    open val name: String,
    open val health: Int,
    open val attackPower: Int,
    open val level: CharacterLevel,
    open val experience: Int
): Recoverable {

    protected var currentHealth = health
    protected val isAlive: Boolean
        get() = currentHealth > 0


    open fun receiveAttack(attackPower: Int){
        currentHealth -= attackPower

        if(currentHealth <= 0) {
            logger.info{ "$name has been defeated" }
            currentHealth = 0
        }
        else {
            logger.info { "$name has $currentHealth remaining" }
        }
    }

    open fun getCurrentHeath(): Int {
        return this.currentHealth
    }

    abstract fun attack(target: Character)
}
