package br.com.uniube.seniorcare.web.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ElderlySummary {
    private UUID id;
    private String name;
    private LocalDate birthDate;
}

