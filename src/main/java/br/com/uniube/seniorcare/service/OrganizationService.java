package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Organization;
import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de organizações.
 *
 * Regras de negócio:
 * 1. Validação de unicidade do nome
 * 2. Validação de formato de domínio
 * 3. Registro de eventos de auditoria
 * 4. Verificação de registros dependentes antes da exclusão
 */
public interface OrganizationService {

    /**
     * Retorna todas as organizações.
     *
     * @return lista de organizações.
     */
    List<Organization> findAll();

    /**
     * Busca uma organização pelo seu ID, lançando exceção se não encontrada.
     *
     * @param id identificador da organização.
     * @return organização encontrada.
     */
    Organization findById(UUID id);

    /**
     * Cria uma nova organização, aplicando validações de negócio:
     * - Verifica se já existe outra com o mesmo nome;
     * - Valida o formato do domínio;
     * - Registra criação na auditoria.
     *
     * @param organization entidade que representa a nova organização.
     * @return organização criada.
     */
    Organization createOrganization(Organization organization);

    /**
     * Atualiza uma organização existente, aplicando regras de negócio:
     * - Se o nome for alterado, verifica unicidade;
     * - Se o domínio for alterado, valida formato;
     * - Registra atualização na auditoria.
     *
     * @param id identificador da organização a ser atualizada.
     * @param updatedOrganization entidade com os dados atualizados.
     * @return organização atualizada.
     */
    Organization updateOrganization(UUID id, Organization updatedOrganization);

    /**
     * Exclui uma organização se não houver registros dependentes,
     * registrando a ação na auditoria.
     *
     * @param id identificador da organização a ser excluída.
     */
    void deleteOrganization(UUID id);
}