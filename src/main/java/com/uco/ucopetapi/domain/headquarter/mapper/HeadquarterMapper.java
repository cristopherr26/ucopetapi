
package com.uco.ucopetapi.domain.headquarter.mapper;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.dto.headquarter.HeadquarterDTO;
import org.springframework.stereotype.Component;

@Component
public class HeadquarterMapper {

    public HeadquarterDTO toDTO(HeadquarterDomain domain) {
        if (domain == null) return null;

        return new HeadquarterDTO(
                domain.getId(),
                domain.getName(),
                domain.getAddress(),
                domain.getIsActive()
        );
    }

    public HeadquarterDomain toDomain(HeadquarterDTO dto) {
        if (dto == null) return null;

        return new HeadquarterDomain(
                dto.getId(),
                dto.getName(),
                dto.getAddress(),
                dto.getIsActive()
        );
    }
}