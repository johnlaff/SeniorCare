package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, UUID> {
}
