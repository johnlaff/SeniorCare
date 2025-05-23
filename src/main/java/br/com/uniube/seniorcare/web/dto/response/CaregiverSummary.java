package br.com.uniube.seniorcare.web.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class CaregiverSummary {
    private UUID id;
    private UserSummary user;
    private String specialty;
}

