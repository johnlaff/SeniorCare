package br.com.uniube.seniorcare.web.dto.response;

import br.com.uniube.seniorcare.domain.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private UUID id;
    private UUID organizationId;
    private ElderlyBasicResponse elderly;
    private CaregiverBasicResponse caregiver;
    private LocalDateTime dateTime;
    private String description;
    private AppointmentStatus status;
    private LocalDateTime createdAt;

    // DTO aninhado para informações básicas do Idoso
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ElderlyBasicResponse {
        private UUID id;
        private String name;
    }

    // DTO aninhado para informações básicas do Cuidador
    // Presume que você queira o nome do User associado ao Caregiver
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaregiverBasicResponse {
        private UUID id; // ID do Caregiver
        private String name; // Nome do User (cuidador)
        private String specialty;
    }
}