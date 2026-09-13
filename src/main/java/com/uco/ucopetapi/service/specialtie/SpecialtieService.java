package com.uco.ucopetapi.service.specialtie;

import com.uco.ucopetapi.domain.specialtie.SpecialtieDomain;
import com.uco.ucopetapi.repository.specialtie.ISpecialtieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SpecialtieService {

    private final ISpecialtieRepository iSpecialtieRepository;

    public SpecialtieService(ISpecialtieRepository iSpecialtieRepository) {
        this.iSpecialtieRepository = iSpecialtieRepository;
    }

    public List<SpecialtieDomain> getAllSpecialties() {
        return iSpecialtieRepository.findAll();
    }

    public Optional<SpecialtieDomain> findSpecialtieById(UUID id) {
        return iSpecialtieRepository.findById(id);
    }

    public Optional<SpecialtieDomain> findSpecialtieByName(String name) {
        return iSpecialtieRepository.findSpecialtieByName(name);
    }

    @Transactional
    public SpecialtieDomain createNewSpecialtie(SpecialtieDomain specialtie){
        return iSpecialtieRepository.save(specialtie);
    }

    @Transactional
    public Optional<SpecialtieDomain> updateSpecialtie(UUID id, SpecialtieDomain specialtie) {

        Optional<SpecialtieDomain> existingSpecialtie = iSpecialtieRepository.findById(id);

        if (existingSpecialtie.isEmpty()) {
            return Optional.empty();
        }

        SpecialtieDomain updatedSpecialtie = existingSpecialtie.get();

        updatedSpecialtie.setName(specialtie.getName());
        updatedSpecialtie.setDescription(specialtie.getDescription());


        return Optional.of(iSpecialtieRepository.save(updatedSpecialtie));
    }

    @Transactional
    public Optional<SpecialtieDomain> deactivateSpecialtie(UUID id) {

        Optional<SpecialtieDomain> existingSpecialtie = iSpecialtieRepository.findById(id);

        if (existingSpecialtie.isEmpty()) {
            return Optional.empty();
        }

        SpecialtieDomain updatedSpecialtie = existingSpecialtie.get();

        updatedSpecialtie.setActive(false);

        return Optional.of(iSpecialtieRepository.save(updatedSpecialtie));
    }
}
