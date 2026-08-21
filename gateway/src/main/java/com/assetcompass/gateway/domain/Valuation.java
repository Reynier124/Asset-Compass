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
 * A Valuation.
 */
@Table("valuation")
@JsonIgnoreProperties(value = { "new" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Valuation implements Serializable, Persistable<UUID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("snapshot_date")
    private LocalDate snapshotDate;

    @NotNull
    @Column("total_value")
    private BigDecimal totalValue;

    @NotNull
    @Column("currency")
    private String currency;

    @org.springframework.data.annotation.Transient
    private boolean isPersisted;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "broker" }, allowSetters = true)
    private BrokerAccount account;

    @Column("account_id")
    private UUID accountId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Valuation id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getSnapshotDate() {
        return this.snapshotDate;
    }

    public Valuation snapshotDate(LocalDate snapshotDate) {
        this.setSnapshotDate(snapshotDate);
        return this;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public BigDecimal getTotalValue() {
        return this.totalValue;
    }

    public Valuation totalValue(BigDecimal totalValue) {
        this.setTotalValue(totalValue);
        return this;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public String getCurrency() {
        return this.currency;
    }

    public Valuation currency(String currency) {
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

    public Valuation setIsPersisted() {
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

    public Valuation account(BrokerAccount brokerAccount) {
        this.setAccount(brokerAccount);
        return this;
    }

    public UUID getAccountId() {
        return this.accountId;
    }

    public void setAccountId(UUID brokerAccount) {
        this.accountId = brokerAccount;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Valuation)) {
            return false;
        }
        return getId() != null && getId().equals(((Valuation) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Valuation{" +
            "id=" + getId() +
            ", snapshotDate='" + getSnapshotDate() + "'" +
            ", totalValue=" + getTotalValue() +
            ", currency='" + getCurrency() + "'" +
            "}";
    }
}
