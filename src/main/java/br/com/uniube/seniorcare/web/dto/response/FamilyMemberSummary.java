package br.com.uniube.seniorcare.web.dto.response;

import br.com.uniube.seniorcare.domain.enums.Relationship;
import lombok.Data;
import java.util.UUID;

@Data
public class FamilyMemberSummary {
    private UUID id;
    private UserSummary user;
    private Relationship relationship;
}

