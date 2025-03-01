package com.motycka.edu.game.account

import com.fasterxml.jackson.databind.ObjectMapper
import com.motycka.edu.game.account.rest.AccountRegistrationRequest
import com.motycka.edu.game.account.rest.toAccountResponse
import com.motycka.edu.game.config.WebMvcTestConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

@WebMvcTest(AccountController::class)
@Import(AccountControllerTest.TestConfig::class, WebMvcTestConfig::class)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun accountService(): AccountService = mockk(relaxed = true)
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var accountService: AccountService

    private val objectMapper = ObjectMapper()
    private val endpoint = "/api/accounts"

    private val newAccount = AccountFixtures.DEVELOPER
    private val existingAccount = AccountFixtures.TESTER

    @BeforeEach
    fun setUp() {
        every { accountService.createAccount(any()) } returns newAccount
        every { accountService.getByUsername(existingAccount.username) } returns existingAccount
        every { accountService.getCurrentAccountId() } returns existingAccount.id!!
        every { accountService.getAccount() } returns existingAccount
    }

    @Test
    fun `postAccount should create account`() {
        val accountRegistrationRequest = AccountRegistrationRequest(
            name = newAccount.name,
            username = newAccount.username,
            password = newAccount.password
        )

        val expectedResponse = objectMapper.writeValueAsString(
            newAccount.toAccountResponse()
        )

        mockMvc.perform(
            post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRegistrationRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(content().json(expectedResponse))

        verify {
            accountService.createAccount(
                account = newAccount.copy(id = null)
            )
        }
    }

    @Test
    fun `getAccounts should return current account`() {
        val expectedResponse = objectMapper.writeValueAsString(
            existingAccount.toAccountResponse()
        )

        mockMvc.perform(
            get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().json(expectedResponse))

        verify {
            accountService.getAccount()
        }
    }
}
