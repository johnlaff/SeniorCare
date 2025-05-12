package br.com.uniube.seniorcare.web.controller;

import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.Role;
import br.com.uniube.seniorcare.service.UserService;
import br.com.uniube.seniorcare.web.dto.request.UserRequest;
import br.com.uniube.seniorcare.web.dto.response.UserResponse;
import br.com.uniube.seniorcare.web.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    @Test
    void findAllReturnsListOfUsersAndStatus200() {
        // Arrange
        List<User> users = List.of(createUser(), createUser());
        List<UserResponse> responses = List.of(createUserResponse(), createUserResponse());

        when(userService.findAll()).thenReturn(users);
        when(userMapper.toDtoList(users)).thenReturn(responses);

        // Act
        ResponseEntity<List<UserResponse>> response = userController.findAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responses, response.getBody());
    }

    @Test
    void findByIdReturnsUserAndStatus200WhenIdExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        User user = createUser();
        UserResponse response = createUserResponse();

        when(userService.findById(id)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(response);

        // Act
        ResponseEntity<UserResponse> result = userController.findById(id);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void findByIdThrowsExceptionWhenIdDoesNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();

        when(userService.findById(id)).thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userController.findById(id));
    }

    @Test
    void createReturnsCreatedUserAndStatus201() {
        // Arrange
        UserRequest request = createUserRequest();
        User user = createUser();
        User createdUser = createUser();
        UserResponse response = createUserResponse();

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userService.createUser(user)).thenReturn(createdUser);
        when(userMapper.toDto(createdUser)).thenReturn(response);

        // Act
        ResponseEntity<UserResponse> result = userController.create(request);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void updateReturnsUpdatedUserAndStatus200() {
        // Arrange
        UUID id = UUID.randomUUID();
        UserRequest request = createUserRequest();
        User user = createUser();
        User updatedUser = createUser();
        UserResponse response = createUserResponse();

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userService.updateUser(id, user)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(response);

        // Act
        ResponseEntity<UserResponse> result = userController.update(id, request);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteReturnsStatus204WhenUserIsDeleted() {
        // Arrange
        UUID id = UUID.randomUUID();

        doNothing().when(userService).deleteUser(id);

        // Act
        ResponseEntity<Void> result = userController.delete(id);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userService, times(1)).deleteUser(id);
    }

    private User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.ADMIN);
        return user;
    }

    private UserResponse createUserResponse() {
        UserResponse response = new UserResponse();
        response.setId(UUID.randomUUID());
        response.setOrganizationId(UUID.randomUUID());
        response.setName("Test User");
        response.setEmail("test@example.com");
        response.setRole(Role.ADMIN);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    private UserRequest createUserRequest() {
        UserRequest request = new UserRequest();
        request.setOrganizationId(UUID.randomUUID());
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setRole(Role.ADMIN);
        return request;
    }
}