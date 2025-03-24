package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ElderlyRepository extends JpaRepository<Elderly, UUID> {
}
