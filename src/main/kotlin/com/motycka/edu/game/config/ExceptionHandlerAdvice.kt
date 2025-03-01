package com.motycka.edu.game.config

import com.motycka.edu.game.error.ErrorResponse
import com.motycka.edu.game.error.NotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandlerAdvice : ResponseEntityExceptionHandler() {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        return handleResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        return handleResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(AuthenticationException::class)
    fun unauthorizedError(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        return handleResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }
    
    @ExceptionHandler(RuntimeException::class)
    fun handleServerError(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        return handleResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    fun handleResponse(ex: Exception, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        val response = ErrorResponse(ex.message ?: "Unknown error", status.value());
        return handleExceptionInternal(ex, response, HttpHeaders(), status, request);
    }
}
