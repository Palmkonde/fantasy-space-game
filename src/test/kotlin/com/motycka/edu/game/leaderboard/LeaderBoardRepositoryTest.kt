package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.character.model.CharacterClass
import com.motycka.edu.game.character.model.CharacterLevel
import com.motycka.edu.game.character.rest.CharacterResponse
import com.motycka.edu.game.match.model.MatchOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration

@JdbcTest
@ContextConfiguration(classes = [LeaderBoardRepository::class])
class LeaderBoardRepositoryTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var leaderBoardRepository: LeaderBoardRepository

    private val accountId = 1L
    private val otherAccountId = 2L
    private val warriorCharacterId = 1L
    private val sorcererCharacterId = 2L
    private val matchId = 1L

    private val warriorCharacter = CharacterResponse(
        id = warriorCharacterId,
        name = "TestWarrior",
        characterClass = CharacterClass.WARRIOR,
        level = CharacterLevel.LEVEL_1,
        experience = 0,
        health = 100,
        attackPower = 50,
        defensePower = 30,
        stamina = 40,
        mana = null,
        healingPower = null,
        isOwner = true,
        shouldLevelUp = false
    )

    private val sorcererCharacter = CharacterResponse(
        id = sorcererCharacterId,
        name = "TestSorcerer",
        characterClass = CharacterClass.SORCERER,
        level = CharacterLevel.LEVEL_1,
        experience = 0,
        health = 80,
        attackPower = 40,
        defensePower = null,
        stamina = null,
        mana = 60,
        healingPower = 50,
        isOwner = false,
        shouldLevelUp = false
    )

    @BeforeEach
    fun setUp() {
        // Clear existing data
        jdbcTemplate.update("DELETE FROM leaderboard")
        jdbcTemplate.update("DELETE FROM match_round_data")
        jdbcTemplate.update("DELETE FROM match")
        jdbcTemplate.update("DELETE FROM character")

        // Insert test characters
        jdbcTemplate.update("""
            INSERT INTO character (id, account_id, name, character_class, level, experience, health, attack_power, defense_power, stamina, mana, healing_power)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, warriorCharacterId, accountId, "TestWarrior", "WARRIOR", "LEVEL_1", 0, 100, 50, 30, 40, null, null)

        jdbcTemplate.update("""
            INSERT INTO character (id, account_id, name, character_class, level, experience, health, attack_power, defense_power, stamina, mana, healing_power)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, sorcererCharacterId, otherAccountId, "TestSorcerer", "SORCERER", "LEVEL_1", 0, 80, 40, null, null, 60, 50)

        // Insert test match
        jdbcTemplate.update("""
            INSERT INTO match (id, challenger_id, opponent_id, outcome)
            VALUES (?, ?, ?, ?)
        """, matchId, warriorCharacterId, sorcererCharacterId, MatchOutcome.CHALLENGER_WON.name)

        // Insert initial leaderboard entries
        jdbcTemplate.update("""
            INSERT INTO leaderboard (character_id, wins, losses, draws)
            VALUES (?, ?, ?, ?)
        """, warriorCharacterId, 10, 2, 1)

        jdbcTemplate.update("""
            INSERT INTO leaderboard (character_id, wins, losses, draws)
            VALUES (?, ?, ?, ?)
        """, sorcererCharacterId, 8, 3, 2)
    }

    @Test
    fun `getLeaderBoard should return all entries when no class filter`() {
        val result = leaderBoardRepository.getLeaderBoard(null, accountId)

        assert(result.size == 2)
        assert(result[0].position == 1)
        assert(result[0].character.id == warriorCharacterId)
        assert(result[0].character.characterClass == CharacterClass.WARRIOR)
        assert(result[0].character.isOwner)
        assert(result[0].wins == 10)
        assert(result[0].losses == 2)
        assert(result[0].draws == 1)
        
        assert(result[1].position == 2)
        assert(result[1].character.id == sorcererCharacterId)
        assert(result[1].character.characterClass == CharacterClass.SORCERER)
        assert(!result[1].character.isOwner)
        assert(result[1].wins == 8)
        assert(result[1].losses == 3)
        assert(result[1].draws == 2)
    }

    @Test
    fun `getLeaderBoard should return filtered warrior entries`() {
        val result = leaderBoardRepository.getLeaderBoard("WARRIOR", accountId)

        assert(result.size == 1)
        assert(result[0].position == 1)
        assert(result[0].character.id == warriorCharacterId)
        assert(result[0].character.characterClass == CharacterClass.WARRIOR)
        assert(result[0].character.isOwner)
        assert(result[0].wins == 10)
        assert(result[0].losses == 2)
        assert(result[0].draws == 1)
    }

    @Test
    fun `getLeaderBoard should return filtered sorcerer entries`() {
        val result = leaderBoardRepository.getLeaderBoard("SORCERER", accountId)

        assert(result.size == 1)
        assert(result[0].position == 1)  // Position is 1 since it's the only entry in filtered results
        assert(result[0].character.id == sorcererCharacterId)
        assert(result[0].character.characterClass == CharacterClass.SORCERER)
        assert(!result[0].character.isOwner)
        assert(result[0].wins == 8)
        assert(result[0].losses == 3)
        assert(result[0].draws == 2)
    }

    @Test
    fun `getLeaderBoard should return empty list when no entries found`() {
        jdbcTemplate.update("DELETE FROM leaderboard")
        
        val result = leaderBoardRepository.getLeaderBoard(null, accountId)
        
        assert(result.isEmpty())
    }

    @Test
    fun `updateLeaderBoardFromMatch should update winner and loser stats`() {
        leaderBoardRepository.updateLeaderBoardFromMatch(matchId)

        // Verify winner (warrior) stats
        val warriorStats = jdbcTemplate.queryForMap("""
            SELECT wins, losses, draws FROM leaderboard WHERE character_id = ?
        """, warriorCharacterId)
        assert(warriorStats["wins"] == 11)
        assert(warriorStats["losses"] == 2)
        assert(warriorStats["draws"] == 1)

        // Verify loser (sorcerer) stats
        val sorcererStats = jdbcTemplate.queryForMap("""
            SELECT wins, losses, draws FROM leaderboard WHERE character_id = ?
        """, sorcererCharacterId)
        assert(sorcererStats["wins"] == 8)
        assert(sorcererStats["losses"] == 4)
        assert(sorcererStats["draws"] == 2)
    }

    @Test
    fun `updateLeaderBoardFromMatch should handle draw outcome`() {
        // Update match to be a draw
        jdbcTemplate.update("""
            UPDATE match SET outcome = ? WHERE id = ?
        """, MatchOutcome.DRAW.name, matchId)

        leaderBoardRepository.updateLeaderBoardFromMatch(matchId)

        // Verify both characters' stats
        val warriorStats = jdbcTemplate.queryForMap("""
            SELECT wins, losses, draws FROM leaderboard WHERE character_id = ?
        """, warriorCharacterId)
        assert(warriorStats["wins"] == 10)
        assert(warriorStats["losses"] == 2)
        assert(warriorStats["draws"] == 2)

        val sorcererStats = jdbcTemplate.queryForMap("""
            SELECT wins, losses, draws FROM leaderboard WHERE character_id = ?
        """, sorcererCharacterId)
        assert(sorcererStats["wins"] == 8)
        assert(sorcererStats["losses"] == 3)
        assert(sorcererStats["draws"] == 3)
    }

    @Test
    fun `updateLeaderBoardFromMatch should throw exception when match not found`() {
        val nonExistentMatchId = 999L
        
        assertThrows<NoSuchElementException> {
            leaderBoardRepository.updateLeaderBoardFromMatch(nonExistentMatchId)
        }
    }
}