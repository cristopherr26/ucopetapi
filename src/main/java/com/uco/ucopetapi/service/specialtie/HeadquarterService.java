package com.uco.ucopetapi.service.specialtie;


import com.uco.ucopetapi.domain.specialtie.HeadquarterDomain;
import com.uco.ucopetapi.repository.specialtie.HeadquarterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HeadquarterService {

    private final HeadquarterRepository headquarterRepository;

    public HeadquarterService(HeadquarterRepository headquarterRepository) {
        this.headquarterRepository = headquarterRepository;
    }

    public List<HeadquarterDomain> get() {
        return headquarterRepository.findAll();
    }

    public Optional<HeadquarterDomain> findById(UUID id) {
        return headquarterRepository.findById(id);
    }

    public HeadquarterDomain save(HeadquarterDomain headquarter) {
        return headquarterRepository.save(headquarter);
    }

    public void delete(UUID id) {
        headquarterRepository.deleteById(id);
    }

    public Optional<HeadquarterDomain> deactivate(UUID id) {
        return headquarterRepository.findById(id).map(existingHeadquarter -> {
            existingHeadquarter.setIsActive(false);
            return headquarterRepository.save(existingHeadquarter);
        });
    }
}
