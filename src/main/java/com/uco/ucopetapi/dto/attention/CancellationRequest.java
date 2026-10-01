package com.uco.ucopetapi.dto.attention;

public class CancellationRequest {

    private String reason;

    public CancellationRequest() {
    }

    public CancellationRequest(final String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

