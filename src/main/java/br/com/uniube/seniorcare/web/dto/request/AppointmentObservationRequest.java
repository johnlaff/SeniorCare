package br.com.uniube.seniorcare.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentObservationRequest {

    @NotBlank(message = "A observação não pode ser vazia")
    @Size(max = 1000, message = "A observação não pode exceder 1000 caracteres")
    private String observation;
}