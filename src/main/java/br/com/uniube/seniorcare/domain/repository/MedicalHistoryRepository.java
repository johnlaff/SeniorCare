package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, UUID> {

    List<MedicalHistory> findByElderlyId(UUID elderlyId);

    @Query("SELECT mh FROM MedicalHistory mh WHERE mh.dateRecorded BETWEEN :startDate AND :endDate")
    List<MedicalHistory> findByDateRecordedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}