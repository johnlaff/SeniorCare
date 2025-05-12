package br.com.uniube.seniorcare.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {

    @NotBlank(message = "O nome da organização é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String name;

    @NotBlank(message = "O domínio da organização é obrigatório")
    @Pattern(regexp = "^[a-zA-Z0-9-]{3,100}$",
            message = "O domínio deve conter de 3 a 100 caracteres alfanuméricos ou hífens")
    private String domain;
}