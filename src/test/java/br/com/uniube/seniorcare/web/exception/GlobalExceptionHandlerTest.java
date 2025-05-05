package br.com.uniube.seniorcare.web.exception;

import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.web.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleBusinessException_ShouldReturnBadRequest() {
        // Arrange
        BusinessException ex = new BusinessException("Erro de negócio");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Erro de negócio", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequestWithErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "mensagem de erro");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Há campos com valores inválidos", errorResponse.getMessage());
        assertEquals(1, errorResponse.getErrors().size());
        assertEquals("field", errorResponse.getErrors().get(0).getField());
        assertEquals("mensagem de erro", errorResponse.getErrors().get(0).getMessage());
    }

    @Test
    void handleNoSuchElementException_ShouldReturnNotFound() {
        // Arrange
        NoSuchElementException ex = new NoSuchElementException("Recurso não encontrado");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNoSuchElementException(ex, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("Recurso não encontrado", errorResponse.getMessage());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Arrange
        Exception ex = new RuntimeException("Erro inesperado");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("Ocorreu um erro inesperado. Por favor, contate o administrador do sistema.", errorResponse.getMessage());
    }
}