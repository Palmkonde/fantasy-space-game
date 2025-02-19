package com.motycka.edu.game.character.interfaces

interface Healer {
    val healingPower: Int
    val mana: Int

    fun heal()
}
