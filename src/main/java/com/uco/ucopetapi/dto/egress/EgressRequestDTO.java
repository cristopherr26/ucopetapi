package com.uco.ucopetapi.dto.egress;

public class EgressRequestDTO {

    private String date;
    private String provider;
    private String payMethod;
    private String purchaseOrder;
    private String concept;
    private Float total;

    public EgressRequestDTO() {
    }

    public EgressRequestDTO(String date, String provider, String payMethod,
                            String purchaseOrder, String concept, Float total) {
        this.date = date;
        this.provider = provider;
        this.payMethod = payMethod;
        this.purchaseOrder = purchaseOrder;
        this.concept = concept;
        this.total = total;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(String purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }
}
