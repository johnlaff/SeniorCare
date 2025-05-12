package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import br.com.uniube.seniorcare.service.AuditService;
import br.com.uniube.seniorcare.service.UserService;
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    // Regex para validação básica de email
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    // Regex para validação de senha (8+ caracteres, letras e números)
    private static final Pattern PASSWORD_REGEX = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    public UserServiceImpl(UserRepository userRepository,
                           AuditService auditService,
                           PasswordEncoder passwordEncoder,
                           SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o id: " + id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(User user) {
        // Validações
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("Email já está em uso: " + user.getEmail());
        }

        // Criptografar senha antes de salvar
        user.setPassword(encodePassword(user.getPassword()));

        User createdUser = userRepository.save(user);

        // Auditoria
        auditService.recordEvent(
                user.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "CREATE_USER",
                "Usuário",
                createdUser.getId(),
                "Usuário criado: " + createdUser.getName()
        );

        return createdUser;
    }

    @Override
    public User updateUser(UUID id, User updatedUser) {
        User user = findById(id);

        // Se o email mudou, valida formato e unicidade
        if (!user.getEmail().equals(updatedUser.getEmail())) {
            validateEmail(updatedUser.getEmail());

            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new BusinessException("Email já está em uso: " + updatedUser.getEmail());
            }
        }

        // Atualiza dados
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setRole(updatedUser.getRole());

        User updated = userRepository.save(user);

        // Auditoria
        auditService.recordEvent(
                updated.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "UPDATE_USER",
                "Usuário",
                updated.getId(),
                "Usuário atualizado: " + updated.getName()
        );

        return updated;
    }

    @Override
    public User changePassword(UUID id, String currentPassword, String newPassword) {
        User user = findById(id);

        // Verifica se senha atual está correta
        if (!passwordMatches(currentPassword, user.getPassword())) {
            throw new BusinessException("Senha atual incorreta");
        }

        validatePassword(newPassword);
        user.setPassword(encodePassword(newPassword));

        User updated = userRepository.save(user);

        // Auditoria
        auditService.recordEvent(
                updated.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "CHANGE_PASSWORD",
                "Usuário",
                updated.getId(),
                "Senha alterada para o usuário: " + updated.getName()
        );

        return updated;
    }

    @Override
    public void deleteUser(UUID id) {
        User user = findById(id);
        userRepository.delete(user);

        // Auditoria
        auditService.recordEvent(
                user.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "DELETE_USER",
                "Usuário",
                user.getId(),
                "Usuário excluído: " + user.getName()
        );
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Email não pode ser nulo ou vazio");
        }

        if (!EMAIL_REGEX.matcher(email).matches()) {
            throw new BusinessException("Formato de email inválido");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException("Senha não pode ser nula ou vazia");
        }

        if (!PASSWORD_REGEX.matcher(password).matches()) {
            throw new BusinessException("A senha deve conter pelo menos 8 caracteres, incluindo letras e números");
        }
    }

    private boolean passwordMatches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}