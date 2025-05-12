package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ElderlyRepository extends JpaRepository<Elderly, UUID> {

    /**
     * Verifica se o idoso possui registros dependentes em outras tabelas.
     *
     * @param elderlyId ID do idoso
     * @return true se existirem registros dependentes, false caso contrário
     */
    @Query(value = """
            SELECT CASE 
                WHEN COUNT(f) > 0 THEN true 
                WHEN COUNT(a) > 0 THEN true 
                WHEN COUNT(m) > 0 THEN true 
                WHEN COUNT(med) > 0 THEN true 
                WHEN COUNT(d) > 0 THEN true 
                WHEN COUNT(ec) > 0 THEN true 
                ELSE false 
            END FROM family_members f 
            LEFT JOIN appointments a ON a.elderly_id = :elderlyId 
            LEFT JOIN medical_history m ON m.elderly_id = :elderlyId 
            LEFT JOIN medications med ON med.elderly_id = :elderlyId 
            LEFT JOIN documents d ON d.elderly_id = :elderlyId 
            LEFT JOIN elderly_caregiver ec ON ec.elderly_id = :elderlyId 
            WHERE f.elderly_id = :elderlyId 
            LIMIT 1
            """, nativeQuery = true)
    boolean hasDependentRecords(@Param("elderlyId") UUID elderlyId);

    /**
     * Verifica se existe relação entre um idoso e um cuidador
     *
     * @param elderlyId ID do idoso
     * @param caregiverId ID do cuidador
     * @return true se existir a relação, false caso contrário
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM elderly_caregiver " +
           "WHERE elderly_id = :elderlyId AND caregiver_id = :caregiverId",
           nativeQuery = true)
    boolean hasCaregiver(@Param("elderlyId") UUID elderlyId, @Param("caregiverId") UUID caregiverId);

    /**
     * Atribui um cuidador a um idoso
     *
     * @param elderlyId ID do idoso
     * @param caregiverId ID do cuidador
     */
    @Modifying
    @Query(value = """
            INSERT INTO elderly_caregiver (id, organization_id, elderly_id, caregiver_id) 
            SELECT gen_random_uuid(), e.organization_id, :elderlyId, :caregiverId 
            FROM elderly e WHERE e.id = :elderlyId
            """, nativeQuery = true)
    void assignCaregiver(@Param("elderlyId") UUID elderlyId, @Param("caregiverId") UUID caregiverId);

    /**
     * Remove a relação entre um idoso e um cuidador
     *
     * @param elderlyId ID do idoso
     * @param caregiverId ID do cuidador
     */
    @Modifying
    @Query(value = "DELETE FROM elderly_caregiver " +
           "WHERE elderly_id = :elderlyId AND caregiver_id = :caregiverId",
           nativeQuery = true)
    void removeCaregiver(@Param("elderlyId") UUID elderlyId, @Param("caregiverId") UUID caregiverId);

    /**
     * Lista todos os idosos associados a um cuidador específico
     *
     * @param caregiverId ID do cuidador
     * @return Lista de idosos
     */
    @Query(value = """
            SELECT e.* FROM elderly e 
            JOIN elderly_caregiver ec ON e.id = ec.elderly_id 
            WHERE ec.caregiver_id = :caregiverId
            """, nativeQuery = true)
    List<Elderly> findByCaregiverId(@Param("caregiverId") UUID caregiverId);

    @Query(value = """
        SELECT u.* FROM users u 
        JOIN caregiver c ON u.id = c.user_id
        JOIN elderly_caregiver ec ON c.id = ec.caregiver_id 
        WHERE ec.elderly_id = :elderlyId
        """, nativeQuery = true)
    List<User> findCaregiversByElderlyId(@Param("elderlyId") UUID elderlyId);
}