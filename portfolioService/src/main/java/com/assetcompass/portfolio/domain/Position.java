package com.assetcompass.portfolio.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Position.
 */
@Entity
@Table(name = "position")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Position implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "quantity", precision = 21, scale = 2, nullable = false)
    private BigDecimal quantity;

    @NotNull
    @Column(name = "average_cost", precision = 21, scale = 2, nullable = false)
    private BigDecimal averageCost;

    @NotNull
    @Column(name = "current_value", precision = 21, scale = 2, nullable = false)
    private BigDecimal currentValue;

    @NotNull
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull
    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "broker" }, allowSetters = true)
    private BrokerAccount account;

    @ManyToOne(optional = false)
    @NotNull
    private Asset asset;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Position id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getQuantity() {
        return this.quantity;
    }

    public Position quantity(BigDecimal quantity) {
        this.setQuantity(quantity);
        return this;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAverageCost() {
        return this.averageCost;
    }

    public Position averageCost(BigDecimal averageCost) {
        this.setAverageCost(averageCost);
        return this;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public BigDecimal getCurrentValue() {
        return this.currentValue;
    }

    public Position currentValue(BigDecimal currentValue) {
        this.setCurrentValue(currentValue);
        return this;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public String getCurrency() {
        return this.currency;
    }

    public Position currency(String currency) {
        this.setCurrency(currency);
        return this;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getLastSyncedAt() {
        return this.lastSyncedAt;
    }

    public Position lastSyncedAt(Instant lastSyncedAt) {
        this.setLastSyncedAt(lastSyncedAt);
        return this;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public BrokerAccount getAccount() {
        return this.account;
    }

    public void setAccount(BrokerAccount brokerAccount) {
        this.account = brokerAccount;
    }

    public Position account(BrokerAccount brokerAccount) {
        this.setAccount(brokerAccount);
        return this;
    }

    public Asset getAsset() {
        return this.asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Position asset(Asset asset) {
        this.setAsset(asset);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        return getId() != null && getId().equals(((Position) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Position{" +
            "id=" + getId() +
            ", quantity=" + getQuantity() +
            ", averageCost=" + getAverageCost() +
            ", currentValue=" + getCurrentValue() +
            ", currency='" + getCurrency() + "'" +
            ", lastSyncedAt='" + getLastSyncedAt() + "'" +
            "}";
    }
}
