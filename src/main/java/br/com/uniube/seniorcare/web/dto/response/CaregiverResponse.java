package br.com.uniube.seniorcare.web.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class CaregiverResponse {
    private UUID id;
    private UUID organizationId;
    private UUID userId;
    private String specialty;
}

