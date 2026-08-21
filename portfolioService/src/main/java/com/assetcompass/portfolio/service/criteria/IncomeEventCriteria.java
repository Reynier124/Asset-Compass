package com.assetcompass.portfolio.service.criteria;

import com.assetcompass.portfolio.domain.enumeration.IncomeType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.assetcompass.portfolio.domain.IncomeEvent} entity. This class is used
 * in {@link com.assetcompass.portfolio.web.rest.IncomeEventResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /income-events?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class IncomeEventCriteria implements Serializable, Criteria {

    /**
     * Class for filtering IncomeType
     */
    public static class IncomeTypeFilter extends Filter<IncomeType> {

        public IncomeTypeFilter() {}

        public IncomeTypeFilter(IncomeTypeFilter filter) {
            super(filter);
        }

        @Override
        public IncomeTypeFilter copy() {
            return new IncomeTypeFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private IncomeTypeFilter type;

    private LocalDateFilter eventDate;

    private BigDecimalFilter amount;

    private StringFilter currency;

    private UUIDFilter accountId;

    private UUIDFilter assetId;

    private Boolean distinct;

    public IncomeEventCriteria() {}

    public IncomeEventCriteria(IncomeEventCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.type = other.optionalType().map(IncomeTypeFilter::copy).orElse(null);
        this.eventDate = other.optionalEventDate().map(LocalDateFilter::copy).orElse(null);
        this.amount = other.optionalAmount().map(BigDecimalFilter::copy).orElse(null);
        this.currency = other.optionalCurrency().map(StringFilter::copy).orElse(null);
        this.accountId = other.optionalAccountId().map(UUIDFilter::copy).orElse(null);
        this.assetId = other.optionalAssetId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public IncomeEventCriteria copy() {
        return new IncomeEventCriteria(this);
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

    public IncomeTypeFilter getType() {
        return type;
    }

    public Optional<IncomeTypeFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public IncomeTypeFilter type() {
        if (type == null) {
            setType(new IncomeTypeFilter());
        }
        return type;
    }

    public void setType(IncomeTypeFilter type) {
        this.type = type;
    }

    public LocalDateFilter getEventDate() {
        return eventDate;
    }

    public Optional<LocalDateFilter> optionalEventDate() {
        return Optional.ofNullable(eventDate);
    }

    public LocalDateFilter eventDate() {
        if (eventDate == null) {
            setEventDate(new LocalDateFilter());
        }
        return eventDate;
    }

    public void setEventDate(LocalDateFilter eventDate) {
        this.eventDate = eventDate;
    }

    public BigDecimalFilter getAmount() {
        return amount;
    }

    public Optional<BigDecimalFilter> optionalAmount() {
        return Optional.ofNullable(amount);
    }

    public BigDecimalFilter amount() {
        if (amount == null) {
            setAmount(new BigDecimalFilter());
        }
        return amount;
    }

    public void setAmount(BigDecimalFilter amount) {
        this.amount = amount;
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
        final IncomeEventCriteria that = (IncomeEventCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(type, that.type) &&
            Objects.equals(eventDate, that.eventDate) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(currency, that.currency) &&
            Objects.equals(accountId, that.accountId) &&
            Objects.equals(assetId, that.assetId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, eventDate, amount, currency, accountId, assetId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "IncomeEventCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalEventDate().map(f -> "eventDate=" + f + ", ").orElse("") +
            optionalAmount().map(f -> "amount=" + f + ", ").orElse("") +
            optionalCurrency().map(f -> "currency=" + f + ", ").orElse("") +
            optionalAccountId().map(f -> "accountId=" + f + ", ").orElse("") +
            optionalAssetId().map(f -> "assetId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
