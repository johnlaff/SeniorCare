package br.com.uniube.seniorcare.web.dto.request;

import br.com.uniube.seniorcare.domain.enums.Relationship;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class FamilyMemberRequest {
    @NotNull(message = "Organização é obrigatória")
    private UUID organizationId;

    @NotNull(message = "Usuário é obrigatório")
    private UUID userId;

    @NotNull(message = "Idoso é obrigatório")
    private UUID elderlyId;

    @NotNull(message = "Tipo de relacionamento é obrigatório")
    private Relationship relationship;
}

