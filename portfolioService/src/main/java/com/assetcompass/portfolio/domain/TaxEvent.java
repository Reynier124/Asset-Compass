package com.assetcompass.portfolio.domain;

import com.assetcompass.portfolio.domain.enumeration.TaxType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A TaxEvent.
 */
@Entity
@Table(name = "tax_event")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TaxEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TaxType type;

    @NotNull
    @Column(name = "tax_date", nullable = false)
    private LocalDate taxDate;

    @NotNull
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotNull
    @Column(name = "currency", nullable = false)
    private String currency;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "broker" }, allowSetters = true)
    private BrokerAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "account", "asset", "closesOperation" }, allowSetters = true)
    private Operation operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "account", "asset" }, allowSetters = true)
    private IncomeEvent incomeEvent;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public TaxEvent id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TaxType getType() {
        return this.type;
    }

    public TaxEvent type(TaxType type) {
        this.setType(type);
        return this;
    }

    public void setType(TaxType type) {
        this.type = type;
    }

    public LocalDate getTaxDate() {
        return this.taxDate;
    }

    public TaxEvent taxDate(LocalDate taxDate) {
        this.setTaxDate(taxDate);
        return this;
    }

    public void setTaxDate(LocalDate taxDate) {
        this.taxDate = taxDate;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public TaxEvent amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return this.currency;
    }

    public TaxEvent currency(String currency) {
        this.setCurrency(currency);
        return this;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BrokerAccount getAccount() {
        return this.account;
    }

    public void setAccount(BrokerAccount brokerAccount) {
        this.account = brokerAccount;
    }

    public TaxEvent account(BrokerAccount brokerAccount) {
        this.setAccount(brokerAccount);
        return this;
    }

    public Operation getOperation() {
        return this.operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public TaxEvent operation(Operation operation) {
        this.setOperation(operation);
        return this;
    }

    public IncomeEvent getIncomeEvent() {
        return this.incomeEvent;
    }

    public void setIncomeEvent(IncomeEvent incomeEvent) {
        this.incomeEvent = incomeEvent;
    }

    public TaxEvent incomeEvent(IncomeEvent incomeEvent) {
        this.setIncomeEvent(incomeEvent);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaxEvent)) {
            return false;
        }
        return getId() != null && getId().equals(((TaxEvent) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TaxEvent{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", taxDate='" + getTaxDate() + "'" +
            ", amount=" + getAmount() +
            ", currency='" + getCurrency() + "'" +
            "}";
    }
}
