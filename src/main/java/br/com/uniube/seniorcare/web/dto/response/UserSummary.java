package br.com.uniube.seniorcare.web.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class UserSummary {
    private UUID id;
    private String name;
    private String email;
}

