package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.MedicalHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de histórico médico dos idosos.
 *
 * Regras de negócio:
 * 1. Cada registro deve estar vinculado a um idoso e uma organização
 * 2. A data de registro não pode ser futura
 * 3. Registro de eventos de auditoria para todas as operações
 */
public interface MedicalHistoryService {

    /**
     * Retorna todos os registros de histórico médico da organização do usuário atual.
     *
     * @return lista de registros de histórico médico.
     */
    List<MedicalHistory> findAll();

    /**
     * Busca um registro de histórico médico pelo seu ID, lançando exceção se não encontrado.
     *
     * @param id identificador do registro.
     * @return registro encontrado.
     */
    MedicalHistory findById(UUID id);

    /**
     * Lista registros de histórico médico por idoso.
     *
     * @param elderlyId identificador do idoso.
     * @return lista de registros de histórico médico do idoso.
     */
    List<MedicalHistory> findByElderly(UUID elderlyId);

    /**
     * Lista registros de histórico médico por período.
     *
     * @param startDate data inicial do período.
     * @param endDate data final do período.
     * @return lista de registros no período.
     */
    List<MedicalHistory> findByPeriod(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Cria um novo registro de histórico médico, aplicando validações de negócio.
     *
     * @param medicalHistory entidade que representa o novo registro.
     * @return registro criado.
     */
    MedicalHistory createMedicalHistory(MedicalHistory medicalHistory);

    /**
     * Atualiza um registro de histórico médico existente.
     *
     * @param id identificador do registro a ser atualizado.
     * @param updatedMedicalHistory entidade com os dados atualizados.
     * @return registro atualizado.
     */
    MedicalHistory updateMedicalHistory(UUID id, MedicalHistory updatedMedicalHistory);

    /**
     * Exclui um registro de histórico médico.
     *
     * @param id identificador do registro a ser excluído.
     */
    void deleteMedicalHistory(UUID id);
}