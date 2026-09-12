package com.uco.ucopetapi.domain.sale;

import com.uco.ucopetapi.domain.sale.enums.PaymentMethod;
import com.uco.ucopetapi.domain.sale.enums.SaleOrderState;

import java.time.LocalDateTime;
import java.util.UUID;

public class SaleOrderDomain {
    private UUID id;
    private String orderNumber;
    private LocalDateTime saleDate;
    private Integer subtotal;
    private Integer totalDiscount;
    private Integer totalTaxes;
    private Integer total;
    private PaymentMethod paymentMethod;
    private String comment;
    private SaleOrderState state;

    public SaleOrderDomain(String orderNumber, LocalDateTime saleDate, Integer subtotal, Integer totalDiscount, Integer totalTaxes, Integer total, PaymentMethod paymentMethod, String comment, SaleOrderState state) {
        this.id = UUID.randomUUID();
        setOrderNumber(orderNumber);
        setSaleDate(saleDate);
        setSubtotal(subtotal);
        setTotalDiscount(totalDiscount);
        setTotalTaxes(totalTaxes);
        setTotal(total);
        setPaymentMethod(paymentMethod);
        setComment(comment);
        setState(state);
    }

    public UUID getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public Integer getSubtotal() { return subtotal; }
    public Integer getTotalDiscount() { return totalDiscount; }
    public Integer getTotalTaxes() { return totalTaxes; }
    public Integer getTotal() { return total; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getComment() { return comment; }
    public SaleOrderState getState() { return state; }

    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    public void setSubtotal(Integer subtotal) { this.subtotal = subtotal; }
    public void setTotalDiscount(Integer totalDiscount) { this.totalDiscount = totalDiscount; }
    public void setTotalTaxes(Integer totalTaxes) { this.totalTaxes = totalTaxes; }
    public void setTotal(Integer total) { this.total = total; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setComment(String comment) { this.comment = comment; }
    public void setState(SaleOrderState state) { this.state = state; }
}
