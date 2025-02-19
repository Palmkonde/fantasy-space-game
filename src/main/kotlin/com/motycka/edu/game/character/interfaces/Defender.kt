package com.motycka.edu.game.character.interfaces

interface Defender {
    val defensePower: Int
    val stamina: Int

    fun defend(attackPower: Int): Int
}
