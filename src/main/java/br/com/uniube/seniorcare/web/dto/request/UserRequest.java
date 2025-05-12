package br.com.uniube.seniorcare.web.dto.request;

import br.com.uniube.seniorcare.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UserRequest {

    @NotNull
    private UUID organizationId;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank(groups = Create.class)
    private String password;

    @NotNull
    private Role role;

    public interface Create {}
}