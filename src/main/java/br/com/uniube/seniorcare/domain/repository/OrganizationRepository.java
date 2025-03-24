package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}
