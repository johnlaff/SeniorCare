package br.com.uniube.seniorcare.web.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ElderlyResponse {
    private UUID id;
    private UUID organizationId;
    private String name;
    private LocalDate birthDate;
    private String emergencyContact;
    private String address;
}

