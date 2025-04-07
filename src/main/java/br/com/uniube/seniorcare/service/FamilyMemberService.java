package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import br.com.uniube.seniorcare.domain.enums.Relationship;

import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de membros familiares dos idosos.
 *
 * Regras de negócio:
 * 1. Cada membro da família é um usuário com papel FAMILY vinculado a um idoso
 * 2. Um usuário pode ser vinculado a múltiplos idosos
 * 3. Cada vínculo deve especificar o tipo de relacionamento (FILHO, NETO, SOBRINHO, OUTRO)
 * 4. Registro de eventos de auditoria para todas as operações
 */
public interface FamilyMemberService {

    /**
     * Retorna todos os membros da família da organização do usuário atual.
     *
     * @return lista de membros da família.
     */
    List<FamilyMember> findAll();

    /**
     * Busca um membro da família pelo seu ID, lançando exceção se não encontrado.
     *
     * @param id identificador do membro da família.
     * @return membro da família encontrado.
     */
    FamilyMember findById(UUID id);

    /**
     * Lista membros da família por idoso.
     *
     * @param elderlyId identificador do idoso.
     * @return lista de membros da família do idoso.
     */
    List<FamilyMember> findByElderly(UUID elderlyId);

    /**
     * Lista membros da família por usuário.
     *
     * @param userId identificador do usuário.
     * @return lista de vínculos de membro da família do usuário.
     */
    List<FamilyMember> findByUser(UUID userId);

    /**
     * Cria um novo vínculo de membro da família, aplicando validações de negócio.
     *
     * @param familyMember entidade que representa o novo vínculo.
     * @return vínculo criado.
     */
    FamilyMember createFamilyMember(FamilyMember familyMember);

    /**
     * Vincula um usuário como membro da família de um idoso.
     *
     * @param userId identificador do usuário.
     * @param elderlyId identificador do idoso.
     * @param relationship tipo de relacionamento.
     * @return vínculo criado.
     */
    FamilyMember addFamilyMemberToElderly(UUID userId, UUID elderlyId, Relationship relationship);

    /**
     * Atualiza um vínculo de membro da família existente.
     *
     * @param id identificador do vínculo a ser atualizado.
     * @param relationship novo tipo de relacionamento.
     * @return vínculo atualizado.
     */
    FamilyMember updateRelationship(UUID id, Relationship relationship);

    /**
     * Exclui um vínculo de membro da família.
     *
     * @param id identificador do vínculo a ser excluído.
     */
    void deleteFamilyMember(UUID id);
}