package br.com.uniube.seniorcare.web.controller;

import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.service.OrganizationService;
import br.com.uniube.seniorcare.web.dto.request.OrganizationRequest;
import br.com.uniube.seniorcare.web.dto.response.OrganizationResponse;
import br.com.uniube.seniorcare.web.mapper.OrganizationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizações", description = "API para gerenciamento de organizações")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final OrganizationMapper organizationMapper;

    @GetMapping
    @Operation(summary = "Listar todas as organizações",
               description = "Retorna uma lista com todas as organizações cadastradas")
    @ApiResponse(responseCode = "200", description = "Operação bem-sucedida")
    public ResponseEntity<List<OrganizationResponse>> findAll() {
        List<Organization> organizations = organizationService.findAll();
        return ResponseEntity.ok(organizationMapper.toDtoList(organizations));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar organização por ID",
               description = "Retorna os detalhes de uma organização específica")
    @ApiResponse(responseCode = "200", description = "Organização encontrada")
    @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    public ResponseEntity<OrganizationResponse> findById(@PathVariable UUID id) {
        Organization organization = organizationService.findById(id);
        return ResponseEntity.ok(organizationMapper.toDto(organization));
    }

    @PostMapping
    @Operation(summary = "Criar nova organização",
               description = "Cria uma nova organização com os dados fornecidos")
    @ApiResponse(responseCode = "201", description = "Organização criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody OrganizationRequest requestDTO) {
        Organization organization = organizationMapper.toEntity(requestDTO);
        Organization created = organizationService.createOrganization(organization);
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationMapper.toDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar organização",
               description = "Atualiza os dados de uma organização existente")
    @ApiResponse(responseCode = "200", description = "Organização atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    public ResponseEntity<OrganizationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody OrganizationRequest requestDTO) {
        Organization organization = organizationMapper.toEntity(requestDTO);
        Organization updated = organizationService.updateOrganization(id, organization);
        return ResponseEntity.ok(organizationMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir organização",
               description = "Remove uma organização do sistema")
    @ApiResponse(responseCode = "204", description = "Organização excluída com sucesso")
    @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    @ApiResponse(responseCode = "400", description = "Organização não pode ser excluída")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }
}