package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.FamilyMember;

import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de idosos.
 *
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
     * Cria um novo registro de idoso, aplicando validações de negócio.
     *
     * @param elderly entidade que representa o novo idoso.
     * @return idoso criado.
     */
    Elderly createElderly(Elderly elderly);

    /**
     * Atualiza um idoso existente.
     *
     * @param id identificador do idoso a ser atualizado.
     * @param updatedElderly entidade com os dados atualizados.
     * @return idoso atualizado.
     */
    Elderly updateElderly(UUID id, Elderly updatedElderly);

    /**
     * Exclui um idoso se não houver registros dependentes.
     *
     * @param id identificador do idoso a ser excluído.
     */
    void deleteElderly(UUID id);

    /**
     * Vincula um cuidador a um idoso.
     *
     * @param elderlyId identificador do idoso.
     * @param caregiverId identificador do cuidador.
     * @return idoso atualizado com novo vínculo.
     */
    Elderly assignCaregiver(UUID elderlyId, UUID caregiverId);

    /**
     * Remove o vínculo entre um cuidador e um idoso.
     *
     * @param elderlyId identificador do idoso.
     * @param caregiverId identificador do cuidador.
     * @return idoso atualizado sem o vínculo.
     */
    Elderly removeCaregiver(UUID elderlyId, UUID caregiverId);

    /**
     * Adiciona um membro da família ao idoso.
     *
     * @param elderlyId identificador do idoso.
     * @param userId identificador do usuário que será vinculado como familiar.
     * @param relationship relacionamento com o idoso.
     * @return o objeto FamilyMember criado.
     */
    FamilyMember addFamilyMember(UUID elderlyId, UUID userId, String relationship);

    /**
     * Remove um membro da família do idoso.
     *
     * @param familyMemberId identificador do vínculo familiar a ser removido.
     */
    void removeFamilyMember(UUID familyMemberId);
}