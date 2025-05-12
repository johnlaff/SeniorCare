package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Medication;

import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de medicamentos dos idosos.
 *
 * Regras de negócio:
 * 1. Cada medicamento deve estar vinculado a um idoso e uma organização
 * 2. Validações de nome, dosagem e frequência
 * 3. Medicamento deve pertencer à organização do usuário atual
 * 4. Registro de eventos de auditoria para todas as operações
 */
public interface MedicationService {

    /**
     * Retorna todos os medicamentos da organização do usuário atual.
     *
     * @return lista de medicamentos.
     */
    List<Medication> findAll();

    /**
     * Busca um medicamento pelo seu ID, lançando exceção se não encontrado.
     *
     * @param id identificador do medicamento.
     * @return medicamento encontrado.
     */
    Medication findById(UUID id);

    /**
     * Lista medicamentos por idoso.
     *
     * @param elderlyId identificador do idoso.
     * @return lista de medicamentos do idoso.
     */
    List<Medication> findByElderly(UUID elderlyId);

    /**
     * Cria um novo medicamento, aplicando validações de negócio.
     *
     * @param medication entidade que representa o novo medicamento.
     * @return medicamento criado.
     */
    Medication createMedication(Medication medication);

    /**
     * Atualiza um medicamento existente.
     *
     * @param id identificador do medicamento a ser atualizado.
     * @param updatedMedication entidade com os dados atualizados.
     * @return medicamento atualizado.
     */
    Medication updateMedication(UUID id, Medication updatedMedication);

    /**
     * Exclui um medicamento.
     *
     * @param id identificador do medicamento a ser excluído.
     */
    void deleteMedication(UUID id);
}