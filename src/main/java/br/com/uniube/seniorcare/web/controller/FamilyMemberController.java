package br.com.uniube.seniorcare.web.controller;

import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import br.com.uniube.seniorcare.domain.enums.Relationship;
import br.com.uniube.seniorcare.service.FamilyMemberService;
import br.com.uniube.seniorcare.web.dto.request.FamilyMemberRequest;
import br.com.uniube.seniorcare.web.dto.response.FamilyMemberResponse;
import br.com.uniube.seniorcare.web.mapper.FamilyMemberMapper;
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
@RequestMapping("/api/family-members")
@RequiredArgsConstructor
@Tag(name = "Familiares", description = "API para gerenciamento de membros da família")
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;
    private final FamilyMemberMapper familyMemberMapper;

    @GetMapping
    @Operation(summary = "Listar todos os vínculos familiares")
    @ApiResponse(responseCode = "200", description = "Operação bem-sucedida")
    public ResponseEntity<List<FamilyMemberResponse>> findAll() {
        List<FamilyMember> familyMembers = familyMemberService.findAll();
        return ResponseEntity.ok(familyMemberMapper.toDtoList(familyMembers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar vínculo familiar por ID")
    @ApiResponse(responseCode = "200", description = "Vínculo encontrado")
    @ApiResponse(responseCode = "404", description = "Vínculo não encontrado")
    public ResponseEntity<FamilyMemberResponse> findById(@PathVariable UUID id) {
        FamilyMember familyMember = familyMemberService.findById(id);
        return ResponseEntity.ok(familyMemberMapper.toDto(familyMember));
    }

    @PostMapping
    @Operation(summary = "Criar novo vínculo familiar")
    @ApiResponse(responseCode = "201", description = "Vínculo criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<FamilyMemberResponse> create(@Valid @RequestBody FamilyMemberRequest request) {
        FamilyMember familyMember = familyMemberMapper.toEntity(request);
        FamilyMember created = familyMemberService.createFamilyMember(familyMember);
        return ResponseEntity.status(HttpStatus.CREATED).body(familyMemberMapper.toDto(created));
    }

    @PutMapping("/{id}/relationship")
    @Operation(summary = "Atualizar tipo de relacionamento do vínculo familiar")
    @ApiResponse(responseCode = "200", description = "Relacionamento atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Vínculo não encontrado")
    public ResponseEntity<FamilyMemberResponse> updateRelationship(
            @PathVariable UUID id,
            @RequestParam Relationship relationship) {
        FamilyMember updated = familyMemberService.updateRelationship(id, relationship);
        return ResponseEntity.ok(familyMemberMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir vínculo familiar")
    @ApiResponse(responseCode = "204", description = "Vínculo excluído com sucesso")
    @ApiResponse(responseCode = "404", description = "Vínculo não encontrado")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        familyMemberService.deleteFamilyMember(id);
        return ResponseEntity.noContent().build();
    }
}
