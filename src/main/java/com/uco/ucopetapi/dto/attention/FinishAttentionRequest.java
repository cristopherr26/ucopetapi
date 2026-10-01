package com.uco.ucopetapi.dto.attention;

import com.uco.ucopetapi.dto.episode.DischargeType;

public class FinishAttentionRequest {

    private String summary;
    private boolean closeEpisode;
    private DischargeType dischargeType;
    private String dischargeNotes;

    public FinishAttentionRequest() {
    }

    public FinishAttentionRequest(final String summary, final boolean closeEpisode,
                                  final DischargeType dischargeType, final String dischargeNotes) {
        this.summary = summary;
        this.closeEpisode = closeEpisode;
        this.dischargeType = dischargeType;
        this.dischargeNotes = dischargeNotes;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isCloseEpisode() {
        return closeEpisode;
    }

    public void setCloseEpisode(boolean closeEpisode) {
        this.closeEpisode = closeEpisode;
    }

    public DischargeType getDischargeType() {
        return dischargeType;
    }

    public void setDischargeType(DischargeType dischargeType) {
        this.dischargeType = dischargeType;
    }

    public String getDischargeNotes() {
        return dischargeNotes;
    }

    public void setDischargeNotes(String dischargeNotes) {
        this.dischargeNotes = dischargeNotes;
    }
}