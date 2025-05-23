package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.web.dto.response.ElderlyResponse;

import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de cuidadores.
 * <p>
 * Regras de negócio:
 * 1. Cada cuidador deve estar vinculado a um usuário existente no sistema
 * 2. Um usuário só pode ser cuidador uma vez
 * 3. O usuário vinculado deve ter o papel (role) CAREGIVER
 * 4. Registro de eventos de auditoria para todas as operações
 * 5. Verificação de vínculos existentes antes da exclusão
 */
public interface CaregiverService {

    /**
     * Retorna todos os cuidadores da organização do usuário atual.
     *
     * @return lista de cuidadores.
     */
    List<Caregiver> findAll();

    /**
     * Busca um cuidador pelo seu ID, lançando exceção se não encontrado.
     *
     * @param id identificador do cuidador.
     * @return cuidador encontrado.
     */
    Caregiver findById(UUID id);

    /**
     * Cria um novo registro de cuidador, aplicando validações de negócio:
     * - Verifica se o usuário existe
     * - Verifica se o usuário já é um cuidador
     * - Verifica se o usuário tem o papel CAREGIVER
     *
     * @param caregiver entidade que representa o novo cuidador.
     * @return cuidador criado.
     */
    Caregiver createCaregiver(Caregiver caregiver);

    /**
     * Atualiza um cuidador existente, permitindo a modificação da especialidade.
     *
     * @param id identificador do cuidador a ser atualizado.
     * @param updatedCaregiver entidade com os dados atualizados.
     * @return cuidador atualizado.
     */
    Caregiver updateCaregiver(UUID id, Caregiver updatedCaregiver);

    /**
     * Exclui um cuidador se não houver vínculos com idosos.
     *
     * @param id identificador do cuidador a ser excluído.
     */
    void deleteCaregiver(UUID id);

    /**
     * Lista os cuidadores por especialidade.
     *
     * @param specialty especialidade a ser pesquisada.
     * @return lista de cuidadores com a especialidade especificada.
     */
    List<Caregiver> findBySpecialty(String specialty);

    /**
     * Lista os idosos atribuídos a um cuidador específico.
     *
     * @param caregiverId identificador do cuidador.
     * @return lista de idosos vinculados ao cuidador.
     */
    List<Elderly> getAssignedElderly(UUID caregiverId);

    /**
     * Retorna todos os idosos vinculados ao cuidador, já enriquecidos com vínculos (caregivers/familyMembers).
     *
     * @param caregiverId identificador do cuidador.
     * @return lista de ElderlyResponse enriquecidos.
     */
    List<ElderlyResponse> getAssignedElderlyEnriched(UUID caregiverId);
}

