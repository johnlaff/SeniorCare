package br.com.uniube.seniorcare.web.dto.response;

import br.com.uniube.seniorcare.domain.enums.Relationship;
import lombok.Data;
import java.util.UUID;

@Data
public class FamilyMemberResponse {
    private UUID id;
    private UUID organizationId;
    private UUID userId;
    private UUID elderlyId;
    private Relationship relationship;
}

