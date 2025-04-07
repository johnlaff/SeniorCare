package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MedicationRepository extends JpaRepository<Medication, UUID> {
    List<Medication> findByElderlyId(UUID elderlyId);
}