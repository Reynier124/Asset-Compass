package com.assetcompass.portfolio.service.criteria;

import com.assetcompass.portfolio.domain.enumeration.TaxType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.assetcompass.portfolio.domain.TaxEvent} entity. This class is used
 * in {@link com.assetcompass.portfolio.web.rest.TaxEventResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /tax-events?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TaxEventCriteria implements Serializable, Criteria {

    /**
     * Class for filtering TaxType
     */
    public static class TaxTypeFilter extends Filter<TaxType> {

        public TaxTypeFilter() {}

        public TaxTypeFilter(TaxTypeFilter filter) {
            super(filter);
        }

        @Override
        public TaxTypeFilter copy() {
            return new TaxTypeFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private TaxTypeFilter type;

    private LocalDateFilter taxDate;

    private BigDecimalFilter amount;

    private StringFilter currency;

    private UUIDFilter accountId;

    private UUIDFilter operationId;

    private UUIDFilter incomeEventId;

    private Boolean distinct;

    public TaxEventCriteria() {}

    public TaxEventCriteria(TaxEventCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.type = other.optionalType().map(TaxTypeFilter::copy).orElse(null);
        this.taxDate = other.optionalTaxDate().map(LocalDateFilter::copy).orElse(null);
        this.amount = other.optionalAmount().map(BigDecimalFilter::copy).orElse(null);
        this.currency = other.optionalCurrency().map(StringFilter::copy).orElse(null);
        this.accountId = other.optionalAccountId().map(UUIDFilter::copy).orElse(null);
        this.operationId = other.optionalOperationId().map(UUIDFilter::copy).orElse(null);
        this.incomeEventId = other.optionalIncomeEventId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public TaxEventCriteria copy() {
        return new TaxEventCriteria(this);
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

    public TaxTypeFilter getType() {
        return type;
    }

    public Optional<TaxTypeFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public TaxTypeFilter type() {
        if (type == null) {
            setType(new TaxTypeFilter());
        }
        return type;
    }

    public void setType(TaxTypeFilter type) {
        this.type = type;
    }

    public LocalDateFilter getTaxDate() {
        return taxDate;
    }

    public Optional<LocalDateFilter> optionalTaxDate() {
        return Optional.ofNullable(taxDate);
    }

    public LocalDateFilter taxDate() {
        if (taxDate == null) {
            setTaxDate(new LocalDateFilter());
        }
        return taxDate;
    }

    public void setTaxDate(LocalDateFilter taxDate) {
        this.taxDate = taxDate;
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

    public UUIDFilter getOperationId() {
        return operationId;
    }

    public Optional<UUIDFilter> optionalOperationId() {
        return Optional.ofNullable(operationId);
    }

    public UUIDFilter operationId() {
        if (operationId == null) {
            setOperationId(new UUIDFilter());
        }
        return operationId;
    }

    public void setOperationId(UUIDFilter operationId) {
        this.operationId = operationId;
    }

    public UUIDFilter getIncomeEventId() {
        return incomeEventId;
    }

    public Optional<UUIDFilter> optionalIncomeEventId() {
        return Optional.ofNullable(incomeEventId);
    }

    public UUIDFilter incomeEventId() {
        if (incomeEventId == null) {
            setIncomeEventId(new UUIDFilter());
        }
        return incomeEventId;
    }

    public void setIncomeEventId(UUIDFilter incomeEventId) {
        this.incomeEventId = incomeEventId;
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
        final TaxEventCriteria that = (TaxEventCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(type, that.type) &&
            Objects.equals(taxDate, that.taxDate) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(currency, that.currency) &&
            Objects.equals(accountId, that.accountId) &&
            Objects.equals(operationId, that.operationId) &&
            Objects.equals(incomeEventId, that.incomeEventId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, taxDate, amount, currency, accountId, operationId, incomeEventId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TaxEventCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalTaxDate().map(f -> "taxDate=" + f + ", ").orElse("") +
            optionalAmount().map(f -> "amount=" + f + ", ").orElse("") +
            optionalCurrency().map(f -> "currency=" + f + ", ").orElse("") +
            optionalAccountId().map(f -> "accountId=" + f + ", ").orElse("") +
            optionalOperationId().map(f -> "operationId=" + f + ", ").orElse("") +
            optionalIncomeEventId().map(f -> "incomeEventId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
