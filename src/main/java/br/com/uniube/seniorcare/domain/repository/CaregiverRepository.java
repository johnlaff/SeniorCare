package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CaregiverRepository extends JpaRepository<Caregiver, UUID> {

    /**
     * Lista todos os cuidadores associados a um idoso específico
     *
     * @param elderlyId ID do idoso
     * @return Lista de cuidadores
     */
    @Query(value = """
            SELECT c.* FROM caregiver c 
            JOIN elderly_caregiver ec ON c.id = ec.caregiver_id 
            WHERE ec.elderly_id = :elderlyId
            """, nativeQuery = true)
    List<Caregiver> findByElderlyId(@Param("elderlyId") UUID elderlyId);

    /**
     * Verifica se já existe um cuidador com o ID de usuário especificado
     *
     * @param userId ID do usuário
     * @return true se existir um cuidador com o userId informado, false caso contrário
     */
    boolean existsByUserId(UUID userId);

    /**
     * Busca cuidadores por especialidade, ignorando maiúsculas/minúsculas
     *
     * @param specialty especialidade a ser buscada
     * @return lista de cuidadores com a especialidade correspondente
     */
    List<Caregiver> findBySpecialtyContainingIgnoreCase(String specialty);

    List<Caregiver> findByOrganizationId(UUID organizationId);
}