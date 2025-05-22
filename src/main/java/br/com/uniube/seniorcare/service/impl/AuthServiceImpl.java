package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.Role;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.OrganizationRepository;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import br.com.uniube.seniorcare.service.AuthService;
import br.com.uniube.seniorcare.web.dto.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email já cadastrado");
        }
        var organization = organizationRepository.findById(UUID.fromString(request.getOrganizationId()))
                .orElseThrow(() -> new BusinessException("Organização não encontrada"));
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .organization(organization)
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .build();
        userRepository.save(user);
    }
}

