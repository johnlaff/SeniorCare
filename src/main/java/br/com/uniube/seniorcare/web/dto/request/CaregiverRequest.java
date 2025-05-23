package br.com.uniube.seniorcare.web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CaregiverRequest {
    @NotNull(message = "Organização é obrigatória")
    private UUID organizationId;

    @NotNull(message = "Usuário é obrigatório")
    private UUID userId;

    private String specialty;
}

