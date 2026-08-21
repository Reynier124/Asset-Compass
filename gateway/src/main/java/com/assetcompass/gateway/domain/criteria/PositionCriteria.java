package com.assetcompass.gateway.domain.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.assetcompass.gateway.domain.Position} entity. This class is used
 * in {@link com.assetcompass.gateway.web.rest.PositionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /positions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PositionCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private BigDecimalFilter quantity;

    private BigDecimalFilter averageCost;

    private BigDecimalFilter currentValue;

    private StringFilter currency;

    private InstantFilter lastSyncedAt;

    private UUIDFilter accountId;

    private UUIDFilter assetId;

    private Boolean distinct;

    public PositionCriteria() {}

    public PositionCriteria(PositionCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.quantity = other.optionalQuantity().map(BigDecimalFilter::copy).orElse(null);
        this.averageCost = other.optionalAverageCost().map(BigDecimalFilter::copy).orElse(null);
        this.currentValue = other.optionalCurrentValue().map(BigDecimalFilter::copy).orElse(null);
        this.currency = other.optionalCurrency().map(StringFilter::copy).orElse(null);
        this.lastSyncedAt = other.optionalLastSyncedAt().map(InstantFilter::copy).orElse(null);
        this.accountId = other.optionalAccountId().map(UUIDFilter::copy).orElse(null);
        this.assetId = other.optionalAssetId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public PositionCriteria copy() {
        return new PositionCriteria(this);
    }

    public UUIDFilter getId() {
        return id;
    }

    public Optional<UUIDFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public UUIDFilter id() {
        if (id == null) {
            setId(new UUIDFilter());
        }
        return id;
    }

    public void setId(UUIDFilter id) {
        this.id = id;
    }

    public BigDecimalFilter getQuantity() {
        return quantity;
    }

    public Optional<BigDecimalFilter> optionalQuantity() {
        return Optional.ofNullable(quantity);
    }

    public BigDecimalFilter quantity() {
        if (quantity == null) {
            setQuantity(new BigDecimalFilter());
        }
        return quantity;
    }

    public void setQuantity(BigDecimalFilter quantity) {
        this.quantity = quantity;
    }

    public BigDecimalFilter getAverageCost() {
        return averageCost;
    }

    public Optional<BigDecimalFilter> optionalAverageCost() {
        return Optional.ofNullable(averageCost);
    }

    public BigDecimalFilter averageCost() {
        if (averageCost == null) {
            setAverageCost(new BigDecimalFilter());
        }
        return averageCost;
    }

    public void setAverageCost(BigDecimalFilter averageCost) {
        this.averageCost = averageCost;
    }

    public BigDecimalFilter getCurrentValue() {
        return currentValue;
    }

    public Optional<BigDecimalFilter> optionalCurrentValue() {
        return Optional.ofNullable(currentValue);
    }

    public BigDecimalFilter currentValue() {
        if (currentValue == null) {
            setCurrentValue(new BigDecimalFilter());
        }
        return currentValue;
    }

    public void setCurrentValue(BigDecimalFilter currentValue) {
        this.currentValue = currentValue;
    }

    public StringFilter getCurrency() {
        return currency;
    }

    public Optional<StringFilter> optionalCurrency() {
        return Optional.ofNullable(currency);
    }

    public StringFilter currency() {
        if (currency == null) {
            setCurrency(new StringFilter());
        }
        return currency;
    }

    public void setCurrency(StringFilter currency) {
        this.currency = currency;
    }

    public InstantFilter getLastSyncedAt() {
        return lastSyncedAt;
    }

    public Optional<InstantFilter> optionalLastSyncedAt() {
        return Optional.ofNullable(lastSyncedAt);
    }

    public InstantFilter lastSyncedAt() {
        if (lastSyncedAt == null) {
            setLastSyncedAt(new InstantFilter());
        }
        return lastSyncedAt;
    }

    public void setLastSyncedAt(InstantFilter lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public UUIDFilter getAccountId() {
        return accountId;
    }

    public Optional<UUIDFilter> optionalAccountId() {
        return Optional.ofNullable(accountId);
    }

    public UUIDFilter accountId() {
        if (accountId == null) {
            setAccountId(new UUIDFilter());
        }
        return accountId;
    }

    public void setAccountId(UUIDFilter accountId) {
        this.accountId = accountId;
    }

    public UUIDFilter getAssetId() {
        return assetId;
    }

    public Optional<UUIDFilter> optionalAssetId() {
        return Optional.ofNullable(assetId);
    }

    public UUIDFilter assetId() {
        if (assetId == null) {
            setAssetId(new UUIDFilter());
        }
        return assetId;
    }

    public void setAssetId(UUIDFilter assetId) {
        this.assetId = assetId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PositionCriteria that = (PositionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(quantity, that.quantity) &&
            Objects.equals(averageCost, that.averageCost) &&
            Objects.equals(currentValue, that.currentValue) &&
            Objects.equals(currency, that.currency) &&
            Objects.equals(lastSyncedAt, that.lastSyncedAt) &&
            Objects.equals(accountId, that.accountId) &&
            Objects.equals(assetId, that.assetId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity, averageCost, currentValue, currency, lastSyncedAt, accountId, assetId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PositionCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalQuantity().map(f -> "quantity=" + f + ", ").orElse("") +
            optionalAverageCost().map(f -> "averageCost=" + f + ", ").orElse("") +
            optionalCurrentValue().map(f -> "currentValue=" + f + ", ").orElse("") +
            optionalCurrency().map(f -> "currency=" + f + ", ").orElse("") +
            optionalLastSyncedAt().map(f -> "lastSyncedAt=" + f + ", ").orElse("") +
            optionalAccountId().map(f -> "accountId=" + f + ", ").orElse("") +
            optionalAssetId().map(f -> "assetId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
