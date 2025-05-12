package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.Role;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import br.com.uniube.seniorcare.service.AuditService;
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityUtils securityUtils;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, auditService, passwordEncoder, securityUtils);
    }

    @Test
    void findAllShouldReturnAllUsers() {
        // Arrange
        List<User> expectedUsers = List.of(createUser("user1@example.com"), createUser("user2@example.com"));
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> result = userService.findAll();

        // Assert
        assertEquals(expectedUsers, result);
        verify(userRepository).findAll();
    }

    @Test
    void findByIdShouldReturnUserWhenExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        User expectedUser = createUser("user@example.com");
        when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));

        // Act
        User result = userService.findById(id);

        // Assert
        assertEquals(expectedUser, result);
        verify(userRepository).findById(id);
    }

    @Test
    void findByIdShouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.findById(id));
        assertEquals("Usuário não encontrado com o id: " + id, exception.getMessage());
        verify(userRepository).findById(id);
    }

    @Test
    void findByEmailShouldReturnUserWhenExists() {
        // Arrange
        String email = "user@example.com";
        User expectedUser = createUser(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void createUserShouldSaveAndReturnUserWhenValid() {
        // Arrange
        User user = createUser("user@example.com");
        user.setPassword("Password123");
        UUID currentUserId = UUID.randomUUID();
        UUID organizationId = user.getOrganization().getId();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(securityUtils.getCurrentUserId()).thenReturn(currentUserId);

        // Act
        User result = userService.createUser(user);

        // Assert
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(user, result);
        verify(userRepository).existsByEmail(user.getEmail());
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(user);
        verify(auditService).recordEvent(
                eq(organizationId),
                eq(currentUserId),
                eq("CREATE_USER"),
                eq("Usuário"),
                eq(user.getId()),
                anyString()
        );
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailExists() {
        // Arrange
        User user = createUser("existing@example.com");
        user.setPassword("Password123");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.createUser(user));
        assertEquals("Email já está em uso: " + user.getEmail(), exception.getMessage());
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailIsInvalid() {
        // Arrange
        User user = createUser("invalid-email");
        user.setPassword("Password123");

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.createUser(user));
        assertEquals("Formato de email inválido", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserShouldThrowExceptionWhenPasswordIsInvalid() {
        // Arrange
        User user = createUser("user@example.com");
        user.setPassword("weak");

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.createUser(user));
        assertEquals("A senha deve conter pelo menos 8 caracteres, incluindo letras e números", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserShouldUpdateAndReturnUserWhenValid() {
        // Arrange
        UUID id = UUID.randomUUID();
        User existingUser = createUser("existing@example.com");
        User updatedUser = createUser("updated@example.com");
        updatedUser.setName("Updated Name");
        updatedUser.setRole(Role.CAREGIVER);
        UUID currentUserId = UUID.randomUUID();
        UUID organizationId = existingUser.getOrganization().getId();

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(updatedUser.getEmail())).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(securityUtils.getCurrentUserId()).thenReturn(currentUserId);

        // Act
        User result = userService.updateUser(id, updatedUser);

        // Assert
        assertEquals(updatedUser.getName(), existingUser.getName());
        assertEquals(updatedUser.getEmail(), existingUser.getEmail());
        assertEquals(updatedUser.getRole(), existingUser.getRole());
        assertEquals(existingUser, result);
        verify(userRepository).findById(id);
        verify(userRepository).existsByEmail(updatedUser.getEmail());
        verify(userRepository).save(existingUser);
        verify(auditService).recordEvent(
                eq(organizationId),
                eq(currentUserId),
                eq("UPDATE_USER"),
                eq("Usuário"),
                eq(existingUser.getId()),
                anyString()
        );
    }

    @Test
    void updateUserShouldThrowExceptionWhenEmailExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        User existingUser = createUser("existing@example.com");
        User updatedUser = createUser("taken@example.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(updatedUser.getEmail())).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.updateUser(id, updatedUser));
        assertEquals("Email já está em uso: " + updatedUser.getEmail(), exception.getMessage());
        verify(userRepository).findById(id);
        verify(userRepository).existsByEmail(updatedUser.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordShouldUpdatePasswordWhenCurrentPasswordIsCorrect() {
        // Arrange
        UUID id = UUID.randomUUID();
        User user = createUser("user@example.com");
        user.setPassword("encodedOldPassword");
        String currentPassword = "oldPassword";
        String newPassword = "newPassword123";
        UUID currentUserId = UUID.randomUUID();
        UUID organizationId = user.getOrganization().getId();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(securityUtils.getCurrentUserId()).thenReturn(currentUserId);

        // Act
        User result = userService.changePassword(id, currentPassword, newPassword);

        // Assert
        assertEquals("encodedNewPassword", user.getPassword());
        assertEquals(user, result);
        verify(userRepository).findById(id);
        verify(passwordEncoder).matches(currentPassword, "encodedOldPassword");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
        verify(auditService).recordEvent(
                eq(organizationId),
                eq(currentUserId),
                eq("CHANGE_PASSWORD"),
                eq("Usuário"),
                eq(user.getId()),
                anyString()
        );
    }

    @Test
    void changePasswordShouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
        // Arrange
        UUID id = UUID.randomUUID();
        User user = createUser("user@example.com");
        user.setPassword("encodedPassword");
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword123";

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> userService.changePassword(id, currentPassword, newPassword));
        assertEquals("Senha atual incorreta", exception.getMessage());
        verify(userRepository).findById(id);
        verify(passwordEncoder).matches(currentPassword, "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserShouldRemoveUserWhenExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        User user = createUser("user@example.com");
        UUID currentUserId = UUID.randomUUID();
        UUID organizationId = user.getOrganization().getId();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(securityUtils.getCurrentUserId()).thenReturn(currentUserId);

        // Act
        userService.deleteUser(id);

        // Assert
        verify(userRepository).findById(id);
        verify(userRepository).delete(user);
        verify(auditService).recordEvent(
                eq(organizationId),
                eq(currentUserId),
                eq("DELETE_USER"),
                eq("Usuário"),
                eq(user.getId()),
                anyString()
        );
    }

    private User createUser(String email) {
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setRole(Role.ADMIN);
        user.setOrganization(organization);
        return user;
    }
}