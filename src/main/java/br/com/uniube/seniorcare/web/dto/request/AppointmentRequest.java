package br.com.uniube.seniorcare.web.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class AppointmentRequest {

    @NotNull(message = "O ID da organização é obrigatório")
    private UUID organizationId;

    @NotNull(message = "O ID do idoso é obrigatório")
    private UUID elderlyId;

    @NotNull(message = "O ID do cuidador é obrigatório")
    private UUID caregiverId;

    @NotNull(message = "A data e hora do agendamento são obrigatórias")
    @Future(message = "A data e hora do agendamento devem ser futuras")
    private LocalDateTime dateTime;

    @Size(max = 1000, message = "A descrição não pode exceder 1000 caracteres")
    private String description;
}