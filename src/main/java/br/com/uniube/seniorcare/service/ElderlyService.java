package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.repository.ElderlyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ElderlyService {

    private final ElderlyRepository elderlyRepository;

    public ElderlyService(ElderlyRepository elderlyRepository) {
        this.elderlyRepository = elderlyRepository;
    }

    public List<Elderly> findAll() {
        return elderlyRepository.findAll();
    }

    public Elderly findById(UUID id) {
        return elderlyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Elderly not found with id: " + id));
    }

    public Elderly createElderly(Elderly elderly) {
        return elderlyRepository.save(elderly);
    }

    public Elderly updateElderly(UUID id, Elderly updatedElderly) {
        Elderly elderly = findById(id);
        elderly.setName(updatedElderly.getName());
        elderly.setBirthDate(updatedElderly.getBirthDate());
        elderly.setEmergencyContact(updatedElderly.getEmergencyContact());
        elderly.setAddress(updatedElderly.getAddress());
        return elderlyRepository.save(elderly);
    }

    public void deleteElderly(UUID id) {
        Elderly elderly = findById(id);
        elderlyRepository.delete(elderly);
    }
}
