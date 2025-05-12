package br.com.uniube.seniorcare.service;

import java.util.UUID;

/**
 * Define o contrato para o serviço de auditoria,
 * responsável por registrar eventos críticos do sistema.
 *
 * Cada evento deve fornecer o contexto completo, incluindo
 * organizationId, userId, nome da entidade afetada, ação executada e descrição.
 *
 * O serviço persiste os eventos na tabela audit_logs, garantindo
 * rastreabilidade e confiabilidade para ações importantes no sistema.
 *
 * Regras de negócio:
 * 1. A organização e usuário devem ser identificados em cada evento
 * 2. A ação e entidade afetada devem ser explicitamente declaradas
 * 3. Cada evento deve ter um timestamp preciso de quando ocorreu
 * 4. Descrições detalhadas devem ser incluídas para facilitar auditorias futuras
 */
public interface AuditService {

    /**
     * Registra um evento de auditoria no sistema, persistindo-o na tabela audit_logs.
     *
     * @param organizationId ID da organização onde o evento ocorreu (não deve ser nulo).
     * @param userId         ID do usuário que realizou a ação (não deve ser nulo).
     * @param action         Ação realizada (ex.: "CREATE_ORGANIZATION"), não deve ser vazia.
     * @param entityName     Nome da entidade afetada (ex.: "Organization", "User"), não deve ser vazia.
     * @param entityId       Opcional: ID do registro afetado; se não houver registro específico, pode ser nulo.
     * @param description    Descrição detalhada do evento (ex.: "Organization created: ACME Ltd.").
     */
    void recordEvent(UUID organizationId, UUID userId, String action,
                     String entityName, UUID entityId, String description);
}