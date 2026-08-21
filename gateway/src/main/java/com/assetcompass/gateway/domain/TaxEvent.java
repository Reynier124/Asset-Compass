package com.assetcompass.gateway.domain;

import com.assetcompass.gateway.domain.enumeration.TaxType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A TaxEvent.
 */
@Table("tax_event")
@JsonIgnoreProperties(value = { "new" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TaxEvent implements Serializable, Persistable<UUID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("type")
    private TaxType type;

    @NotNull
    @Column("tax_date")
    private LocalDate taxDate;

    @NotNull
    @Column("amount")
    private BigDecimal amount;

    @NotNull
    @Column("currency")
    private String currency;

    @org.springframework.data.annotation.Transient
    private boolean isPersisted;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "broker" }, allowSetters = true)
    private BrokerAccount account;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "account", "asset", "closesOperation" }, allowSetters = true)
    private Operation operation;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "account", "asset" }, allowSetters = true)
    private IncomeEvent incomeEvent;

    @Column("account_id")
    private UUID accountId;

    @Column("operation_id")
    private UUID operationId;

    @Column("income_event_id")
    private UUID incomeEventId;

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

    @org.springframework.data.annotation.Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public TaxEvent setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public BrokerAccount getAccount() {
        return this.account;
    }

    public void setAccount(BrokerAccount brokerAccount) {
        this.account = brokerAccount;
        this.accountId = brokerAccount != null ? brokerAccount.getId() : null;
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
        this.operationId = operation != null ? operation.getId() : null;
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
        this.incomeEventId = incomeEvent != null ? incomeEvent.getId() : null;
    }

    public TaxEvent incomeEvent(IncomeEvent incomeEvent) {
        this.setIncomeEvent(incomeEvent);
        return this;
    }

    public UUID getAccountId() {
        return this.accountId;
    }

    public void setAccountId(UUID brokerAccount) {
        this.accountId = brokerAccount;
    }

    public UUID getOperationId() {
        return this.operationId;
    }

    public void setOperationId(UUID operation) {
        this.operationId = operation;
    }

    public UUID getIncomeEventId() {
        return this.incomeEventId;
    }

    public void setIncomeEventId(UUID incomeEvent) {
        this.incomeEventId = incomeEvent;
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
