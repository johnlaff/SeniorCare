package br.com.uniube.seniorcare.web.dto.response;

import br.com.uniube.seniorcare.domain.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}