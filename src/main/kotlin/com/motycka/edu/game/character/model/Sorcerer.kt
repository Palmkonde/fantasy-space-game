package com.motycka.edu.game.character.model

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.interfaces.Character
import com.motycka.edu.game.character.interfaces.Healer
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {  }

class Sorcerer(
    id: Long,
    accountId: AccountId,
    name: String,
    health: Int,
    attackPower: Int,
    level: CharacterLevel,
    experience: Int,
    override val mana: Int,
    override val healingPower: Int,
): Character(
    id = id,
    accountId = accountId,
    name=name,
    health=health,
    attackPower=attackPower,
    level = level,
    experience = experience
), Healer {
    private var currentMana = mana

    init {
        val totalPoints = attackPower + mana + healingPower
        require(totalPoints <= level.points) {
            logger.error {
                "Invaild totalPoints: $totalPoints only allowed ${level.points} at ${level.name}"
            }
        }
    }

    override fun attack(target: Character){
        if(!isAlive) {
            logger.info { "$name is dead and cannot attack" }
            return
        }
        if(mana <= 0){
            logger.info { "$name is too tired to attack" }
            return
        }
        else {
            logger.info { "$name casts a spell at ${target.name}" }
            target.receiveAttack(attackPower)
            currentMana--
            this.heal()
        }
    }

    override fun heal() {
        if(currentMana <= 0) {
            currentMana = 0
            println("$name is out of mana")
            logger.info { "$name is out of mana "}
        }
        else if(currentMana > 0 && currentHealth < health) {
            currentHealth += healingPower
            logger.info { "$name heals self to $currentHealth health "}
            currentMana -= 2
        }
    }

    override fun beforeRounds() {
        currentMana++
    }

    override fun afterRound() {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return super.toString()
    }
}