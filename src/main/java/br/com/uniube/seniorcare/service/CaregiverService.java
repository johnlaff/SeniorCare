package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.repository.CaregiverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CaregiverService {

    private final CaregiverRepository caregiverRepository;

    public CaregiverService(CaregiverRepository caregiverRepository) {
        this.caregiverRepository = caregiverRepository;
    }

    public List<Caregiver> findAll() {
        return caregiverRepository.findAll();
    }

    public Caregiver findById(UUID id) {
        return caregiverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caregiver not found with id: " + id));
    }

    public Caregiver createCaregiver(Caregiver caregiver) {
        return caregiverRepository.save(caregiver);
    }

    public Caregiver updateCaregiver(UUID id, Caregiver updatedCaregiver) {
        Caregiver caregiver = findById(id);
        caregiver.setSpecialty(updatedCaregiver.getSpecialty());
        caregiver.setUser(updatedCaregiver.getUser());
        return caregiverRepository.save(caregiver);
    }

    public void deleteCaregiver(UUID id) {
        Caregiver caregiver = findById(id);
        caregiverRepository.delete(caregiver);
    }
}
