package br.com.uniube.seniorcare.web.controller;

import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.service.CaregiverService;
import br.com.uniube.seniorcare.service.ElderlyService;
import br.com.uniube.seniorcare.web.dto.request.CaregiverRequest;
import br.com.uniube.seniorcare.web.dto.response.CaregiverResponse;
import br.com.uniube.seniorcare.web.dto.response.ElderlyResponse;
import br.com.uniube.seniorcare.web.mapper.CaregiverMapper;
import br.com.uniube.seniorcare.web.mapper.ElderlyMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/caregivers")
@RequiredArgsConstructor
@Tag(name = "Cuidadores", description = "API para gerenciamento de cuidadores")
public class CaregiverController {

    private final CaregiverService caregiverService;
    private final CaregiverMapper caregiverMapper;

    @GetMapping
    @Operation(summary = "Listar todos os cuidadores")
    @ApiResponse(responseCode = "200", description = "Operação bem-sucedida")
    public ResponseEntity<List<CaregiverResponse>> findAll() {
        List<Caregiver> caregivers = caregiverService.findAll();
        return ResponseEntity.ok(caregiverMapper.toDtoList(caregivers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cuidador por ID")
    @ApiResponse(responseCode = "200", description = "Cuidador encontrado")
    @ApiResponse(responseCode = "404", description = "Cuidador não encontrado")
    public ResponseEntity<CaregiverResponse> findById(@PathVariable UUID id) {
        Caregiver caregiver = caregiverService.findById(id);
        return ResponseEntity.ok(caregiverMapper.toDto(caregiver));
    }

    @PostMapping
    @Operation(summary = "Criar novo cuidador")
    @ApiResponse(responseCode = "201", description = "Cuidador criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<CaregiverResponse> create(@Valid @RequestBody CaregiverRequest request) {
        Caregiver caregiver = caregiverMapper.toEntity(request);
        Caregiver created = caregiverService.createCaregiver(caregiver);
        return ResponseEntity.status(HttpStatus.CREATED).body(caregiverMapper.toDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cuidador")
    @ApiResponse(responseCode = "200", description = "Cuidador atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Cuidador não encontrado")
    public ResponseEntity<CaregiverResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CaregiverRequest request) {
        Caregiver caregiver = caregiverMapper.toEntity(request);
        Caregiver updated = caregiverService.updateCaregiver(id, caregiver);
        return ResponseEntity.ok(caregiverMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cuidador")
    @ApiResponse(responseCode = "204", description = "Cuidador excluído com sucesso")
    @ApiResponse(responseCode = "404", description = "Cuidador não encontrado")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        caregiverService.deleteCaregiver(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/elderly")
    @Operation(summary = "Listar idosos vinculados ao cuidador")
    @ApiResponse(responseCode = "200", description = "Operação bem-sucedida")
    public ResponseEntity<List<ElderlyResponse>> getAssignedElderly(@PathVariable UUID id) {
        return ResponseEntity.ok(caregiverService.getAssignedElderlyEnriched(id));
    }
}

