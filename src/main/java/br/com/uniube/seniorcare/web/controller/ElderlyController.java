package br.com.uniube.seniorcare.web.controller;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.service.ElderlyService;
import br.com.uniube.seniorcare.web.dto.request.ElderlyRequest;
import br.com.uniube.seniorcare.web.dto.response.ElderlyResponse;
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
@RequestMapping("/api/elderly")
@RequiredArgsConstructor
@Tag(name = "Idosos", description = "API para gerenciamento de idosos")
public class ElderlyController {

    private final ElderlyService elderlyService;
    private final ElderlyMapper elderlyMapper;

    @GetMapping
    @Operation(summary = "Listar todos os idosos")
    @ApiResponse(responseCode = "200", description = "Operação bem-sucedida")
    public ResponseEntity<List<ElderlyResponse>> findAll() {
        return ResponseEntity.ok(elderlyService.findAllEnriched());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar idoso por ID")
    @ApiResponse(responseCode = "200", description = "Idoso encontrado")
    @ApiResponse(responseCode = "404", description = "Idoso não encontrado")
    public ResponseEntity<ElderlyResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(elderlyService.findEnrichedById(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo idoso")
    @ApiResponse(responseCode = "201", description = "Idoso criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<ElderlyResponse> create(@Valid @RequestBody ElderlyRequest request) {
        Elderly elderly = elderlyMapper.toEntity(request);
        Elderly created = elderlyService.createElderly(elderly);
        return ResponseEntity.status(HttpStatus.CREATED).body(elderlyService.findEnrichedById(created.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar idoso")
    @ApiResponse(responseCode = "200", description = "Idoso atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Idoso não encontrado")
    public ResponseEntity<ElderlyResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ElderlyRequest request) {
        Elderly elderly = elderlyMapper.toEntity(request);
        Elderly updated = elderlyService.updateElderly(id, elderly);
        return ResponseEntity.ok(elderlyService.findEnrichedById(updated.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir idoso")
    @ApiResponse(responseCode = "204", description = "Idoso excluído com sucesso")
    @ApiResponse(responseCode = "404", description = "Idoso não encontrado")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        elderlyService.deleteElderly(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{elderlyId}/caregivers/{caregiverId}")
    @Operation(summary = "Vincular cuidador a idoso")
    @ApiResponse(responseCode = "200", description = "Cuidador vinculado com sucesso")
    @ApiResponse(responseCode = "404", description = "Idoso ou cuidador não encontrado")
    public ResponseEntity<ElderlyResponse> assignCaregiver(
            @PathVariable UUID elderlyId,
            @PathVariable UUID caregiverId) {
        Elderly elderly = elderlyService.assignCaregiver(elderlyId, caregiverId);
        return ResponseEntity.ok(elderlyService.findEnrichedById(elderly.getId()));
    }

    @DeleteMapping("/{elderlyId}/caregivers/{caregiverId}")
    @Operation(summary = "Remover vínculo de cuidador do idoso")
    @ApiResponse(responseCode = "200", description = "Vínculo removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Idoso ou cuidador não encontrado")
    public ResponseEntity<ElderlyResponse> removeCaregiver(
            @PathVariable UUID elderlyId,
            @PathVariable UUID caregiverId) {
        Elderly elderly = elderlyService.removeCaregiver(elderlyId, caregiverId);
        return ResponseEntity.ok(elderlyService.findEnrichedById(elderly.getId()));
    }
}

