package com.vvise.auth

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@SpringBootApplication
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}

//@Component
//class StartupLogger {
//    private val log = LoggerFactory.getLogger(StartupLogger::class.java)
//
//    @EventListener(ApplicationReadyEvent::class)
//    fun onApplicationReady() {
//        log.info("=== Auth Server Configuration ===")
//        log.info("OAUTH2_BASE_URL: ${System.getenv("OAUTH2_BASE_URL") ?: "(not set, using default)"}")
//        log.info("OAUTH2_REDIRECT_URI: ${System.getenv("OAUTH2_REDIRECT_URI") ?: "(not set, using default)"}")
//        log.info("CORS_ALLOWED_ORIGINS: ${System.getenv("CORS_ALLOWED_ORIGINS") ?: "(not set, using default)"}")
//        log.info("=================================")
//    }
//}
