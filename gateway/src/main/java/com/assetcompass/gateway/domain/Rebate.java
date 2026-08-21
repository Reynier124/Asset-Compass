package com.assetcompass.gateway.domain;

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
 * A Rebate.
 */
@Table("rebate")
@JsonIgnoreProperties(value = { "new" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Rebate implements Serializable, Persistable<UUID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("rebate_date")
    private LocalDate rebateDate;

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

    @Column("account_id")
    private UUID accountId;

    @Column("operation_id")
    private UUID operationId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Rebate id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getRebateDate() {
        return this.rebateDate;
    }

    public Rebate rebateDate(LocalDate rebateDate) {
        this.setRebateDate(rebateDate);
        return this;
    }

    public void setRebateDate(LocalDate rebateDate) {
        this.rebateDate = rebateDate;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Rebate amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return this.currency;
    }

    public Rebate currency(String currency) {
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

    public Rebate setIsPersisted() {
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

    public Rebate account(BrokerAccount brokerAccount) {
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

    public Rebate operation(Operation operation) {
        this.setOperation(operation);
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

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Rebate)) {
            return false;
        }
        return getId() != null && getId().equals(((Rebate) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Rebate{" +
            "id=" + getId() +
            ", rebateDate='" + getRebateDate() + "'" +
            ", amount=" + getAmount() +
            ", currency='" + getCurrency() + "'" +
            "}";
    }
}
