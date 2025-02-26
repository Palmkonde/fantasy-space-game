package com.motycka.edu.game.account

import com.motycka.edu.game.account.model.Account
import com.motycka.edu.game.account.rest.AccountRegistrationRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf

@WebMvcTest(AccountController::class)
class AccountControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var accountService: AccountService

    private val objectMapper = ObjectMapper()

    private val accountRegistrationRequest = AccountRegistrationRequest(
        name = "The Developer",
        username = "developer",
        password = "password"
    )

    private val account = Account(
        id = 4L,
        name = "The Developer",
        username = "developer",
        password = "password"
    )

    @BeforeEach
    fun setUp() {
        accountService = mockk()
        every { accountService.createAccount(any()) } returns account
    }

    @Test
    fun `postAccount should create account`() {
        mockMvc.perform(
            post("/api/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRegistrationRequest))
        ).andExpect(status().isOk)

        verify { accountService.createAccount(account) }
    }
}
