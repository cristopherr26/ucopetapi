package com.uco.ucopetapi.domain.receipts;

import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
import com.uco.ucopetapi.dto.receipts.ReceiptStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "receipts")
public class ReceiptDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "receipt_number", nullable = false, unique = true)
    private String receiptNumber;

    @Column(name = "tutor_id", nullable = false)
    private UUID tutorId;

    @Column(name = "pet_id")
    private UUID petId;

    @Column(name = "concept", nullable = false)
    private String concept;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pay_method_id", nullable = false)
    private PayMethodDomain payMethod;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private ReceiptStatus state;

    protected ReceiptDomain() {
        // required by JPA/Hibernate
    }

    private ReceiptDomain(final Builder builder) {
        this.id = builder.id;
        this.receiptNumber = builder.receiptNumber;
        this.tutorId = builder.tutorId;
        this.petId = builder.petId;
        this.concept = builder.concept;
        this.amount = builder.amount;
        this.payMethod = builder.payMethod;
        this.date = builder.date;
        this.state = builder.state;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public UUID getTutorId() {
        return tutorId;
    }

    public void setTutorId(final UUID tutorId) {
        this.tutorId = tutorId;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(final UUID petId) {
        this.petId = petId;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(final String concept) {
        this.concept = concept;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public PayMethodDomain getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(final PayMethodDomain payMethod) {
        this.payMethod = payMethod;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(final LocalDateTime date) {
        this.date = date;
    }

    public ReceiptStatus getState() {
        return state;
    }

    public void setState(final ReceiptStatus state) {
        this.state = state;
    }

    public static final class Builder {
        private UUID id;
        private String receiptNumber;
        private UUID tutorId;
        private UUID petId;
        private String concept;
        private BigDecimal amount;
        private PayMethodDomain payMethod;
        private LocalDateTime date;
        private ReceiptStatus state;

        private Builder() {
        }

        public Builder id(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder receiptNumber(final String receiptNumber) {
            this.receiptNumber = receiptNumber;
            return this;
        }

        public Builder tutorId(final UUID tutorId) {
            this.tutorId = tutorId;
            return this;
        }

        public Builder petId(final UUID petId) {
            this.petId = petId;
            return this;
        }

        public Builder concept(final String concept) {
            this.concept = concept;
            return this;
        }

        public Builder amount(final BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder payMethod(final PayMethodDomain payMethod) {
            this.payMethod = payMethod;
            return this;
        }

        public Builder date(final LocalDateTime date) {
            this.date = date;
            return this;
        }

        public Builder state(final ReceiptStatus state) {
            this.state = state;
            return this;
        }

        public ReceiptDomain build() {
            return new ReceiptDomain(this);
        }
    }
}