package com.uco.ucopetapi.dto.sale.sale;

import com.uco.ucopetapi.crosscutting.helpers.DateTimeHelper;
import com.uco.ucopetapi.crosscutting.helpers.ObjectHelper;
import com.uco.ucopetapi.crosscutting.helpers.TextHelper;
import com.uco.ucopetapi.crosscutting.helpers.UUIDHelper;
import com.uco.ucopetapi.domain.sale.sale.SaleOrderDomain;
import com.uco.ucopetapi.dto.sale.enums.SaleOrderState;
import com.uco.ucopetapi.dto.sale.itemSale.ItemSaleDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public final class SaleOrderDTO {
    private UUID id;
    private UUID headquarterId;
    private String orderNumber;
    private LocalDateTime saleDate;
    private UUID clientID;
    private UUID petId;
    private UUID healthPlanId;
    private List<ItemSaleDTO> items;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalTaxes;
    private BigDecimal total;
    private String comment;
    private SaleOrderState state;
    private static SaleOrderDTO DEFAULT = new SaleOrderDTO();

    public static SaleOrderDTO getDefaultObject() {
        return DEFAULT;
    }

    public SaleOrderDTO(){
        this.id = UUIDHelper.getUUIDHelper().getDefault();
        this.headquarterId = UUIDHelper.getUUIDHelper().getDefault();
        this.orderNumber = TextHelper.getDefault();
        this.saleDate = DateTimeHelper.getDefault();
        this.clientID = UUIDHelper.getUUIDHelper().getDefault();
        this.petId = UUIDHelper.getUUIDHelper().getDefault();
        this.healthPlanId = UUIDHelper.getUUIDHelper().getDefault();
        this.items = new ArrayList<>();
        this.subtotal = BigDecimal.ZERO;
        this.totalDiscount = BigDecimal.ZERO;
        this.totalTaxes = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.comment = TextHelper.getDefault();
        this.state = SaleOrderState.SIN_ESTADO;
    }

    public SaleOrderDTO(UUID id) {
        this.id = UUIDHelper.getUUIDHelper().getDefault(id);
        this.headquarterId = UUIDHelper.getUUIDHelper().getDefault();
        this.orderNumber = TextHelper.getDefault();
        this.saleDate = DateTimeHelper.getDefault();
        this.clientID = UUIDHelper.getUUIDHelper().getDefault();
        this.petId = UUIDHelper.getUUIDHelper().getDefault();
        this.healthPlanId = UUIDHelper.getUUIDHelper().getDefault();
        this.subtotal = BigDecimal.ZERO;
        this.totalDiscount = BigDecimal.ZERO;
        this.totalTaxes = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.comment = TextHelper.getDefault();
        this.state = SaleOrderState.SIN_ESTADO;
    }

    public SaleOrderDTO(UUID id, UUID headquarterId, String orderNumber, LocalDateTime saleDate, UUID clientID,
                        UUID petId, UUID healthPlanId, BigDecimal subtotal, BigDecimal totalDiscount, BigDecimal totalTaxes,
                        BigDecimal total, String comment, SaleOrderState state) {
        this.id = UUIDHelper.getUUIDHelper().getDefault(id);
        setHeadquarterId(headquarterId);
        setOrderNumber(orderNumber);
        setSaleDate(saleDate);
        setClientID(clientID);
        setPetId(petId);
        setHealthPlanId(healthPlanId);
        setSubtotal(subtotal);
        setTotalDiscount(totalDiscount);
        setTotalTaxes(totalTaxes);
        setTotal(total);
        setComment(comment);
        setState(state);
    }

    public UUID getId() { return id; }
    public UUID getHeadquarterId() { return headquarterId; }
    public String getOrderNumber() { return orderNumber; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public UUID getClientID() { return clientID; }
    public UUID getPetId() { return petId; }
    public UUID getHealthPlanId() { return healthPlanId; }
    public List<ItemSaleDTO> getItems() {return items;}
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public BigDecimal getTotalTaxes() { return totalTaxes; }
    public BigDecimal getTotal() { return total; }
    public String getComment() { return comment; }
    public SaleOrderState getState() { return state; }

    public void setId(UUID id){
        this.id = UUIDHelper.getUUIDHelper().getDefault(id);
    }
    public void setHeadquarterId(UUID headquarterId){ this.headquarterId = UUIDHelper.getUUIDHelper().getDefault(headquarterId);}
    public void setOrderNumber(String orderNumber){
        this.orderNumber = TextHelper.getDefault(orderNumber);
    }
    public void setSaleDate(LocalDateTime saleDate){
        this.saleDate = DateTimeHelper.getDefault(saleDate);
    }
    public void setClientID(UUID clientID){
        this.clientID = UUIDHelper.getUUIDHelper().getDefault(clientID);
    }
    public void setPetId(UUID petId){
        this.petId = UUIDHelper.getUUIDHelper().getDefault(petId);
    }
    public void setHealthPlanId(UUID healthPlanId){this.healthPlanId = UUIDHelper.getUUIDHelper().getDefault(healthPlanId);}
    public void setItems(List<ItemSaleDTO> items) {this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);}
    public void setSubtotal(BigDecimal subtotal) {this.subtotal = ObjectHelper.getDefault(subtotal, BigDecimal.ZERO);}
    public void setTotalDiscount(BigDecimal totalDiscount) {this.totalDiscount = ObjectHelper.getDefault(totalDiscount, BigDecimal.ZERO);}
    public void setTotalTaxes(BigDecimal totalTaxes) {this.totalTaxes = ObjectHelper.getDefault(totalTaxes, BigDecimal.ZERO);}
    public void setTotal(BigDecimal total) {this.total = ObjectHelper.getDefault(total, BigDecimal.ZERO);}
    public void setComment(String comment){
        this.comment = TextHelper.getDefault(comment);
    }
    public void setState(SaleOrderState state){this.state = ObjectHelper.getDefault(state, SaleOrderState.SIN_ESTADO);}

    public SaleOrderDomain toDomain() {
        if (this.state == SaleOrderState.SIN_ESTADO) {
            throw new IllegalStateException(
                    "No se puede persistir una SaleOrder sin un estado de negocio definido.");
        }

        return new SaleOrderDomain(
                this.id,
                this.headquarterId,
                this.orderNumber,
                this.saleDate,
                this.clientID,
                this.petId,
                this.healthPlanId,
                this.subtotal,
                this.totalDiscount,
                this.totalTaxes,
                this.total,
                this.comment,
                this.state
        );
    }
}
