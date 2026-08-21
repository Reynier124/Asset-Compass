package com.assetcompass.gateway.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Position.
 */
@Table("position")
@JsonIgnoreProperties(value = { "new" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Position implements Serializable, Persistable<UUID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("quantity")
    private BigDecimal quantity;

    @NotNull
    @Column("average_cost")
    private BigDecimal averageCost;

    @NotNull
    @Column("current_value")
    private BigDecimal currentValue;

    @NotNull
    @Column("currency")
    private String currency;

    @NotNull
    @Column("last_synced_at")
    private Instant lastSyncedAt;

    @org.springframework.data.annotation.Transient
    private boolean isPersisted;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "broker" }, allowSetters = true)
    private BrokerAccount account;

    @org.springframework.data.annotation.Transient
    private Asset asset;

    @Column("account_id")
    private UUID accountId;

    @Column("asset_id")
    private UUID assetId;

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

    @org.springframework.data.annotation.Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public Position setIsPersisted() {
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

    public Position account(BrokerAccount brokerAccount) {
        this.setAccount(brokerAccount);
        return this;
    }

    public Asset getAsset() {
        return this.asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
        this.assetId = asset != null ? asset.getId() : null;
    }

    public Position asset(Asset asset) {
        this.setAsset(asset);
        return this;
    }

    public UUID getAccountId() {
        return this.accountId;
    }

    public void setAccountId(UUID brokerAccount) {
        this.accountId = brokerAccount;
    }

    public UUID getAssetId() {
        return this.assetId;
    }

    public void setAssetId(UUID asset) {
        this.assetId = asset;
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
