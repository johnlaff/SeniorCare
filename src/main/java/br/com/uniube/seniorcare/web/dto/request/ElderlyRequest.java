package br.com.uniube.seniorcare.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ElderlyRequest {
    @NotNull(message = "Organização é obrigatória")
    private UUID organizationId;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate birthDate;

    private String emergencyContact;
    private String address;
}

