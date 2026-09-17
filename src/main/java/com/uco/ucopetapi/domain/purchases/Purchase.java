package com.uco.ucopetapi.domain.purchases;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID supplierId;

    @Column(nullable = false, unique = true)
    private String purchaseNumber;

    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal totalTaxes;

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseStatus status;

    private UUID expenseId;

    @Column(nullable = false)
    private UUID headquarterId;

    @Column(nullable = false)
    private boolean hasDiscount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private UUID createdByPersonId;

    @Column(nullable = false)
    private UUID updatedByPersonId;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseItem> items = new ArrayList<>();

    /**
     * Constructor vacío requerido por JPA/Hibernate para poder
     * instanciar la entidad vía reflexión al recuperarla de la base
     * de datos. No debe usarse directamente desde el código de
     * negocio — para eso está Purchase.builder().
     */
    public Purchase() {
        // Intencionalmente vacío: JPA lo exige.
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public String getPurchaseNumber() {
        return purchaseNumber;
    }

    public void setPurchaseNumber(String purchaseNumber) {
        this.purchaseNumber = purchaseNumber;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotalTaxes() {
        return totalTaxes;
    }

    public void setTotalTaxes(BigDecimal totalTaxes) {
        this.totalTaxes = totalTaxes;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseStatus status) {
        this.status = status;
    }

    public UUID getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(UUID expenseId) {
        this.expenseId = expenseId;
    }

    public UUID getHeadquarterId() {
        return headquarterId;
    }

    public void setHeadquarterId(UUID headquarterId) {
        this.headquarterId = headquarterId;
    }

    public boolean isHasDiscount() {
        return hasDiscount;
    }

    public void setHasDiscount(boolean hasDiscount) {
        this.hasDiscount = hasDiscount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getCreatedByPersonId() {
        return createdByPersonId;
    }

    public void setCreatedByPersonId(UUID createdByPersonId) {
        this.createdByPersonId = createdByPersonId;
    }

    public UUID getUpdatedByPersonId() {
        return updatedByPersonId;
    }

    public void setUpdatedByPersonId(UUID updatedByPersonId) {
        this.updatedByPersonId = updatedByPersonId;
    }

    public List<PurchaseItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(PurchaseItem item) {
        item.setPurchase(this);
        this.items.add(item);
    }

    public static class Builder {

        private UUID id;
        private UUID supplierId;
        private String purchaseNumber;
        private LocalDateTime purchaseDate;
        private BigDecimal subtotal;
        private BigDecimal totalTaxes;
        private BigDecimal total;
        private PurchaseStatus status;
        private UUID expenseId;
        private UUID headquarterId;
        private boolean hasDiscount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private UUID createdByPersonId;
        private UUID updatedByPersonId;
        private List<PurchaseItem> items;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder supplierId(UUID supplierId) {
            this.supplierId = supplierId;
            return this;
        }

        public Builder purchaseNumber(String purchaseNumber) {
            this.purchaseNumber = purchaseNumber;
            return this;
        }

        public Builder purchaseDate(LocalDateTime purchaseDate) {
            this.purchaseDate = purchaseDate;
            return this;
        }

        public Builder subtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public Builder totalTaxes(BigDecimal totalTaxes) {
            this.totalTaxes = totalTaxes;
            return this;
        }

        public Builder total(BigDecimal total) {
            this.total = total;
            return this;
        }

        public Builder status(PurchaseStatus status) {
            this.status = status;
            return this;
        }

        public Builder expenseId(UUID expenseId) {
            this.expenseId = expenseId;
            return this;
        }

        public Builder headquarterId(UUID headquarterId) {
            this.headquarterId = headquarterId;
            return this;
        }

        public Builder hasDiscount(boolean hasDiscount) {
            this.hasDiscount = hasDiscount;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder createdByPersonId(UUID createdByPersonId) {
            this.createdByPersonId = createdByPersonId;
            return this;
        }

        public Builder updatedByPersonId(UUID updatedByPersonId) {
            this.updatedByPersonId = updatedByPersonId;
            return this;
        }

        public Builder items(List<PurchaseItem> items) {
            this.items = items;
            return this;
        }

        public Purchase build() {
            Purchase purchase = new Purchase();
            purchase.setId(id);
            purchase.setSupplierId(supplierId);
            purchase.setPurchaseNumber(purchaseNumber);
            purchase.setPurchaseDate(purchaseDate);
            purchase.setSubtotal(subtotal);
            purchase.setTotalTaxes(totalTaxes);
            purchase.setTotal(total);
            purchase.setStatus(status);
            purchase.setExpenseId(expenseId);
            purchase.setHeadquarterId(headquarterId);
            purchase.setHasDiscount(hasDiscount);
            purchase.setCreatedAt(createdAt);
            purchase.setUpdatedAt(updatedAt);
            purchase.setCreatedByPersonId(createdByPersonId);
            purchase.setUpdatedByPersonId(updatedByPersonId);
            if (items != null) {
                for (PurchaseItem item : items) {
                    purchase.addItem(item);
                }
            }
            return purchase;
        }
    }
}
