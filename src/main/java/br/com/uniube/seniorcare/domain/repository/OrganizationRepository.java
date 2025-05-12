package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

/**
 * Repositório que gerencia a persistência de Organization.
 */
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    /**
     * Verifica se existe uma organização com determinado nome.
     */
    boolean existsByName(String name);

    /**
     * Verifica se há registros dependentes (exemplo simplificado).
     * Pode ser checando se há usuários, escalas, etc. associados.
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.organization.id = :organizationId")
    boolean hasDependentRecords(UUID organizationId);
}
