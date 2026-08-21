package com.assetcompass.portfolio.domain;

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
 * A Rebate.
 */
@Entity
@Table(name = "rebate")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Rebate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "rebate_date", nullable = false)
    private LocalDate rebateDate;

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

    public BrokerAccount getAccount() {
        return this.account;
    }

    public void setAccount(BrokerAccount brokerAccount) {
        this.account = brokerAccount;
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
    }

    public Rebate operation(Operation operation) {
        this.setOperation(operation);
        return this;
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
