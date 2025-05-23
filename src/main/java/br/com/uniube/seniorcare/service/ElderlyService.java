package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import br.com.uniube.seniorcare.web.dto.response.ElderlyResponse;

import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de idosos.
 * <p>
 * Regras de negócio:
 * 1. Cada idoso deve estar vinculado a uma organização
 * 2. O nome e data de nascimento são obrigatórios
 * 3. Registro de eventos de auditoria para todas as operações
 * 4. Verificação de vínculos existentes antes da exclusão
 */
public interface ElderlyService {

    /**
     * Retorna todos os idosos da organização do usuário atual.
     *
     * @return lista de idosos.
     */
    List<Elderly> findAll();

    /**
     * Busca um idoso pelo seu ID, lançando exceção se não encontrado.
     *
     * @param id identificador do idoso.
     * @return idoso encontrado.
     */
    Elderly findById(UUID id);

    /**
     * Retorna todos os cuidadores vinculados a um idoso.
     *
     * @param elderlyId identificador do idoso.
     * @return lista de cuidadores vinculados ao idoso.
     */
    List<Caregiver> getCaregiversByElderlyId(UUID elderlyId);

    /**
     * Retorna todos os familiares vinculados a um idoso.
     *
     * @param elderlyId identificador do idoso.
     * @return lista de familiares vinculados ao idoso.
     */
    List<FamilyMember> getFamilyMembersByElderlyId(UUID elderlyId);

    /**
     * Cria um novo registro de idoso, aplicando validações de negócio.
     *
     * @param elderly entidade que representa o novo idoso.
     * @return idoso criado.
     */
    Elderly createElderly(Elderly elderly);

    /**
     * Atualiza um idoso existente, aplicando validações de negócio.
     *
     * @param id identificador do idoso a ser atualizado.
     * @param updatedElderly entidade com os dados atualizados.
     * @return idoso atualizado.
     */
    Elderly updateElderly(UUID id, Elderly updatedElderly);

    /**
     * Exclui um idoso, verificando vínculos dependentes antes da exclusão.
     *
     * @param id identificador do idoso a ser excluído.
     */
    void deleteElderly(UUID id);

    /**
     * Vincula um cuidador a um idoso.
     *
     * @param elderlyId identificador do idoso.
     * @param caregiverId identificador do cuidador.
     * @return idoso atualizado com o novo vínculo.
     */
    Elderly assignCaregiver(UUID elderlyId, UUID caregiverId);

    /**
     * Remove o vínculo de um cuidador de um idoso.
     *
     * @param elderlyId identificador do idoso.
     * @param caregiverId identificador do cuidador.
     * @return idoso atualizado sem o vínculo.
     */
    Elderly removeCaregiver(UUID elderlyId, UUID caregiverId);

    /**
     * Adiciona um membro da família a um idoso.
     *
     * @param elderlyId identificador do idoso.
     * @param userId identificador do usuário.
     * @param relationship tipo de relacionamento.
     * @return vínculo criado.
     */
    FamilyMember addFamilyMember(UUID elderlyId, UUID userId, String relationship);

    /**
     * Remove um vínculo de membro da família de um idoso.
     *
     * @param familyMemberId identificador do vínculo familiar a ser removido.
     */
    void removeFamilyMember(UUID familyMemberId);

    /**
     * Retorna todos os idosos enriquecidos com vínculos (caregivers/familyMembers).
     *
     * @return lista de ElderlyResponse enriquecidos.
     */
    List<ElderlyResponse> findAllEnriched();

    /**
     * Busca um idoso por ID e retorna enriquecido com vínculos (caregivers/familyMembers).
     *
     * @param id identificador do idoso.
     * @return ElderlyResponse enriquecido.
     */
    ElderlyResponse findEnrichedById(UUID id);
}

