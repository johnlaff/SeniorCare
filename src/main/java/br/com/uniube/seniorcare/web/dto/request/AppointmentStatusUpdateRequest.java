package br.com.uniube.seniorcare.web.dto.request;

import br.com.uniube.seniorcare.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusUpdateRequest {

    @NotNull(message = "O novo status é obrigatório")
    private AppointmentStatus status;
}