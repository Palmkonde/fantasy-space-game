package com.motycka.edu.game.character.model

enum class CharacterLevel(
    private val points: Int
) {
    LEVEL_1(0),
    LEVEL_2(100),
    LEVEL_3(200),
    LEVEL_4(300),
    LEVEL_5(400),
    LEVEL_6(500),
    LEVEL_7(600),
    LEVEL_8(700),
    LEVEL_9(800),
    LEVEL_10(900),
    LEVEL_11(1000),
    LEVEL_12(1100),
    LEVEL_13(1200),
    LEVEL_14(1300),
    LEVEL_15(1400),
    LEVEL_16(1500),
    LEVEL_17(1600),
    LEVEL_18(1700),
    LEVEL_19(1800),
    LEVEL_20(1900);

    fun shouldLevelup(currentExp: Int): Boolean {
        return currentExp > this.points
    }

    fun upLevel(currentExp: Int): CharacterLevel {
        return entries.toTypedArray().findLast { it.points <= currentExp } ?: LEVEL_1
    }
}