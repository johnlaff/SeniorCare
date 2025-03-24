package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CaregiverRepository extends JpaRepository<Caregiver, UUID> {
}
