package com.assetcompass.gateway.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link com.assetcompass.gateway.domain.Position} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PositionDTO implements Serializable {

    @NotNull
    private UUID id;

    @NotNull
    private BigDecimal quantity;

    @NotNull
    private BigDecimal averageCost;

    @NotNull
    private BigDecimal currentValue;

    @NotNull
    private String currency;

    @NotNull
    private Instant lastSyncedAt;

    @NotNull
    private BrokerAccountDTO account;

    @NotNull
    private AssetDTO asset;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public BrokerAccountDTO getAccount() {
        return account;
    }

    public void setAccount(BrokerAccountDTO account) {
        this.account = account;
    }

    public AssetDTO getAsset() {
        return asset;
    }

    public void setAsset(AssetDTO asset) {
        this.asset = asset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PositionDTO)) {
            return false;
        }

        PositionDTO positionDTO = (PositionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, positionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PositionDTO{" +
            "id='" + getId() + "'" +
            ", quantity=" + getQuantity() +
            ", averageCost=" + getAverageCost() +
            ", currentValue=" + getCurrentValue() +
            ", currency='" + getCurrency() + "'" +
            ", lastSyncedAt='" + getLastSyncedAt() + "'" +
            ", account=" + getAccount() +
            ", asset=" + getAsset() +
            "}";
    }
}
