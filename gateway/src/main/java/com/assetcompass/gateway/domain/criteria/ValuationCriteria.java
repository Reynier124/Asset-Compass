package com.assetcompass.gateway.domain.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.assetcompass.gateway.domain.Valuation} entity. This class is used
 * in {@link com.assetcompass.gateway.web.rest.ValuationResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /valuations?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ValuationCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private LocalDateFilter snapshotDate;

    private BigDecimalFilter totalValue;

    private StringFilter currency;

    private UUIDFilter accountId;

    private Boolean distinct;

    public ValuationCriteria() {}

    public ValuationCriteria(ValuationCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.snapshotDate = other.optionalSnapshotDate().map(LocalDateFilter::copy).orElse(null);
        this.totalValue = other.optionalTotalValue().map(BigDecimalFilter::copy).orElse(null);
        this.currency = other.optionalCurrency().map(StringFilter::copy).orElse(null);
        this.accountId = other.optionalAccountId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ValuationCriteria copy() {
        return new ValuationCriteria(this);
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

    public LocalDateFilter getSnapshotDate() {
        return snapshotDate;
    }

    public Optional<LocalDateFilter> optionalSnapshotDate() {
        return Optional.ofNullable(snapshotDate);
    }

    public LocalDateFilter snapshotDate() {
        if (snapshotDate == null) {
            setSnapshotDate(new LocalDateFilter());
        }
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDateFilter snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public BigDecimalFilter getTotalValue() {
        return totalValue;
    }

    public Optional<BigDecimalFilter> optionalTotalValue() {
        return Optional.ofNullable(totalValue);
    }

    public BigDecimalFilter totalValue() {
        if (totalValue == null) {
            setTotalValue(new BigDecimalFilter());
        }
        return totalValue;
    }

    public void setTotalValue(BigDecimalFilter totalValue) {
        this.totalValue = totalValue;
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
        final ValuationCriteria that = (ValuationCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(snapshotDate, that.snapshotDate) &&
            Objects.equals(totalValue, that.totalValue) &&
            Objects.equals(currency, that.currency) &&
            Objects.equals(accountId, that.accountId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, snapshotDate, totalValue, currency, accountId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ValuationCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalSnapshotDate().map(f -> "snapshotDate=" + f + ", ").orElse("") +
            optionalTotalValue().map(f -> "totalValue=" + f + ", ").orElse("") +
            optionalCurrency().map(f -> "currency=" + f + ", ").orElse("") +
            optionalAccountId().map(f -> "accountId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
