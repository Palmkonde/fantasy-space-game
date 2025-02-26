package com.motycka.edu.game.character.model

import com.motycka.edu.game.character.interfaces.Character

enum class CharacterLevel(
    val points: Int,
    val requireExp: Int
) {
    LEVEL_1(200, 300),
    LEVEL_2(210, 600),
    LEVEL_3(230, 900),
    LEVEL_4(260, 1050),
    LEVEL_5(300, 1550),
    LEVEL_6(350, 2100),
    LEVEL_7(410, 2700),
    LEVEL_8(480, 3300),
    LEVEL_9(560, 3950),
    LEVEL_10(650, 4650);

    fun shouldLevelup(currentExp: Int): Boolean {
        return currentExp > this.requireExp
    }

    fun upLevel(character: Character?, otherPoints: List<Int>?): CharacterLevel {
        var totalPoints: Int
        if (character != null) {
            totalPoints = character.health + when(character) {
                is Warrior -> character.attackPower + character.stamina + character.defensePower
                is Sorcerer -> character.attackPower + character.mana + character.healingPower
                else -> error("Require Character")
            }
        }
        else if(otherPoints != null ) {
            require(otherPoints.size == 4) {
                "Require 4 points"
            }
            totalPoints = otherPoints.sum()
        }
        else {
            error("Require Character or List<Int>")
        }

        return entries.toTypedArray().findLast { it.points <= totalPoints} ?: LEVEL_1
    }
}