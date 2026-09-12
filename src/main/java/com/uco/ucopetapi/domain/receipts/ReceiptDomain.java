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

    public ReceiptDomain(final UUID id, final String receiptNumber, final UUID tutorId, final UUID petId,
                         final String concept, final BigDecimal amount, final PayMethodDomain payMethod,
                         final LocalDateTime date, final ReceiptStatus state) {
        this.id = id;
        this.receiptNumber = receiptNumber;
        this.tutorId = tutorId;
        this.petId = petId;
        this.concept = concept;
        this.amount = amount;
        this.payMethod = payMethod;
        this.date = date;
        this.state = state;
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
}
