package com.motycka.edu.game.character.interfaces

interface Recoverable {
    fun beforeRounds(): List<Int>
    fun afterRound()
}