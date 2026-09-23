package com.uco.ucopetapi.dto.attention;

import com.uco.ucopetapi.dto.vitalSigns.VitalSignsDTO;

import java.util.ArrayList;
import java.util.List;

public class StartAttentionRequest {

    private String reason;
    private String description;
    private List<VitalSignsDTO> vitalSigns = new ArrayList<>();

    public StartAttentionRequest() {
    }

    public StartAttentionRequest(final String reason, final String description,
                                 final List<VitalSignsDTO> vitalSigns) {
        this.reason = reason;
        this.description = description;
        this.vitalSigns = vitalSigns != null ? vitalSigns : new ArrayList<>();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<VitalSignsDTO> getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(List<VitalSignsDTO> vitalSigns) {
        this.vitalSigns = vitalSigns != null ? vitalSigns : new ArrayList<>();
    }
}