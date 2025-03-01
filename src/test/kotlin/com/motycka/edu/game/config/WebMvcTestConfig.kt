package com.motycka.edu.game.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@TestConfiguration
class WebMvcTestConfig : WebMvcConfigurer {
    // This class is intentionally empty
    // It serves as a marker for test configurations
} 