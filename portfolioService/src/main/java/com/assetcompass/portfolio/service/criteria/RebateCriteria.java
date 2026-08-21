package com.assetcompass.portfolio.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.assetcompass.portfolio.domain.Rebate} entity. This class is used
 * in {@link com.assetcompass.portfolio.web.rest.RebateResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /rebates?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RebateCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private LocalDateFilter rebateDate;

    private BigDecimalFilter amount;

    private StringFilter currency;

    private UUIDFilter accountId;

    private UUIDFilter operationId;

    private Boolean distinct;

    public RebateCriteria() {}

    public RebateCriteria(RebateCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.rebateDate = other.optionalRebateDate().map(LocalDateFilter::copy).orElse(null);
        this.amount = other.optionalAmount().map(BigDecimalFilter::copy).orElse(null);
        this.currency = other.optionalCurrency().map(StringFilter::copy).orElse(null);
        this.accountId = other.optionalAccountId().map(UUIDFilter::copy).orElse(null);
        this.operationId = other.optionalOperationId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public RebateCriteria copy() {
        return new RebateCriteria(this);
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

    public LocalDateFilter getRebateDate() {
        return rebateDate;
    }

    public Optional<LocalDateFilter> optionalRebateDate() {
        return Optional.ofNullable(rebateDate);
    }

    public LocalDateFilter rebateDate() {
        if (rebateDate == null) {
            setRebateDate(new LocalDateFilter());
        }
        return rebateDate;
    }

    public void setRebateDate(LocalDateFilter rebateDate) {
        this.rebateDate = rebateDate;
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
        final RebateCriteria that = (RebateCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(rebateDate, that.rebateDate) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(currency, that.currency) &&
            Objects.equals(accountId, that.accountId) &&
            Objects.equals(operationId, that.operationId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rebateDate, amount, currency, accountId, operationId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RebateCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalRebateDate().map(f -> "rebateDate=" + f + ", ").orElse("") +
            optionalAmount().map(f -> "amount=" + f + ", ").orElse("") +
            optionalCurrency().map(f -> "currency=" + f + ", ").orElse("") +
            optionalAccountId().map(f -> "accountId=" + f + ", ").orElse("") +
            optionalOperationId().map(f -> "operationId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
